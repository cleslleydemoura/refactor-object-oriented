package br.edu.catalogo.dao;

import br.edu.catalogo.config.FabricaConexao;
import br.edu.catalogo.exception.PersistenciaException;
import br.edu.catalogo.model.ItemMidia;
import br.edu.catalogo.model.TipoMidia;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/** Único ponto de CRUD SQL: reutiliza conexão, mapeamento e parâmetros. */
public final class JdbcItemMidiaDao implements ItemMidiaDao {
    private static final String COLUNAS = "id, titulo, autor_diretor, ano_lancamento, genero, sinopse, tipo_midia";
    private final FabricaConexao fabrica;
    public JdbcItemMidiaDao(FabricaConexao fabrica) { this.fabrica = fabrica; }

    @Override public ItemMidia inserir(ItemMidia item) {
        String sql = "INSERT INTO item_midia (titulo, autor_diretor, ano_lancamento, genero, sinopse, tipo_midia) VALUES (?, ?, ?, ?, ?, ?)";
        try (var conexao = fabrica.abrir(); var comando = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preencher(comando, item);
            comando.executeUpdate();
            try (var chaves = comando.getGeneratedKeys()) {
                if (!chaves.next()) throw new SQLException("Chave gerada não retornada.");
                return item.comId(chaves.getLong(1));
            }
        } catch (SQLException e) { throw new PersistenciaException("inserir item", e); }
    }

    @Override public List<ItemMidia> buscar(String termo) {
        String sql = "SELECT " + COLUNAS + " FROM item_midia";
        if (termo.isEmpty()) return consultar(sql + " ORDER BY LOWER(titulo), id", comando -> { });
        // ! é o caractere de escape: %, _ e ! digitados são procurados literalmente.
        String padrao = "%" + termo.toLowerCase(Locale.ROOT).replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
        return consultar(sql + " WHERE LOWER(titulo) LIKE ? ESCAPE '!' OR LOWER(autor_diretor) LIKE ? ESCAPE '!' ORDER BY LOWER(titulo), id", comando -> {
            comando.setString(1, padrao);
            comando.setString(2, padrao);
        });
    }

    @Override public Optional<ItemMidia> buscarPorId(long id) {
        return consultar("SELECT " + COLUNAS + " FROM item_midia WHERE id = ?", comando -> comando.setLong(1, id))
                .stream().findFirst();
    }

    @Override public boolean atualizar(ItemMidia item) {
        return modificar("UPDATE item_midia SET titulo=?, autor_diretor=?, ano_lancamento=?, genero=?, sinopse=?, tipo_midia=? WHERE id=?", comando -> {
            preencher(comando, item);
            comando.setLong(7, item.getId());
        });
    }

    @Override public boolean excluir(long id) {
        return modificar("DELETE FROM item_midia WHERE id = ?", comando -> comando.setLong(1, id));
    }

    private List<ItemMidia> consultar(String sql, Parametros parametros) {
        try (var conexao = fabrica.abrir(); var comando = conexao.prepareStatement(sql)) {
            parametros.aplicar(comando);
            try (var resultado = comando.executeQuery()) {
                List<ItemMidia> itens = new ArrayList<>();
                while (resultado.next()) itens.add(mapear(resultado));
                return itens;
            }
        } catch (SQLException e) { throw new PersistenciaException("consultar itens", e); }
    }

    private boolean modificar(String sql, Parametros parametros) {
        try (var conexao = fabrica.abrir(); var comando = conexao.prepareStatement(sql)) {
            parametros.aplicar(comando);
            return comando.executeUpdate() > 0;
        } catch (SQLException e) { throw new PersistenciaException("alterar item", e); }
    }

    private static void preencher(PreparedStatement comando, ItemMidia item) throws SQLException {
        comando.setString(1, item.getTitulo());
        comando.setString(2, item.getAutorDiretor());
        comando.setInt(3, item.getAnoLancamento());
        comando.setString(4, item.getGenero());
        comando.setString(5, item.getSinopse());
        comando.setString(6, item.getTipoMidia().name());
    }

    private static ItemMidia mapear(ResultSet resultado) throws SQLException {
        return new ItemMidia(resultado.getLong("id"), resultado.getString("titulo"),
                resultado.getString("autor_diretor"), resultado.getInt("ano_lancamento"),
                resultado.getString("genero"), resultado.getString("sinopse"),
                TipoMidia.valueOf(resultado.getString("tipo_midia")));
    }

    @FunctionalInterface private interface Parametros {
        void aplicar(PreparedStatement comando) throws SQLException;
    }
}
