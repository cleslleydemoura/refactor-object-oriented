package br.edu.catalogo.model;

import java.util.Objects;

/** Entidade imutável: concentra o estado de uma obra, sem conhecer HTTP ou SQL. */
public final class ItemMidia {
    private final Long id;
    private final String titulo;
    private final String autorDiretor;
    private final int anoLancamento;
    private final String genero;
    private final String sinopse;
    private final TipoMidia tipoMidia;

    public ItemMidia(Long id, String titulo, String autorDiretor, int anoLancamento,
                     String genero, String sinopse, TipoMidia tipoMidia) {
        this.id = id;
        this.titulo = Objects.requireNonNull(titulo);
        this.autorDiretor = Objects.requireNonNull(autorDiretor);
        this.anoLancamento = anoLancamento;
        this.genero = Objects.requireNonNull(genero);
        this.sinopse = Objects.requireNonNull(sinopse);
        this.tipoMidia = Objects.requireNonNull(tipoMidia);
    }

    /** Produz a versão persistida sem modificar o objeto original. */
    public ItemMidia comId(long novoId) {
        return new ItemMidia(novoId, titulo, autorDiretor, anoLancamento, genero, sinopse, tipoMidia);
    }
    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getAutorDiretor() { return autorDiretor; }
    public int getAnoLancamento() { return anoLancamento; }
    public String getGenero() { return genero; }
    public String getSinopse() { return sinopse; }
    public TipoMidia getTipoMidia() { return tipoMidia; }
}
