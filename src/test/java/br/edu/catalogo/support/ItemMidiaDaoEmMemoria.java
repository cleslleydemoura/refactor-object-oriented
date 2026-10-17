package br.edu.catalogo.support;

import br.edu.catalogo.dao.ItemMidiaDao;
import br.edu.catalogo.model.ItemMidia;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Dublê exclusivo dos testes: demonstra o polimorfismo do contrato DAO. */
public final class ItemMidiaDaoEmMemoria implements ItemMidiaDao {
    public final Map<Long, ItemMidia> itens = new LinkedHashMap<>();
    public int escritas;
    public String ultimoTermo;
    private long sequencia;
    @Override public ItemMidia inserir(ItemMidia item) {
        escritas++;
        ItemMidia salvo = item.comId(++sequencia);
        itens.put(salvo.getId(), salvo);
        return salvo;
    }
    @Override public List<ItemMidia> buscar(String termo) { ultimoTermo = termo; return new ArrayList<>(itens.values()); }
    @Override public Optional<ItemMidia> buscarPorId(long id) { return Optional.ofNullable(itens.get(id)); }
    @Override public boolean atualizar(ItemMidia item) {
        if (!itens.containsKey(item.getId())) return false;
        escritas++;
        itens.put(item.getId(), item);
        return true;
    }
    @Override public boolean excluir(long id) { return itens.remove(id) != null; }
}
