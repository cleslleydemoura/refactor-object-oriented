package br.edu.catalogo.exception;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Erros de negócio organizados por campo para a apresentação do formulário. */
public final class ValidacaoException extends RuntimeException {
    private final Map<String, String> erros;
    public ValidacaoException(Map<String, String> erros) {
        super("Revise os campos indicados.");
        this.erros = Collections.unmodifiableMap(new LinkedHashMap<>(erros));
    }
    public Map<String, String> getErros() { return erros; }
}
