package br.edu.catalogo.config;

import java.sql.Connection;
import java.sql.SQLException;

/** Contrato injetável: produção usa arquivo H2 e testes usam banco isolado. */
@FunctionalInterface
public interface FabricaConexao {
    Connection abrir() throws SQLException;
}
