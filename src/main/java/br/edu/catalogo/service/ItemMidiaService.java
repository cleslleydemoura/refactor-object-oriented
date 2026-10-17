package br.edu.catalogo.service;

import br.edu.catalogo.dao.ItemMidiaDao;
import br.edu.catalogo.exception.NaoEncontradoException;
import br.edu.catalogo.exception.ValidacaoException;
import br.edu.catalogo.model.ItemMidia;
import br.edu.catalogo.model.TipoMidia;
import java.time.Clock;
import java.time.Year;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Regras de negócio testáveis sem servidor HTTP e sem um banco real. */
public final class ItemMidiaService {
    private final ItemMidiaDao dao;
    private final Clock relogio;
    public ItemMidiaService(ItemMidiaDao dao) { this(dao, Clock.systemDefaultZone()); }
    public ItemMidiaService(ItemMidiaDao dao, Clock relogio) {
        this.dao = Objects.requireNonNull(dao);
        this.relogio = Objects.requireNonNull(relogio);
    }

    /** Valida todos os campos antes de realizar qualquer escrita. */
    public ItemMidia salvar(Long id, Map<String, String> campos) {
        Map<String, String> erros = new LinkedHashMap<>();
        String titulo = texto(campos, "titulo", "Título", 255, erros);
        String autor = texto(campos, "autorDiretor", "Autor ou diretor", 255, erros);
        String genero = texto(campos, "genero", "Gênero", 100, erros);
        String sinopse = texto(campos, "sinopse", "Sinopse", 2000, erros);
        int ano = 0;
        int maximo = Year.now(relogio).getValue() + 1;
        try {
            ano = Integer.parseInt(limpar(campos.get("anoLancamento")));
            if (ano < 1 || ano > maximo) erros.put("anoLancamento", "Informe um ano entre 1 e " + maximo + ".");
        } catch (NumberFormatException e) { erros.put("anoLancamento", "Informe um ano inteiro válido."); }
        TipoMidia tipo = null;
        try { tipo = TipoMidia.valueOf(limpar(campos.get("tipoMidia"))); }
        catch (IllegalArgumentException e) { erros.put("tipoMidia", "Selecione Livro, Filme ou Série."); }
        if (!erros.isEmpty()) throw new ValidacaoException(erros);
        var item = new ItemMidia(id, titulo, autor, ano, genero, sinopse, tipo);
        if (id == null) return dao.inserir(item);
        if (!dao.atualizar(item)) throw new NaoEncontradoException();
        return item;
    }

    public List<ItemMidia> buscar(String termo) {
        String normalizado = limpar(termo);
        if (normalizado.length() > 255) throw new ValidacaoException(Map.of("q", "A busca deve ter até 255 caracteres."));
        return dao.buscar(normalizado);
    }
    public ItemMidia obter(long id) {
        if (id <= 0) throw new NaoEncontradoException();
        return dao.buscarPorId(id).orElseThrow(NaoEncontradoException::new);
    }
    public void excluir(long id) {
        if (id <= 0 || !dao.excluir(id)) throw new NaoEncontradoException();
    }
    private static String texto(Map<String, String> campos, String chave, String nome, int maximo, Map<String, String> erros) {
        String valor = limpar(campos.get(chave));
        if (valor.isEmpty()) erros.put(chave, nome + " é obrigatório.");
        else if (valor.length() > maximo) erros.put(chave, nome + " deve ter até " + maximo + " caracteres.");
        return valor;
    }
    private static String limpar(String valor) { return valor == null ? "" : valor.strip(); }
}
