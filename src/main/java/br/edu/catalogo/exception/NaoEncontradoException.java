package br.edu.catalogo.exception;

/** Indica uma operação sobre um item que não existe mais no catálogo. */
public final class NaoEncontradoException extends RuntimeException {
    public NaoEncontradoException() { super("O item solicitado não foi encontrado."); }
}
