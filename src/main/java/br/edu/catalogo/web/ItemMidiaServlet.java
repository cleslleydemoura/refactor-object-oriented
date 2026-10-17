package br.edu.catalogo.web;

import br.edu.catalogo.exception.NaoEncontradoException;
import br.edu.catalogo.exception.PersistenciaException;
import br.edu.catalogo.exception.ValidacaoException;
import br.edu.catalogo.model.ItemMidia;
import br.edu.catalogo.model.TipoMidia;
import br.edu.catalogo.service.ItemMidiaService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Year;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Controlador MVC: traduz HTTP, delega ao serviço e escolhe a JSP/resposta. */
public final class ItemMidiaServlet extends HttpServlet {
    private static final Logger LOG = Logger.getLogger(ItemMidiaServlet.class.getName());
    private ItemMidiaService servico;

    @Override public void init() throws ServletException {
        servico = (ItemMidiaService) getServletContext().getAttribute("itemService");
        if (servico == null) throw new ServletException("Serviço não inicializado.");
    }

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        executar(req, res, () -> {
            Object flash = req.getSession().getAttribute("flash");
            req.getSession().removeAttribute("flash");
            req.setAttribute("flash", flash);
            switch (rota(req)) {
                case "/" -> {
                    String termo = req.getParameter("q");
                    req.setAttribute("termo", termo == null ? "" : termo);
                    req.setAttribute("itens", servico.buscar(termo));
                    exibir(req, res, "lista");
                }
                case "/novo" -> { prepararFormulario(req, null); exibir(req, res, "formulario"); }
                case "/editar" -> { prepararFormulario(req, servico.obter(id(req))); exibir(req, res, "formulario"); }
                case "/detalhes" -> { req.setAttribute("item", servico.obter(id(req))); exibir(req, res, "detalhes"); }
                case "/salvar", "/excluir" -> { res.setHeader("Allow", "POST"); erro(req, res, 405, "Esta operação exige o envio de um formulário."); }
                default -> erro(req, res, 404, "Página não encontrada.");
            }
        });
    }

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        executar(req, res, () -> {
            switch (rota(req)) {
                case "/salvar" -> {
                    Long identificador = req.getParameter("id") == null || req.getParameter("id").isBlank() ? null : id(req);
                    Map<String, String> campos = lerCampos(req);
                    try {
                        ItemMidia item = servico.salvar(identificador, campos);
                        req.getSession().setAttribute("flash", identificador == null ? "Item cadastrado com sucesso." : "Alterações salvas com sucesso.");
                        redirecionar(req, res, "/itens/detalhes?id=" + item.getId());
                    } catch (ValidacaoException e) {
                        res.setStatus(422);
                        req.setAttribute("form", campos);
                        req.setAttribute("id", identificador);
                        req.setAttribute("erros", e.getErros());
                        opcoes(req);
                        exibir(req, res, "formulario");
                    }
                }
                case "/excluir" -> {
                    servico.excluir(id(req));
                    req.getSession().setAttribute("flash", "Item excluído com sucesso.");
                    redirecionar(req, res, "/itens");
                }
                default -> erro(req, res, 404, "Operação não encontrada.");
            }
        });
    }

    private void executar(HttpServletRequest req, HttpServletResponse res, Acao acao) throws ServletException, IOException {
        try { acao.executar(); }
        catch (NaoEncontradoException e) { erro(req, res, 404, e.getMessage()); }
        catch (NumberFormatException e) { erro(req, res, 400, "O identificador informado é inválido."); }
        catch (ValidacaoException e) { erro(req, res, 400, e.getErros().values().iterator().next()); }
        catch (PersistenciaException e) {
            LOG.log(Level.SEVERE, "Falha de persistência ao atender a requisição", e);
            erro(req, res, 503, "O catálogo está temporariamente indisponível. Tente novamente em instantes.");
        }
    }

    private static long id(HttpServletRequest req) {
        String valor = req.getParameter("id");
        if (valor == null || !valor.matches("[1-9][0-9]*")) throw new NumberFormatException("ID inválido");
        return Long.parseLong(valor);
    }
    private static String rota(HttpServletRequest req) {
        return req.getPathInfo() == null || req.getPathInfo().isEmpty() ? "/" : req.getPathInfo();
    }
    private static Map<String, String> lerCampos(HttpServletRequest req) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (String campo : new String[]{"titulo", "autorDiretor", "anoLancamento", "genero", "sinopse", "tipoMidia"}) {
            String valor = req.getParameter(campo);
            campos.put(campo, valor == null ? "" : valor);
        }
        return campos;
    }
    private static void prepararFormulario(HttpServletRequest req, ItemMidia item) {
        req.setAttribute("id", item == null ? null : item.getId());
        req.setAttribute("form", item == null ? Map.of("tipoMidia", "LIVRO") : Map.of(
                "titulo", item.getTitulo(), "autorDiretor", item.getAutorDiretor(),
                "anoLancamento", String.valueOf(item.getAnoLancamento()), "genero", item.getGenero(),
                "sinopse", item.getSinopse(), "tipoMidia", item.getTipoMidia().name()));
        opcoes(req);
    }
    private static void opcoes(HttpServletRequest req) {
        req.setAttribute("tipos", TipoMidia.values());
        req.setAttribute("anoMaximo", Year.now().getValue() + 1);
    }
    private static void exibir(HttpServletRequest req, HttpServletResponse res, String pagina) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/" + pagina + ".jsp").forward(req, res);
    }
    private static void erro(HttpServletRequest req, HttpServletResponse res, int status, String mensagem) throws ServletException, IOException {
        res.setStatus(status);
        req.setAttribute("mensagemErro", mensagem);
        exibir(req, res, "erro");
    }
    private static void redirecionar(HttpServletRequest req, HttpServletResponse res, String caminho) {
        // Post/Redirect/Get: atualizar o navegador não repete a gravação.
        res.setStatus(303);
        res.setHeader("Location", req.getContextPath() + caminho);
    }
    @FunctionalInterface private interface Acao {
        void executar() throws ServletException, IOException;
    }
}
