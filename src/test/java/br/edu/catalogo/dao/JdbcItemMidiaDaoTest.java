package br.edu.catalogo.dao;

import br.edu.catalogo.config.BancoDados;
import br.edu.catalogo.config.ConexaoJdbc;
import br.edu.catalogo.exception.PersistenciaException;
import br.edu.catalogo.model.ItemMidia;
import br.edu.catalogo.model.TipoMidia;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class JdbcItemMidiaDaoTest {
    private JdbcItemMidiaDao dao;
    private ConexaoJdbc fabrica;
    @BeforeEach void preparar() {
        fabrica = new ConexaoJdbc("jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1", "sa", "");
        BancoDados.inicializar(fabrica);
        dao = new JdbcItemMidiaDao(fabrica);
    }
    private ItemMidia item(String titulo, String autor) {
        return new ItemMidia(null, titulo, autor, 1899, "Romance", "Sinopse da obra.", TipoMidia.LIVRO);
    }
    @Test void crudRealComChaveGerada() {
        var salvo = dao.inserir(item("Dom Casmurro", "Machado de Assis"));
        assertTrue(salvo.getId() > 0);
        assertEquals("Dom Casmurro", dao.buscarPorId(salvo.getId()).orElseThrow().getTitulo());
        var editado = new ItemMidia(salvo.getId(), "Título editado", "Outro autor", 2001, "Drama", "Nova sinopse", TipoMidia.FILME);
        assertTrue(dao.atualizar(editado));
        var lido = dao.buscarPorId(salvo.getId()).orElseThrow();
        assertAll(() -> assertEquals("Título editado", lido.getTitulo()), () -> assertEquals("Outro autor", lido.getAutorDiretor()),
                () -> assertEquals(2001, lido.getAnoLancamento()), () -> assertEquals("Drama", lido.getGenero()),
                () -> assertEquals("Nova sinopse", lido.getSinopse()), () -> assertEquals(TipoMidia.FILME, lido.getTipoMidia()));
        assertTrue(dao.excluir(salvo.getId()));
        assertFalse(dao.excluir(salvo.getId()));
        assertTrue(dao.buscarPorId(salvo.getId()).isEmpty());
    }
    @Test void buscaPorTituloOuAutorSemDiferenciarMaiusculas() {
        dao.inserir(item("Dom Casmurro", "Machado de Assis"));
        dao.inserir(item("Outra obra", "Outro autor"));
        assertEquals(1, dao.buscar("CASMURRO").size());
        assertEquals(1, dao.buscar("mAcHaDo").size());
        assertEquals(2, dao.buscar("").size());
    }
    @Test void aspasEInjecaoSaoDadosLiterais() {
        dao.inserir(item("O livro de D'Ávila", "Autor"));
        assertEquals(1, dao.buscar("D'Ávila").size());
        assertEquals(0, dao.buscar("' OR 1=1 --").size());
        assertEquals(1, dao.buscar("").size());
    }
    @Test void curingasNaoAmpliamBuscaLiteral() {
        dao.inserir(item("100% real_!", "Autor"));
        dao.inserir(item("Outro título", "Outro autor"));
        assertEquals(1, dao.buscar("%").size());
        assertEquals(1, dao.buscar("_").size());
        assertEquals(1, dao.buscar("!").size());
    }
    @Test void falhaSqlPreservaCausaSemExporJdbcAoServico() {
        var indisponivel = new JdbcItemMidiaDao(() -> { throw new SQLException("Falha simulada"); });
        assertInstanceOf(SQLException.class, assertThrows(PersistenciaException.class, () -> indisponivel.buscar("")).getCause());
    }
    @Test void restricoesDoBancoProtegemIntegridade() {
        assertThrows(PersistenciaException.class, () -> dao.inserir(new ItemMidia(null, "", "Autor", 2000, "Drama", "Sinopse", TipoMidia.LIVRO)));
        assertThrows(PersistenciaException.class, () -> dao.inserir(new ItemMidia(null, "Título", "Autor", 0, "Drama", "Sinopse", TipoMidia.LIVRO)));
    }
    @Test void reinicializarEstruturaNaoApagaDados() {
        dao.inserir(item("Preservado", "Autor"));
        BancoDados.inicializar(fabrica);
        assertEquals(1, dao.buscar("").size());
    }
    @Test void dadosSobrevivemAoFechamentoEReabertura(@TempDir Path pasta) {
        String url = "jdbc:h2:file:" + pasta.resolve("catalogo").toAbsolutePath().toString().replace('\\', '/');
        var arquivo = new ConexaoJdbc(url, "sa", "");
        BancoDados.inicializar(arquivo);
        var primeiro = new JdbcItemMidiaDao(arquivo);
        var salvo = primeiro.inserir(item("Persistido em disco", "Autor"));
        // Não há DB_CLOSE_DELAY: o H2 fecha o arquivo ao fechar a última conexão.
        var reaberto = new JdbcItemMidiaDao(new ConexaoJdbc(url, "sa", ""));
        assertEquals("Persistido em disco", reaberto.buscarPorId(salvo.getId()).orElseThrow().getTitulo());
    }
}
