<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<html lang="${empty cookie.lang ? 'en' : cookie.lang.value}">
<head>
    <title>Charm Settings</title>
    <%@ include file="style.jsp" %>
</head>
<body>
<%@ include file="header.jsp" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<div class="container">
    <h2 class="center-text">${wordBundle.getWord("settings")}</h2>

    <!-- E-mail change -->
    <div class="mt-3">
        <h3>${wordBundle.getWord("change-email")}</h3>
        <form method="post" action="${cpath}/email">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
            <input type="hidden" name="_method" value="put"/>
            <input type="hidden" name="version" value="${emailChangeDto.version}">
            <table class="table--form">
                <tr>
                    <td><h4>${wordBundle.getWord("email")}</h4></td>
                    <td><input type="email" name="newEmail" value="${emailChangeDto.newEmail}"></td>
                </tr>
                <tr>
                    <td><h4>${wordBundle.getWord("current-password")}</h4></td>
                    <td><input type="password" name="currentPassword"></td>
                </tr>
            </table>
            <div class="row center mt-2" style="gap: var(--space-3);">
                <input type="image"
                       class="icon-lg"
                       src="${cpath}/img/floppy-disk.png"
                       alt="${wordBundle.getWord('save')}" title="${wordBundle.getWord('save')}"/>
            </div>
        </form>

        <!-- E-mail errors -->
        <c:if test="${not empty errors}">
            <div class="warning mt-1">
                <c:forEach var="error" items="${errors}">
                    <c:if test="${fn:startsWith(error, 'error.email.')}">
                        <p>${wordBundle.getWord(error)}</p>
                    </c:if>
                </c:forEach>
            </div>
        </c:if>
    </div>

    <hr class="divider"/>

    <!-- Password change -->
    <div class="mt-3">
        <h3>${wordBundle.getWord("change-password")}</h3>
        <form method="post" action="${cpath}/password">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
            <input type="hidden" name="_method" value="put"/>
            <input type="hidden" name="version" value="${passwordChangeDto.version}">
            <table class="table--form">
                <tr>
                    <td><h4>${wordBundle.getWord("current-password")}</h4></td>
                    <td><input type="password" name="currentPassword"></td>
                </tr>
                <tr>
                    <td><h4>${wordBundle.getWord("new-password")}</h4></td>
                    <td><input type="password" name="newPassword"></td>
                </tr>
                <tr>
                    <td><h4>${wordBundle.getWord("confirm-password")}</h4></td>
                    <td><input type="password" name="confirmPassword"></td>
                </tr>
            </table>
            <div class="row center mt-2" style="gap: var(--space-3);">
                <input type="image"
                       class="icon-lg"
                       src="${cpath}/img/floppy-disk.png"
                       alt="${wordBundle.getWord('save')}" title="${wordBundle.getWord('save')}"/>
            </div>
        </form>

        <!-- Password errors -->
        <c:if test="${not empty errors}">
            <div class="warning mt-1">
                <c:forEach var="error" items="${errors}">
                    <c:if test="${fn:startsWith(error, 'error.password.')}">
                        <p>${wordBundle.getWord(error)}</p>
                    </c:if>
                </c:forEach>
            </div>
        </c:if>
    </div>

    <!-- Delete profile -->
    <c:if test="${!empty profileGetDto.id}">
        <form method="post" action="${cpath}/profile" style="display:inline;">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
            <input type="hidden" name="_method" value="delete">
            <button class="btn-reset" type="submit"
                    title="${wordBundle.getWord('delete')}"
                    aria-label="${wordBundle.getWord('delete')}"
                    onclick="return confirm('${wordBundle.getWord('delete-confirmation')}?');">
                <img class="icon-sm" src="${cpath}/img/cross.png" alt="delete">
            </button>
        </form>
    </c:if>

</div>

<%@ include file="footer.jsp" %>
</body>
</html>