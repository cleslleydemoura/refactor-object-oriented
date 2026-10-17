<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="tags.jspf" %>
<c:set var="tituloPagina" value="Não foi possível concluir"/>
<%@ include file="cabecalho.jspf" %>
<section class="empty-state error-page"><p class="eyebrow">VAMOS TENTAR NOVAMENTE</p><h1>Algo saiu do roteiro.</h1><p><c:out value="${empty mensagemErro ? 'Não foi possível abrir esta página. Volte ao catálogo para continuar.' : mensagemErro}"/></p><a class="button" href="${pageContext.request.contextPath}/itens">Voltar ao catálogo</a></section>
<%@ include file="rodape.jspf" %>
