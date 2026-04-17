<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<html lang="${empty cookie.lang ? 'en' : cookie.lang.value}">
<head>
    <title>Charm Settings</title>
    <%@ include file="style.jsp" %>
</head>
<body>
<%@ include file="header.jsp" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<div class="container">
    <h2 class="center-text"><spring:message code="settings"/></h2>

    <div class="mt-3">
        <h3><spring:message code="change-email"/></h3>
        <form method="post" action="${cpath}/email">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
            <input type="hidden" name="_method" value="put"/>
            <input type="hidden" name="version" value="${emailChangeRequest.version}">
            <table class="table--form">
                <tr>
                    <td><h4><spring:message code="email"/></h4></td>
                    <td><input type="email" name="newEmail" value="${emailChangeRequest.newEmail}"></td>
                </tr>
                <tr>
                    <td><h4><spring:message code="current-password"/></h4></td>
                    <td><input type="password" name="currentPassword"></td>
                </tr>
            </table>
            <div class="row center mt-2" style="gap: var(--space-3);">
                <input type="image"
                       class="icon-lg"
                       src="${cpath}/img/floppy-disk.png"
                       alt="<spring:message code='save'/>" title="<spring:message code='save'/>"/>
            </div>
        </form>

        <c:if test="${not empty errors}">
            <div class="warning mt-1">
                <c:forEach var="error" items="${errors}">
                    <c:if test="${fn:startsWith(error, 'error.email.')}">
                        <p><spring:message code="${error}"/></p>
                    </c:if>
                </c:forEach>
            </div>
        </c:if>
    </div>

    <hr class="divider"/>

    <div class="mt-3">
        <h3><spring:message code="change-password"/></h3>
        <form method="post" action="${cpath}/password">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
            <input type="hidden" name="_method" value="put"/>
            <input type="hidden" name="version" value="${passwordChangeRequest.version}">
            <table class="table--form">
                <tr>
                    <td><h4><spring:message code="current-password"/></h4></td>
                    <td><input type="password" name="currentPassword"></td>
                </tr>
                <tr>
                    <td><h4><spring:message code="new-password"/></h4></td>
                    <td><input type="password" name="newPassword"></td>
                </tr>
                <tr>
                    <td><h4><spring:message code="confirm-password"/></h4></td>
                    <td><input type="password" name="confirmPassword"></td>
                </tr>
            </table>
            <div class="row center mt-2" style="gap: var(--space-3);">
                <input type="image"
                       class="icon-lg"
                       src="${cpath}/img/floppy-disk.png"
                       alt="<spring:message code='save'/>" title="<spring:message code='save'/>"/>
            </div>
        </form>

        <c:if test="${not empty errors}">
            <div class="warning mt-1">
                <c:forEach var="error" items="${errors}">
                    <c:if test="${fn:startsWith(error, 'error.password.')}">
                        <p><spring:message code="${error}"/></p>
                    </c:if>
                </c:forEach>
            </div>
        </c:if>
    </div>

    <c:if test="${!empty profileGetDto.id}">
        <form method="post" action="${cpath}/profile" style="display:inline;">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
            <input type="hidden" name="_method" value="delete">
            <button class="btn-reset" type="submit"
                    title="<spring:message code='delete'/>"
                    aria-label="<spring:message code='delete'/>"
                    onclick="return confirm('<spring:message code='delete-confirmation'/>?');">
                <img class="icon-sm" src="${cpath}/img/cross.png" alt="delete">
            </button>
        </form>
    </c:if>

</div>

<%@ include file="footer.jsp" %>
</body>
</html>
