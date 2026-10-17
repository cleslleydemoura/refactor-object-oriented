package br.edu.catalogo.service;

import br.edu.catalogo.exception.NaoEncontradoException;
import br.edu.catalogo.exception.ValidacaoException;
import br.edu.catalogo.support.ItemMidiaDaoEmMemoria;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class ItemMidiaServiceTest {
    private ItemMidiaDaoEmMemoria dao;
    private ItemMidiaService servico;
    @BeforeEach void preparar() {
        dao = new ItemMidiaDaoEmMemoria();
        servico = new ItemMidiaService(dao, Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC));
    }
    private Map<String, String> valido() {
        return new LinkedHashMap<>(Map.of("titulo", "  Dom Casmurro  ", "autorDiretor", " Machado de Assis ",
                "anoLancamento", "1899", "genero", "Romance", "sinopse", "Um narrador revisita sua juventude.", "tipoMidia", "LIVRO"));
    }
    @Test void cadastraENormalizaSemHttpOuJdbc() {
        var item = servico.salvar(null, valido());
        assertNotNull(item.getId());
        assertEquals("Dom Casmurro", item.getTitulo());
        assertEquals("Machado de Assis", item.getAutorDiretor());
        assertEquals(1, dao.escritas);
    }
    @Test void acumulaTodosOsErrosSemEscreverNoDao() {
        var erro = assertThrows(ValidacaoException.class, () -> servico.salvar(null, Map.of()));
        assertEquals(6, erro.getErros().size());
        assertEquals(0, dao.escritas);
        assertThrows(UnsupportedOperationException.class, () -> erro.getErros().put("x", "y"));
    }
    @ParameterizedTest @CsvSource({"titulo,256", "autorDiretor,256", "genero,101", "sinopse,2001"})
    void rejeitaComprimentosAcimaDoLimite(String campo, int tamanho) {
        var dados = valido(); dados.put(campo, "x".repeat(tamanho));
        assertTrue(assertThrows(ValidacaoException.class, () -> servico.salvar(null, dados)).getErros().containsKey(campo));
        assertEquals(0, dao.escritas);
    }
    @ParameterizedTest @CsvSource({"titulo,255", "autorDiretor,255", "genero,100", "sinopse,2000"})
    void aceitaComprimentosNoLimite(String campo, int tamanho) {
        var dados = valido(); dados.put(campo, "x".repeat(tamanho));
        assertDoesNotThrow(() -> servico.salvar(null, dados));
    }
    @ParameterizedTest @ValueSource(strings={"texto", "0", "-1", "2028", "999999999999999999"})
    void rejeitaAnoInvalido(String ano) {
        var dados = valido(); dados.put("anoLancamento", ano);
        assertTrue(assertThrows(ValidacaoException.class, () -> servico.salvar(null, dados)).getErros().containsKey("anoLancamento"));
        assertEquals(0, dao.escritas);
    }
    @ParameterizedTest @ValueSource(strings={"1", "2027"})
    void aceitaLimitesDoAno(String ano) {
        var dados = valido(); dados.put("anoLancamento", ano);
        assertDoesNotThrow(() -> servico.salvar(null, dados));
    }
    @Test void rejeitaTipoNaoPrevisto() {
        var dados = valido(); dados.put("tipoMidia", "JOGO");
        assertTrue(assertThrows(ValidacaoException.class, () -> servico.salvar(null, dados)).getErros().containsKey("tipoMidia"));
    }
    @Test void atualizaMantendoIdentificador() {
        var item = servico.salvar(null, valido());
        var dados = valido(); dados.put("titulo", "Novo título");
        assertEquals(item.getId(), servico.salvar(item.getId(), dados).getId());
        assertEquals("Novo título", servico.obter(item.getId()).getTitulo());
        assertEquals(1, dao.itens.size());
    }
    @Test void naoTransformaEdicaoInexistenteEmCadastro() {
        assertThrows(NaoEncontradoException.class, () -> servico.salvar(999L, valido()));
        assertEquals(0, dao.escritas);
    }
    @Test void consultaEExclusaoInexistentesSaoExplicitas() {
        assertThrows(NaoEncontradoException.class, () -> servico.obter(999));
        assertThrows(NaoEncontradoException.class, () -> servico.obter(0));
        assertThrows(NaoEncontradoException.class, () -> servico.excluir(999));
    }
    @Test void normalizaTermoEAceitaBuscaVazia() {
        servico.buscar("  Machado  "); assertEquals("Machado", dao.ultimoTermo);
        servico.buscar(null); assertEquals("", dao.ultimoTermo);
        assertThrows(ValidacaoException.class, () -> servico.buscar("x".repeat(256)));
    }
    @Test void excluiItemExistente() {
        var item = servico.salvar(null, valido());
        servico.excluir(item.getId());
        assertTrue(dao.itens.isEmpty());
    }
}
