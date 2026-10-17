# Acervo — Situação-Problema 3

Projeto acadêmico de **refatoração e orientação a objetos**, baseado nas páginas 39–40 do material *Projetos Computacionais: da Teoria à Prática*, de Artur Marques, fornecido pelo estudante.

Catálogo web de livros, filmes e séries, com CRUD, busca e persistência em banco relacional. Tecnologias: Java 17, Servlets, JSP/JSTL, JDBC, H2 e Tomcat 10.1. O projeto separa apresentação, controle HTTP, regras de negócio e acesso aos dados.

## Começar

No PowerShell, dentro desta pasta:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\iniciar.ps1
```

Abra **http://localhost:8080/itens**. Encerre com `Ctrl+C`. É necessário JDK 17 ou superior; na primeira execução é necessária internet para baixar Maven e dependências. O Maven é instalado somente em `.tools/`, e as dependências ficam em `.m2/` dentro do projeto.

Para testar todas as camadas:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\maven.ps1 verify
```

Com Maven já instalado, também é possível usar `mvn verify`. Em Linux/macOS: `mvn package` e `java -cp 'target/classes:target/dependency/*' br.edu.catalogo.launcher.ServidorLocal`.

## Entrega e documentação

- [Formulário de intervenção preenchido](docs/PROJETO_INTERVENCAO.md), também em Word e PDF na pasta `entrega`.
- [Relatório técnico](docs/RELATORIO_TECNICO.md): problema, requisitos, modelagem, refatoração, segurança, testes e resultados.
- [Guia minucioso do código](docs/GUIA_CODIGO.md): arquivos, classes, métodos, SQL e fluxos explicados.
- [Manual do usuário](docs/MANUAL_USUARIO.md): instalação, operação, backup e resolução de dificuldades.
- [Verificação executada](docs/VERIFICACAO.md): resultados reais das suítes automatizadas.
- [Roteiro de apresentação](docs/ROTEIRO_APRESENTACAO.md).
- [Diagramas editáveis](docs/diagramas/): casos de uso, classes, DER e sequência.
- [Javadoc navegável](docs/javadoc/index.html): documentação gerada das classes Java.
- `entrega/`: documentos em PDF/DOCX, pacote ZIP, aplicação WAR e capturas das telas.

**Antes de entregar:** preencha Nome e RGM no Word, confira as regras da sua instituição e ajuste o campo Tempo para refletir seu próprio trabalho. O cronograma informado é uma estimativa, não uma medição do esforço do estudante.

O ZIP também inclui a compilação e as dependências. Depois de extrair o pacote, execute `iniciar.ps1 -SemCompilar` para iniciar sem baixar o Maven; o JDK 17 continua necessário.

Para regenerar a documentação, instale `scripts/requirements-docs.txt`, execute os testes completos, prepare as capturas de tela e execute `python scripts/gerar_documentos.py`. As capturas podem ser refeitas com um catálogo demonstrativo em execução e `node scripts/capturar_telas.mjs URL` (Node 22+ e Chrome instalado).

## Estrutura

```text
src/main/java/br/edu/catalogo/
  model/       entidade e enum de tipo de mídia
  config/      conexões JDBC e inicialização do banco
  dao/         contrato e implementação SQL
  service/     regras de negócio e validação
  exception/   falhas de validação, consulta e persistência
  web/         Servlet, filtro e montagem de dependências
  launcher/    servidor local para demonstração
src/main/resources/db/schema.sql
src/main/webapp/WEB-INF/views/     páginas JSP protegidas
src/main/webapp/assets/           CSS e JavaScript
src/test/java/                    testes de serviço, JDBC e HTTP
docs/                             documentação-fonte editável
scripts/                          utilitários de demonstração e documentos
```

## Dados e configuração

O catálogo inicia vazio. O banco é criado automaticamente em `data/catalogo.mv.db` e é conservado entre execuções. Nenhum teste utiliza esse arquivo. Não abra duas instâncias simultâneas com o mesmo banco.

| Variável | Padrão | Finalidade |
| --- | --- | --- |
| `CATALOGO_PORT` | `8080` | Porta do servidor de demonstração |
| `CATALOGO_DB_URL` | `jdbc:h2:file:./data/catalogo` | Endereço JDBC do H2 |
| `CATALOGO_DB_USER` | `sa` | Usuário do banco local |
| `CATALOGO_DB_PASSWORD` | vazio | Senha do banco local |

Exemplo de outra porta: `$env:CATALOGO_PORT = '8081'`. Para trocar a configuração, encerre e reinicie o processo. O servidor de demonstração atende apenas na máquina local. O escopo é um catálogo pessoal acadêmico, sem autenticação ou autorização de usuários.

Para popular **um catálogo vazio** com seis obras demonstrativas, com o servidor iniciado:

```powershell
python .\scripts\dados_demonstracao.py
```

Esse utilitário usa apenas a biblioteca padrão do Python. Os resumos são textos originais preparados para demonstração.

## Deploy alternativo

`mvn package` gera `target/catalogo.war`. Copie-o para `webapps` de um Tomcat **10.1**, executando com JDK 17+, e acesse `http://localhost:8080/catalogo/itens`. Não use Tomcat 9: seus pacotes `javax.servlet` são diferentes de `jakarta.servlet`. A persistência relativa ao diretório de trabalho do servidor pode ser substituída por uma URL H2 absoluta. O WAR não contém as bibliotecas Tomcat usadas pelo inicializador local.

As especificações adotadas são documentadas oficialmente por [Apache Tomcat](https://tomcat.apache.org/tomcat-10.1-doc/index.html), [Jakarta Tags](https://jakarta.ee/specifications/tags/3.0/) e [H2](https://h2database.com/html/features.html). As versões exatas da entrega estão fixadas em `pom.xml`.
