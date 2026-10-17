<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="tags.jspf" %>
<c:set var="tituloPagina" value="${empty id ? 'Adicionar item' : 'Editar item'}"/>
<%@ include file="cabecalho.jspf" %>
<a class="back-link" href="${pageContext.request.contextPath}/itens">← Voltar ao catálogo</a>
<div class="page-heading"><p class="eyebrow">CUIDE DA SUA COLEÇÃO</p><h1>${empty id ? 'Uma nova história.' : 'Cada detalhe importa.'}</h1><p class="subtitle">${empty id ? 'Preencha os dados para adicionar um item ao seu acervo.' : 'Atualize as informações desta obra.'} Todos os campos são obrigatórios.</p></div>
<c:if test="${not empty erros}"><div class="notice error" role="alert"><strong>Revise os campos indicados.</strong><ul><c:forEach var="erro" items="${erros}"><li><c:out value="${erro.value}"/></li></c:forEach></ul></div></c:if>
<form class="edit-form" action="${pageContext.request.contextPath}/itens/salvar" method="post">
  <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
  <c:if test="${not empty id}"><input type="hidden" name="id" value="${id}"></c:if>
  <div class="form-grid">
    <div class="field"><label for="tipoMidia">Tipo de mídia</label><select id="tipoMidia" name="tipoMidia" required aria-describedby="erro-tipoMidia"><c:forEach var="tipo" items="${tipos}"><option value="${tipo}" ${form.tipoMidia == tipo.name() ? 'selected' : ''}><c:out value="${tipo.descricao}"/></option></c:forEach></select><small id="erro-tipoMidia" class="field-error"><c:out value="${erros.tipoMidia}"/></small></div>
    <div class="field"><label for="anoLancamento">Ano de publicação ou lançamento</label><input id="anoLancamento" name="anoLancamento" type="number" min="1" max="${anoMaximo}" required value="${fn:escapeXml(form.anoLancamento)}" aria-describedby="erro-anoLancamento"><small id="erro-anoLancamento" class="field-error"><c:out value="${erros.anoLancamento}"/></small></div>
    <div class="field field-wide"><label for="titulo">Título</label><input id="titulo" name="titulo" maxlength="255" required placeholder="Como se chama esta obra?" value="${fn:escapeXml(form.titulo)}" aria-describedby="erro-titulo"><small id="erro-titulo" class="field-error"><c:out value="${erros.titulo}"/></small></div>
    <div class="field"><label for="autorDiretor">Autor ou diretor</label><input id="autorDiretor" name="autorDiretor" maxlength="255" required value="${fn:escapeXml(form.autorDiretor)}" aria-describedby="erro-autorDiretor"><small id="erro-autorDiretor" class="field-error"><c:out value="${erros.autorDiretor}"/></small></div>
    <div class="field"><label for="genero">Gênero</label><input id="genero" name="genero" maxlength="100" required placeholder="Ex.: Ficção científica" value="${fn:escapeXml(form.genero)}" aria-describedby="erro-genero"><small id="erro-genero" class="field-error"><c:out value="${erros.genero}"/></small></div>
    <div class="field field-wide"><label for="sinopse">Sinopse</label><textarea id="sinopse" name="sinopse" rows="6" maxlength="2000" required aria-describedby="sinopse-ajuda erro-sinopse"><c:out value="${form.sinopse}"/></textarea><div class="field-help"><span id="sinopse-ajuda">Um breve resumo, com até 2.000 caracteres.</span><span id="sinopse-contador" aria-live="polite"></span></div><small id="erro-sinopse" class="field-error"><c:out value="${erros.sinopse}"/></small></div>
  </div><div class="form-actions"><button class="button" type="submit">${empty id ? 'Adicionar ao acervo' : 'Salvar alterações'} <span aria-hidden="true">↗</span></button><a class="button button-ghost" href="${pageContext.request.contextPath}/itens">Cancelar</a></div>
</form>
<%@ include file="rodape.jspf" %>
