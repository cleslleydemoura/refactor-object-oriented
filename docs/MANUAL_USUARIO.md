# Manual do usuário — Acervo

## 1. Instalar e iniciar

Instale o JDK 17 ou superior. Confirme em um terminal com `java -version` e `javac -version`. Extraia o pacote completo em uma pasta com permissão de escrita. Abra o PowerShell nessa pasta e execute:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\iniciar.ps1
```

Na primeira execução, aguarde os downloads e a compilação. Quando aparecer a mensagem de disponibilidade, abra `http://localhost:8080/itens`. Mantenha o terminal em execução. Para encerrar, pressione `Ctrl+C`. Para abrir novamente sem recompilar, utilize `iniciar.ps1 -SemCompilar` depois de uma compilação bem-sucedida.

O catálogo começa vazio. Os dados ficam em `data/catalogo.mv.db`. Não é necessário instalar um servidor de banco separado. Python não é necessário para utilizar a aplicação.

## 2. Cadastrar e consultar

Clique em **Adicionar item**. Escolha Livro, Filme ou Série e preencha título, autor/diretor, ano, gênero e sinopse. Todos os campos são obrigatórios. O ano deve ser inteiro, de 1 até o próximo ano do calendário. Os limites são: título e autor/diretor, 255 caracteres; gênero, 100; sinopse, 2.000.

Clique em **Adicionar ao acervo**. A página de detalhes confirma o cadastro. Caso haja um erro de preenchimento, os dados permanecem no formulário e a mensagem indica o que corrigir.

No catálogo, clique em **Ver detalhes** ou no título. A página apresenta os dados completos da obra. As cores e letras das capas são ilustrações tipográficas, não imagens oficiais das obras.

## 3. Buscar, editar e excluir

Digite parte do título ou do autor/diretor na caixa de busca e clique em **Buscar**. A pesquisa não diferencia maiúsculas de minúsculas. Acentos são significativos. Os símbolos `%` e `_` são pesquisados literalmente. Clique em **Limpar busca** para voltar à coleção completa.

Nos detalhes de uma obra, selecione **Editar informações**, ajuste os campos e clique em **Salvar alterações**. **Cancelar** retorna ao catálogo sem salvar as mudanças do formulário.

Para excluir, clique em **Excluir item** e confirme em **Sim, excluir este item**. A exclusão é permanente. Sem JavaScript, abra **Confirmar exclusão** diretamente; o formulário continua funcionando.

## 4. Dificuldades e conservação dos dados

Se a porta estiver ocupada, encerre o servidor, execute `$env:CATALOGO_PORT = '8081'`, reinicie e abra `http://localhost:8081/itens`.

Se o formulário expirar, reabra a tela e envie novamente. Se o banco ficar indisponível, confira o terminal e as permissões da pasta. Evite abrir duas instâncias com o mesmo arquivo H2.

Para backup, encerre o servidor e copie a pasta `data` para um local de sua escolha. Para restaurar, mantenha o servidor encerrado e substitua essa pasta por seu backup. Não copie o arquivo enquanto a aplicação estiver gravando.

O servidor de demonstração atende somente no computador local. A aplicação não possui contas de usuário. Nome e RGM são preenchidos no documento acadêmico, não dentro do catálogo.
