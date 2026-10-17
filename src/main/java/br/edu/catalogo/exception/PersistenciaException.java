package br.edu.catalogo.exception;

/** Isola SQLException da camada de negócio e conserva a causa para o log. */
public final class PersistenciaException extends RuntimeException {
    public PersistenciaException(String operacao, Throwable causa) {
        super("Falha ao " + operacao + " no banco de dados.", causa);
    }
}
