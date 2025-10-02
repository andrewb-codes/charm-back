<%@ page contentType="text/html;charset=UTF-8" %>
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
                        <td><h3>${requestScope.wordBundle.getWord("email")}</h3></td>
                        <td><input type="email" name="email" required placeholder="user@charm.ru"></td>
                    </tr>
                    <tr>
                        <td><h3>${requestScope.wordBundle.getWord("password")}</h3></td>
                        <td><input type="password" required name="password"></td>
                    </tr>
                </table>
                <button type="submit">${requestScope.wordBundle.getWord("save")}</button>
            </form>
        </div>
        <%@ include file="footer.jsp" %>
    </body>
</html>