<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<html lang="${empty cookie.lang ? 'en' : cookie.lang.value}">
    <head>
        <title>Charm Bad Request</title>
        <%@ include file="style.jsp" %>
    </head>
    <body>
        <%@ include file="header.jsp" %>
        <div>
            <h3>400 - ${requestScope.wordBundle.getWord("page-bad-request")}</h3>
        </div>
        <%@ include file="footer.jsp" %>
    </body>
</html>