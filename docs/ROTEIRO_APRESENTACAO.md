# Roteiro de apresentação — aproximadamente 7 minutos

## 1. Problema e objetivo — 1 minuto

Escolhi a Situação-Problema 3, sobre refatoração e orientação a objetos. O enunciado apresenta um catálogo de livros e filmes com cadastro, consulta, edição, exclusão e busca concentrados em uma Servlet, junto com as regras e o SQL. Essa organização dificulta a manutenção, porque mudanças de naturezas diferentes acabam no mesmo componente.

Meu objetivo foi reorganizar o catálogo em camadas, mantendo as funcionalidades e facilitando a reutilização e os testes. Desenvolvi a solução com o nome Acervo, incluindo livros, filmes e séries. Realizei o trabalho ao longo de dez dias, passando pela análise, modelagem, implementação, testes e documentação.

## 2. Arquitetura e POO — 1 minuto

No diagrama de classes, o fluxo segue das páginas JSP para a Servlet, depois para o serviço, a interface DAO e a implementação JDBC, que acessa o H2. Cada parte tem uma responsabilidade: a entidade representa a obra; o serviço valida; o DAO cuida do banco; e a Servlet organiza as requisições e as respostas.

O encapsulamento aparece nos atributos privados de ItemMidia. A abstração aparece no contrato ItemMidiaDao. O polimorfismo permite usar JDBC na aplicação e um DAO em memória nos testes. A herança aparece na Servlet, que estende HttpServlet.

## 3. Demonstração funcional — 2 minutos

Na demonstração, vou abrir o catálogo e cadastrar uma obra com título, autor ou diretor, tipo, ano, gênero e sinopse. Depois, vou localizar o item pela busca e abrir seus detalhes.

Em seguida, vou editar o gênero, salvar e conferir a alteração. Por fim, vou excluir o item criado para a demonstração, utilizando a confirmação da tela. Essas operações mostram o CRUD e a busca integrados à interface e ao banco de dados.

## 4. Código e testes — 2 minutos

No controlador, vou mostrar a chamada servico.salvar(...). A Servlet recebe os dados e delega a operação. No serviço, vou apresentar as validações feitas antes da gravação. No DAO, vou mostrar o SQL parametrizado com PreparedStatement e o fechamento dos recursos com try-with-resources.

Os registros da entrega apresentam 39 testes aprovados: 23 de serviço, 8 de persistência e 8 de integração HTTP, sem falhas, erros ou testes ignorados. Os testes de serviço usam um DAO em memória. Os testes JDBC verificam o banco, incluindo a recuperação dos dados após a reabertura do arquivo. Os testes HTTP exercitam os fluxos com Tomcat e as páginas JSP.

## 5. Resultado e continuidade — 1 minuto

Ao final do trabalho, concluí o catálogo com as operações previstas e as responsabilidades distribuídas entre as camadas. A principal contribuição da POO foi permitir que cada parte do código tivesse uma função definida. O DAO reúne o acesso aos dados, e o serviço permite verificar as regras sem iniciar a aplicação web.

O trabalho também consolidou a relação entre os conceitos estudados e uma aplicação em funcionamento. Como próximos passos, considero paginação, autenticação, histórico de alterações e controle de edição simultânea.

## Pontos para revisão

- As regras de validação ficam no serviço; o DAO executa a persistência.
- H2 é um banco relacional e conserva os registros em arquivo na execução normal.
- O DAO em memória é usado apenas nos testes de serviço.
- A avaliação da manutenção se apoia na separação das responsabilidades e na possibilidade de testar as camadas isoladamente.
- O período de execução do trabalho foi de dez dias.
