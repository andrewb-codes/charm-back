<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<html lang="${empty cookie.lang ? 'en' : cookie.lang.value}">
<head>
    <title>Charm Registration</title>
    <%@ include file="style.jsp" %>
</head>
<body>
<%@ include file="header.jsp" %>
<div>
    <form method="post" action="${pageContext.request.contextPath}/registration">
        <table>
            <tr>
                <td><h3>${wordBundle.getWord("email")}</h3></td>
                <td><input type="email" name="email"
                           required placeholder="user@charm.ru"
                           value="${(fields != null && fields['email'] != null) ? fields['email'] : ''}">
                </td>
            </tr>
            <tr>
                <td><h3>${wordBundle.getWord("password")}</h3></td>
                <td><input type="password" required name="password"></td>
            </tr>
        </table>
        <button type="submit">${wordBundle.getWord("save")}</button>
    </form>
</div>

<c:if test="${not empty errors}">
    <div style="color: red">
        <c:forEach var="error" items="${errors}">
            <p>${wordBundle.getWord(error)}</p>
        </c:forEach>
    </div>
</c:if>
<%@ include file="footer.jsp" %>
</body>
</html>