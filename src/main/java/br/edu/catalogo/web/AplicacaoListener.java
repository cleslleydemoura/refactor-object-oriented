package br.edu.catalogo.web;

import br.edu.catalogo.config.BancoDados;
import br.edu.catalogo.config.ConexaoJdbc;
import br.edu.catalogo.config.FabricaConexao;
import br.edu.catalogo.dao.JdbcItemMidiaDao;
import br.edu.catalogo.service.ItemMidiaService;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

/** Composition root: constrói o grafo de dependências uma única vez. */
public final class AplicacaoListener implements ServletContextListener {
    @Override public void contextInitialized(ServletContextEvent evento) {
        var contexto = evento.getServletContext();
        var fabrica = (FabricaConexao) contexto.getAttribute("fabricaConexao");
        if (fabrica == null) fabrica = ConexaoJdbc.doAmbiente();
        BancoDados.inicializar(fabrica);
        contexto.setAttribute("itemService", new ItemMidiaService(new JdbcItemMidiaDao(fabrica)));
    }
}
