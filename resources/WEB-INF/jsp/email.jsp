<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<html lang="${empty cookie.lang ? 'en' : cookie.lang.value}">
<head>
    <title>Charm Profiles</title>
    <%@ include file="style.jsp" %>
</head>
<body>
<%@ include file="header.jsp" %>
<div>
    <h3 style="color: red">${wordBundle.getWord("email-warning")}</h3>
    <form method="post" action="${pageContext.request.contextPath}/email?id=${profile.id}">
        <input type="hidden" name="_method" value="put"/>
        <table>
            <tr>
                <td><h3>${wordBundle.getWord("email")}</h3></td>
                <td>
                    <input type="email" name="email"
                           value="${(fields != null && fields['email'] != null) ? fields['email'] : profile.email}">
                </td>
            </tr>
        </table>
        <button type="submit">${wordBundle.getWord("save")}</button>
    </form>

    <c:if test="${not empty errors}">
        <div style="color: red">
            <c:forEach var="error" items="${errors}">
                <p>${wordBundle.getWord(error)}</p>
            </c:forEach>
        </div>
    </c:if>
</div>
<%@ include file="footer.jsp" %>
</body>
</html>