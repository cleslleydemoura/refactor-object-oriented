package br.edu.catalogo.dao;

import br.edu.catalogo.model.ItemMidia;
import java.util.List;
import java.util.Optional;

/** Contrato de persistência, independente de JDBC e de Servlets. */
public interface ItemMidiaDao {
    /** Insere um novo item e retorna a entidade com o identificador gerado. */
    ItemMidia inserir(ItemMidia item);
    /** Lista em ordem alfabética, ou pesquisa por título e autor/diretor. */
    List<ItemMidia> buscar(String termo);
    /** Retorna vazio quando o identificador não existe. */
    Optional<ItemMidia> buscarPorId(long id);
    /** Retorna false quando nenhuma linha foi alterada. */
    boolean atualizar(ItemMidia item);
    /** Retorna false quando o registro já não existe. */
    boolean excluir(long id);
}
