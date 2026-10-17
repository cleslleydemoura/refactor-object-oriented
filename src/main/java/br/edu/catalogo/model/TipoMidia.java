package br.edu.catalogo.model;

/** Valores permitidos para o tipo de obra; o nome é persistido no banco. */
public enum TipoMidia {
    LIVRO("Livro"), FILME("Filme"), SERIE("Série");

    private final String descricao;
    TipoMidia(String descricao) { this.descricao = descricao; }
    public String getDescricao() { return descricao; }
}
