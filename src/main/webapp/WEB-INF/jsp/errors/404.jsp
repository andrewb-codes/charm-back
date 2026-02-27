<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<html lang="${empty cookie.lang ? 'en' : cookie.lang.value}">
<head>
    <title>Charm Not Found</title>
    <%@ include file="../style.jsp" %>
</head>
<body>
<%@ include file="../header.jsp" %>
<div>
    <h3>404 - ${wordBundle.getWord("page-not-found")}</h3>
</div>
<%@ include file="../footer.jsp" %>
</body>
</html>