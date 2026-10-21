# Registro de verificação executada

Data da preparação: 17 de outubro de 2026 (America/Sao_Paulo).

Comando: `maven.ps1 verify` (Maven 3.9.9; JDK 17.0.12).

Resultado: **APROVADO**. As contagens abaixo correspondem aos relatórios XML JUnit da execução registrada na entrega.

| Suíte | Testes | Falhas | Erros | Ignorados | Duração da suíte |
| --- | --- | --- | --- | --- | --- |
| JdbcItemMidiaDaoTest | 8 | 0 | 0 | 0 | 0.601 s |
| ItemMidiaServiceTest | 23 | 0 | 0 | 0 | 0.141 s |
| CatalogoHttpIT | 8 | 0 | 0 | 0 | 4.426 s |
| Total | 39 | 0 | 0 | 0 | — |

Os tempos da tabela correspondem à execução das suítes de testes. O desenvolvimento do projeto foi realizado ao longo de dez dias.

## Ambiente e alcance

Windows; Java 17; Tomcat 10.1.61; H2 2.3.232; Servlet 6.0; JSP 3.1; JUnit 5.12.2. Testes JDBC usam bancos isolados. Testes HTTP iniciam um Tomcat real em porta livre e exercitam as JSPs.

Foi verificada persistência real com fechamento e reabertura de um banco H2 em arquivo temporário. A indisponibilidade simulada do banco produz 503 e mensagem pública sem detalhes internos. O log técnico dessa simulação é esperado e não representa uma falha da suíte.

## Casos executados

### JdbcItemMidiaDaoTest

- APROVADO: aspasEInjecaoSaoDadosLiterais
- APROVADO: buscaPorTituloOuAutorSemDiferenciarMaiusculas
- APROVADO: curingasNaoAmpliamBuscaLiteral
- APROVADO: dadosSobrevivemAoFechamentoEReabertura(Path)
- APROVADO: falhaSqlPreservaCausaSemExporJdbcAoServico
- APROVADO: restricoesDoBancoProtegemIntegridade
- APROVADO: reinicializarEstruturaNaoApagaDados
- APROVADO: crudRealComChaveGerada

### ItemMidiaServiceTest

- APROVADO: acumulaTodosOsErrosSemEscreverNoDao
- APROVADO: cadastraENormalizaSemHttpOuJdbc
- APROVADO: excluiItemExistente
- APROVADO: naoTransformaEdicaoInexistenteEmCadastro
- APROVADO: consultaEExclusaoInexistentesSaoExplicitas
- APROVADO: rejeitaAnoInvalido(String)[1]
- APROVADO: rejeitaAnoInvalido(String)[2]
- APROVADO: rejeitaAnoInvalido(String)[3]
- APROVADO: rejeitaAnoInvalido(String)[4]
- APROVADO: rejeitaAnoInvalido(String)[5]
- APROVADO: rejeitaComprimentosAcimaDoLimite(String, int)[1]
- APROVADO: rejeitaComprimentosAcimaDoLimite(String, int)[2]
- APROVADO: rejeitaComprimentosAcimaDoLimite(String, int)[3]
- APROVADO: rejeitaComprimentosAcimaDoLimite(String, int)[4]
- APROVADO: aceitaLimitesDoAno(String)[1]
- APROVADO: aceitaLimitesDoAno(String)[2]
- APROVADO: aceitaComprimentosNoLimite(String, int)[1]
- APROVADO: aceitaComprimentosNoLimite(String, int)[2]
- APROVADO: aceitaComprimentosNoLimite(String, int)[3]
- APROVADO: aceitaComprimentosNoLimite(String, int)[4]
- APROVADO: atualizaMantendoIdentificador
- APROVADO: rejeitaTipoNaoPrevisto
- APROVADO: normalizaTermoEAceitaBuscaVazia

### CatalogoHttpIT

- APROVADO: buscaMaliciosaNaoRetornaTodosOsItens
- APROVADO: cabecalhosEViewsInternas
- APROVADO: rejeitaCsrfAusenteEIncorreto
- APROVADO: getNaoExcluiEIdentificadoresInvalidosNaoQuebramAplicacao
- APROVADO: escapaXssNaListaDetalhesEFormulario
- APROVADO: fluxoCompletoCadastroBuscaEdicaoExclusao
- APROVADO: erroDeValidacaoPreservaDadosNaoGravaEPermiteCorrigir
- APROVADO: indisponibilidadeDoBancoRetornaMensagemSemDetalhesInternos

## Escopo da avaliação

A avaliação abrange as regras de negócio, o acesso JDBC, a persistência em arquivo e os fluxos HTTP com Tomcat embutido. Os testes verificam também entradas inválidas, CSRF, escape de HTML e respostas de erro. Testes de carga, avaliação com usuários e análise formal de acessibilidade são possibilidades de aprofundamento do projeto.
