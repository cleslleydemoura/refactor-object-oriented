package br.edu.catalogo.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Centraliza URL e credenciais; cada operação DAO fecha sua própria conexão. */
public final class ConexaoJdbc implements FabricaConexao {
    private final String url;
    private final String usuario;
    private final String senha;

    public ConexaoJdbc(String url, String usuario, String senha) {
        this.url = url;
        this.usuario = usuario;
        this.senha = senha;
        try { Class.forName("org.h2.Driver"); }
        catch (ClassNotFoundException e) { throw new IllegalStateException("Driver H2 indisponível.", e); }
    }
    public static ConexaoJdbc doAmbiente() {
        return new ConexaoJdbc(valor("CATALOGO_DB_URL", "jdbc:h2:file:./data/catalogo"),
                valor("CATALOGO_DB_USER", "sa"), valor("CATALOGO_DB_PASSWORD", ""));
    }
    private static String valor(String nome, String padrao) {
        String valor = System.getenv(nome);
        return valor == null ? padrao : valor;
    }
    @Override public Connection abrir() throws SQLException {
        return DriverManager.getConnection(url, usuario, senha);
    }
}
