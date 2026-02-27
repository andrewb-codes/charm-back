<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<html lang="${empty cookie.lang ? 'en' : cookie.lang.value}">
<head>
    <title>Charm Forbidden</title>
    <%@ include file="../style.jsp" %>
</head>
<body>
<%@ include file="../header.jsp" %>
<div>
    <h3>403 - ${wordBundle.getWord("page-forbidden")}</h3>
</div>
<%@ include file="../footer.jsp" %>
</body>
</html>