<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<html lang="${empty cookie.lang ? 'en' : cookie.lang.value}">
<head>
    <title>Charm</title>
    <%@ include file="style.jsp" %>
</head>
<body>
<%@ include file="header.jsp" %>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<div class="container">
    <h2 class="center-text" style="margin-top: var(--space-3);">
        <spring:message code="charm-empty.title"/>
    </h2>
    <p class="center-text" style="margin-top: var(--space-2);">
        <spring:message code="charm-empty.text"/>
    </p>

    <div class="row center" style="gap: var(--space-3); margin-top: var(--space-3);">
        <a class="btn" href="${cpath}/charm">
            <spring:message code="charm-empty.try-again"/>
        </a>
        <a class="btn btn--ghost" href="${cpath}/">
            <spring:message code="charm-empty.to-home"/>
        </a>
        <a class="btn btn--ghost" href="${cpath}/matches">
            <spring:message code="charm-empty.to-matches"/>
        </a>
    </div>
</div>

<%@ include file="footer.jsp" %>
</body>
</html>
