<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<div class="container">
    <header class="header-bar">
        <a href="${cpath}/index" aria-label="Home">
            <img src="${cpath}/img/heart.png" alt="">
        </a>

        <form method="post" action="${cpath}/lang">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
            <button class="btn-reset" type="submit" name="lang" value="ru" aria-label="Русский">
                <img src="${cpath}/img/ru.png" alt="">
            </button>
            <button class="btn-reset" type="submit" name="lang" value="en" aria-label="English">
                <img src="${cpath}/img/en.png" alt="">
            </button>
        </form>

        <c:choose>
            <c:when test="${pageContext.request.userPrincipal != null}">
                <form method="post" action="${cpath}/logout">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                    <button class="btn-reset" type="submit"
                            aria-label="${wordBundle.getWord('logout')}"
                            onclick="return confirm('Logout?');">
                        <img src="${cpath}/img/key.png" alt="">
                    </button>
                </form>
            </c:when>
            <c:otherwise>
                <a href="${cpath}/login" aria-label="${wordBundle.getWord('login')}">
                    <img src="${cpath}/img/key.png" alt="">
                </a>
            </c:otherwise>
        </c:choose>
    </header>
    <hr class="divider">
</div>