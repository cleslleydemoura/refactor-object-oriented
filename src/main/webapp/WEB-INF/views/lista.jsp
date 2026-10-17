<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="tags.jspf" %>
<c:set var="tituloPagina" value="Meu catálogo"/>
<%@ include file="cabecalho.jspf" %>
<section class="intro"><div><p class="eyebrow">SUA COLEÇÃO, DO SEU JEITO</p><h1>Uma boa história<br>merece ser <em>guardada.</em></h1><p class="subtitle">Organize os livros, filmes e séries que fazem parte do seu mundo.</p></div><div class="intro-mark" aria-hidden="true"><span>LER.</span><span>VER.</span><span>REVER.</span></div></section>
<section class="catalog-section" aria-labelledby="colecao-titulo">
  <div class="section-top"><div><p class="eyebrow">EXPLORE SEU ACERVO</p><h2 id="colecao-titulo">${empty termo ? 'Todos os itens' : 'Resultados da busca'} <span class="count">${fn:length(itens)}</span></h2></div>
    <form class="search" action="${pageContext.request.contextPath}/itens" method="get" role="search"><label class="sr-only" for="q">Buscar por título ou autor/diretor</label><input id="q" name="q" maxlength="255" placeholder="Título, autor ou diretor..." value="${fn:escapeXml(termo)}"><button type="submit">Buscar <span aria-hidden="true">↗</span></button></form>
  </div>
  <c:if test="${not empty termo}"><p class="search-summary">Buscando por “<c:out value="${termo}"/>”. <a href="${pageContext.request.contextPath}/itens">Limpar busca</a></p></c:if>
  <c:choose><c:when test="${empty itens}"><div class="empty-state"><span class="empty-icon" aria-hidden="true">＋</span><h3>${empty termo ? 'Sua próxima história começa aqui.' : 'Nenhum item encontrado.'}</h3><p>${empty termo ? 'Adicione seu primeiro livro, filme ou série ao catálogo.' : 'Tente outro título, autor ou diretor.'}</p><a class="button" href="${pageContext.request.contextPath}/itens/novo">Adicionar item</a></div></c:when>
  <c:otherwise><div class="card-grid"><c:forEach var="item" items="${itens}">
    <article class="media-card"><div class="card-art art-${item.tipoMidia}"><span class="art-label"><c:out value="${item.tipoMidia.descricao}"/></span><span class="art-letter" aria-hidden="true"><c:out value="${fn:substring(item.titulo, 0, 1)}"/></span><span class="art-year">${item.anoLancamento}</span></div><div class="card-body"><p class="card-genre"><c:out value="${item.genero}"/></p><h3><a href="${pageContext.request.contextPath}/itens/detalhes?id=${item.id}"><c:out value="${item.titulo}"/></a></h3><p class="card-author"><c:out value="${item.autorDiretor}"/></p><p class="card-synopsis"><c:out value="${item.sinopse}"/></p><a class="card-link" href="${pageContext.request.contextPath}/itens/detalhes?id=${item.id}">Ver detalhes <span aria-hidden="true">↗</span></a></div></article>
  </c:forEach></div></c:otherwise></c:choose>
</section>
<%@ include file="rodape.jspf" %>
