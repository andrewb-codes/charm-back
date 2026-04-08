<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<html lang="${empty cookie.lang ? 'en' : cookie.lang.value}">
<head>
    <title>Charm Registration</title>
    <%@ include file="style.jsp" %>
</head>
<body>
<%@ include file="header.jsp" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<div class="container">
    <form method="post" action="${cpath}/registration">
        <table class="table--form">
            <tr>
                <td><h3>${wordBundle.getWord("email")}</h3></td>
                <td><input type="email" name="email" required placeholder="user@charm.ru" value="${registrationDto.email}"></td>
            </tr>
            <tr>
                <td><h3>${wordBundle.getWord("password")}</h3></td>
                <td><input type="password" required name="password"></td>
            </tr>
        </table>

        <div class="row center mt-2" style="gap: var(--space-3);">
            <input type="image" class="icon-lg"
                   src="${cpath}/img/floppy-disk.png"
                   alt="${wordBundle.getWord('save')}" title="${wordBundle.getWord('save')}"/>
        </div>
    </form>

    <c:if test="${not empty errors}">
        <div class="center-text mt-2" style="color: red;">
            <c:forEach var="error" items="${errors}">
                <p>${wordBundle.getWord(error)}</p>
            </c:forEach>
        </div>
    </c:if>
</div>

<%@ include file="footer.jsp" %>
</body>
</html>