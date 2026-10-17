package br.edu.catalogo.launcher;

import br.edu.catalogo.config.FabricaConexao;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.loader.WebappLoader;
import java.nio.file.Files;
import java.nio.file.Path;

/** Inicializador de demonstração local; o WAR também funciona em Tomcat externo. */
public final class ServidorLocal {
    private ServidorLocal() { }
    public static Tomcat iniciar(int porta, Path webapp, Path diretorio, FabricaConexao fabrica) throws Exception {
        Files.createDirectories(diretorio);
        var tomcat = new Tomcat();
        tomcat.setBaseDir(diretorio.toAbsolutePath().toString());
        tomcat.setPort(porta);
        tomcat.getConnector().setProperty("address", "127.0.0.1");
        tomcat.getConnector().setMaxPostSize(64 * 1024);
        var contexto = tomcat.addWebapp("", webapp.toAbsolutePath().toString());
        contexto.setParentClassLoader(ServidorLocal.class.getClassLoader());
        // No modo embutido, compartilha os tipos com o processo e seus testes.
        var carregador = new WebappLoader();
        carregador.setDelegate(true);
        contexto.setLoader(carregador);
        if (fabrica != null) contexto.getServletContext().setAttribute("fabricaConexao", fabrica);
        tomcat.start();
        if (!contexto.getState().isAvailable()) {
            tomcat.stop();
            tomcat.destroy();
            throw new IllegalStateException("A aplicação web não iniciou. Consulte o log do Tomcat.");
        }
        return tomcat;
    }
    public static void main(String[] args) throws Exception {
        int porta = Integer.parseInt(System.getenv().getOrDefault("CATALOGO_PORT", "8080"));
        var tomcat = iniciar(porta, Path.of("target/catalogo"), Path.of(".runtime/tomcat"), null);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try { tomcat.stop(); tomcat.destroy(); }
            catch (Exception e) { System.err.println("Não foi possível encerrar o servidor: " + e.getMessage()); }
        }));
        System.out.println("\nAcervo disponível em http://localhost:" + tomcat.getConnector().getLocalPort() + "/itens\n");
        tomcat.getServer().await();
    }
}
