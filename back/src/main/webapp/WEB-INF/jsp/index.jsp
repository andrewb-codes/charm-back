<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<html lang="${empty cookie.lang ? 'en' : cookie.lang.value}">
<head>
    <title>Charm</title>
    <%@ include file="style.jsp" %>
</head>
<body>
<%@ include file="header.jsp" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<div class="container">
    <c:choose>
        <c:when test="${pageContext.request.userPrincipal == null}">
            <h1 class="center-text" style="margin-top: var(--space-3);">Charm</h1>
            <p class="center-text">${wordBundle.getWord('welcome')}! ${wordBundle.getWord('choose-option')}:</p>

            <div class="row center" style="gap: var(--space-3); margin-top: var(--space-2);">
                <a class="btn" href="${cpath}/registration" aria-label="${wordBundle.getWord('register')}">
                    ${wordBundle.getWord('register')}
                </a>
                <a class="btn btn--ghost" href="${cpath}/login" aria-label="${wordBundle.getWord('login')}">
                    ${wordBundle.getWord('login')}
                </a>
            </div>
        </c:when>

        <c:otherwise>
            <h2 class="center-text" style="margin-top: var(--space-3);">
                ${wordBundle.getWord('hello')}, ${pageContext.request.userPrincipal.name}!
            </h2>

            <div class="row center" style="gap: var(--space-3); margin-top: var(--space-2);">
                <a class="btn" href="${cpath}/profile">
                    ${wordBundle.getWord('go-to-profile')}
                </a>
                <a class="btn btn--ghost" href="${cpath}/settings">
                    ${wordBundle.getWord('settings')}
                </a>
                <a class="btn btn" href="${cpath}/charm">
                    ${wordBundle.getWord('charm')}
                </a>
                <a class="btn btn--ghost" href="${cpath}/matches">
                    ${wordBundle.getWord('matches')}
                </a>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="footer.jsp" %>
</body>
</html>