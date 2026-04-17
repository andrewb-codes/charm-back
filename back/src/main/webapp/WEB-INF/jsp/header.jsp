<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="currentUri"
       value="${requestScope['jakarta.servlet.forward.request_uri'] != null
           ? requestScope['jakarta.servlet.forward.request_uri']
           : pageContext.request.requestURI}" />

<c:url var="langRuUrl" value="${currentUri}">
    <c:forEach var="entry" items="${paramValues}">
        <c:if test="${entry.key ne 'lang'}">
            <c:forEach var="value" items="${entry.value}">
                <c:param name="${entry.key}" value="${value}" />
            </c:forEach>
        </c:if>
    </c:forEach>
    <c:param name="lang" value="ru" />
</c:url>

<c:url var="langEnUrl" value="${currentUri}">
    <c:forEach var="entry" items="${paramValues}">
        <c:if test="${entry.key ne 'lang'}">
            <c:forEach var="value" items="${entry.value}">
                <c:param name="${entry.key}" value="${value}" />
            </c:forEach>
        </c:if>
    </c:forEach>
    <c:param name="lang" value="en" />
</c:url>

<div class="container">
    <header class="header-bar">
        <a href="${cpath}/index" aria-label="<spring:message code='home'/>">
            <img src="${cpath}/img/heart.png" alt="<spring:message code='home'/>">
        </a>

        <div>
            <a class="btn-reset" href="${langRuUrl}" aria-label="<spring:message code='lang.ru'/>">
                <img src="${cpath}/img/ru.png" alt="<spring:message code='lang.ru'/>">
            </a>
            <a class="btn-reset" href="${langEnUrl}" aria-label="<spring:message code='lang.en'/>">
                <img src="${cpath}/img/en.png" alt="<spring:message code='lang.en'/>">
            </a>
        </div>

        <c:choose>
            <c:when test="${pageContext.request.userPrincipal != null}">
                <form method="post" action="${cpath}/logout">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                    <button class="btn-reset" type="submit"
                            aria-label="<spring:message code='logout'/>"
                            onclick="return confirm('<spring:message code='logout-confirm'/>');">
                        <img src="${cpath}/img/key.png" alt="<spring:message code='logout'/>">
                    </button>
                </form>
            </c:when>
            <c:otherwise>
                <a href="${cpath}/login" aria-label="<spring:message code='login'/>">
                    <img src="${cpath}/img/key.png" alt="<spring:message code='login'/>">
                </a>
            </c:otherwise>
        </c:choose>
    </header>
    <hr class="divider">
</div>