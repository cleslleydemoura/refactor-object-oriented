package br.edu.catalogo.config;

import br.edu.catalogo.exception.PersistenciaException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/** Cria apenas estruturas ausentes, sem apagar ou popular dados do usuário. */
public final class BancoDados {
    private BancoDados() { }
    public static void inicializar(FabricaConexao fabrica) {
        try (var recurso = BancoDados.class.getResourceAsStream("/db/schema.sql")) {
            if (recurso == null) throw new IllegalStateException("schema.sql não encontrado.");
            String script = new String(recurso.readAllBytes(), StandardCharsets.UTF_8);
            try (var conexao = fabrica.abrir(); var comando = conexao.createStatement()) {
                for (String sql : script.split(";")) {
                    if (!sql.isBlank()) comando.execute(sql);
                }
            }
        } catch (SQLException | IOException e) {
            throw new PersistenciaException("inicializar a estrutura", e);
        }
    }
}
