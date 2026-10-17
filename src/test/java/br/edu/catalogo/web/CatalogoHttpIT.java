package br.edu.catalogo.web;

import br.edu.catalogo.config.ConexaoJdbc;
import br.edu.catalogo.dao.JdbcItemMidiaDao;
import br.edu.catalogo.launcher.ServidorLocal;
import br.edu.catalogo.model.ItemMidia;
import br.edu.catalogo.model.TipoMidia;
import java.net.CookieManager;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.catalina.startup.Tomcat;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

/** Testa HTTP -> filtro -> Servlet -> serviço -> JDBC -> JSP no Tomcat real. */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CatalogoHttpIT {
    @TempDir static Path temporario;
    private Tomcat tomcat;
    private ConexaoJdbc fabrica;
    private JdbcItemMidiaDao dao;
    private HttpClient cliente;
    private String base;
    private final AtomicBoolean falha = new AtomicBoolean();

    @BeforeAll void iniciar() throws Exception {
        fabrica = new ConexaoJdbc("jdbc:h2:mem:http-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1", "sa", "");
        tomcat = ServidorLocal.iniciar(0, Path.of("target/catalogo"), temporario, () -> {
            if (falha.get()) throw new SQLException("credencial-interna-que-nao-pode-vazar");
            return fabrica.abrir();
        });
        base = "http://127.0.0.1:" + tomcat.getConnector().getLocalPort();
        dao = new JdbcItemMidiaDao(fabrica);
    }
    @BeforeEach void limpar() throws Exception {
        cliente = HttpClient.newBuilder().cookieHandler(new CookieManager()).build();
        try (var conexao = fabrica.abrir(); var comando = conexao.createStatement()) { comando.executeUpdate("DELETE FROM item_midia"); }
    }
    @AfterAll void encerrar() throws Exception { if (tomcat != null) { tomcat.stop(); tomcat.destroy(); } }
    private HttpResponse<String> get(String rota) throws Exception {
        return cliente.send(HttpRequest.newBuilder(URI.create(base + rota)).GET().build(), HttpResponse.BodyHandlers.ofString());
    }
    private HttpResponse<String> post(String rota, Map<String, String> campos) throws Exception {
        String corpo = campos.entrySet().stream().map(e -> codificar(e.getKey()) + "=" + codificar(e.getValue())).collect(Collectors.joining("&"));
        return cliente.send(HttpRequest.newBuilder(URI.create(base + rota)).header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(corpo)).build(), HttpResponse.BodyHandlers.ofString());
    }
    private static String codificar(String valor) { return URLEncoder.encode(valor, StandardCharsets.UTF_8); }
    private Map<String, String> formulario() throws Exception {
        var pagina = get("/itens/novo");
        assertEquals(200, pagina.statusCode(), pagina.body());
        var matcher = Pattern.compile("name=\"csrfToken\" value=\"([^\"]+)\"").matcher(pagina.body());
        assertTrue(matcher.find());
        return new LinkedHashMap<>(Map.of("csrfToken", matcher.group(1), "titulo", "Dom Casmurro", "autorDiretor", "Machado de Assis",
                "anoLancamento", "1899", "genero", "Romance", "sinopse", "Um narrador revisita a juventude.", "tipoMidia", "LIVRO"));
    }
    private ItemMidia cadastrarDireto(String titulo) {
        return dao.inserir(new ItemMidia(null, titulo, "Machado de Assis", 1899, "Romance", "Sinopse de teste.", TipoMidia.LIVRO));
    }
    @Test void fluxoCompletoCadastroBuscaEdicaoExclusao() throws Exception {
        var campos = formulario();
        var cadastro = post("/itens/salvar", campos);
        assertEquals(303, cadastro.statusCode(), cadastro.body());
        String detalhes = cadastro.headers().firstValue("Location").orElseThrow();
        assertTrue(get(detalhes).body().contains("Dom Casmurro"));
        assertTrue(get("/itens?q=machado").body().contains("Dom Casmurro"));
        var item = dao.buscar("").get(0);
        assertTrue(get("/itens/editar?id=" + item.getId()).body().contains("Dom Casmurro"));
        campos.put("id", item.getId().toString()); campos.put("titulo", "Dom Casmurro — edição revisada");
        assertEquals(303, post("/itens/salvar", campos).statusCode());
        assertTrue(get(detalhes).body().contains("edição revisada"));
        assertEquals(1, dao.buscar("").size());
        assertEquals(303, post("/itens/excluir", Map.of("id", item.getId().toString(), "csrfToken", campos.get("csrfToken"))).statusCode());
        assertTrue(dao.buscar("").isEmpty());
        assertEquals(404, get(detalhes).statusCode());
    }
    @Test void erroDeValidacaoPreservaDadosNaoGravaEPermiteCorrigir() throws Exception {
        var campos = formulario(); campos.put("anoLancamento", "abc");
        var resposta = post("/itens/salvar", campos);
        assertEquals(422, resposta.statusCode());
        assertTrue(resposta.body().contains("Dom Casmurro"));
        assertTrue(resposta.body().contains("ano inteiro válido"));
        assertTrue(dao.buscar("").isEmpty());
        campos.put("anoLancamento", "1899");
        assertEquals(303, post("/itens/salvar", campos).statusCode());
    }
    @Test void rejeitaCsrfAusenteEIncorreto() throws Exception {
        var campos = formulario(); campos.remove("csrfToken");
        assertEquals(403, post("/itens/salvar", campos).statusCode());
        campos.put("csrfToken", "invalido");
        assertEquals(403, post("/itens/salvar", campos).statusCode());
        assertTrue(dao.buscar("").isEmpty());
    }
    @Test void getNaoExcluiEIdentificadoresInvalidosNaoQuebramAplicacao() throws Exception {
        var item = cadastrarDireto("Obra preservada");
        assertEquals(405, get("/itens/excluir?id=" + item.getId()).statusCode());
        assertTrue(dao.buscarPorId(item.getId()).isPresent());
        assertEquals(400, get("/itens/detalhes?id=abc").statusCode());
        assertEquals(400, get("/itens/detalhes?id=99999999999999999999999").statusCode());
        assertEquals(400, get("/itens/detalhes").statusCode());
        assertEquals(404, get("/itens/detalhes?id=999999").statusCode());
        assertEquals(404, get("/itens/rota-inexistente").statusCode());
    }
    @Test void escapaXssNaListaDetalhesEFormulario() throws Exception {
        String carga = "<script>alert('x')</script>";
        var item = cadastrarDireto(carga);
        for (String rota : new String[]{"/itens", "/itens/detalhes?id=" + item.getId(), "/itens/editar?id=" + item.getId()}) {
            var pagina = get(rota);
            assertEquals(200, pagina.statusCode());
            assertFalse(pagina.body().contains(carga));
            assertTrue(pagina.body().contains("&lt;script&gt;"), pagina.body());
        }
    }
    @Test void buscaMaliciosaNaoRetornaTodosOsItens() throws Exception {
        cadastrarDireto("Obra segura");
        var pagina = get("/itens?q=" + codificar("' OR 1=1 --"));
        assertEquals(200, pagina.statusCode());
        assertFalse(pagina.body().contains("Obra segura"));
        assertEquals(1, dao.buscar("").size());
    }
    @Test void cabecalhosEViewsInternas() throws Exception {
        var pagina = get("/itens");
        assertEquals(200, pagina.statusCode());
        assertTrue(pagina.body().contains("Meu catálogo"));
        assertTrue(pagina.body().contains("Histórias que ficam com você."));
        assertEquals("nosniff", pagina.headers().firstValue("X-Content-Type-Options").orElseThrow());
        assertTrue(pagina.headers().firstValue("Content-Security-Policy").orElseThrow().contains("frame-ancestors 'none'"));
        assertEquals(404, get("/WEB-INF/views/lista.jsp").statusCode());
        assertTrue(get("/assets/style.css").body().contains(".card-grid"));
        assertTrue(get("/assets/app.js").body().contains("sinopse"));
    }
    @Test void indisponibilidadeDoBancoRetornaMensagemSemDetalhesInternos() throws Exception {
        falha.set(true);
        try {
            var pagina = get("/itens");
            assertEquals(503, pagina.statusCode());
            assertTrue(pagina.body().contains("temporariamente indisponível"));
            assertFalse(pagina.body().contains("credencial-interna"));
            assertFalse(pagina.body().contains("SQLException"));
        } finally { falha.set(false); }
    }
}
