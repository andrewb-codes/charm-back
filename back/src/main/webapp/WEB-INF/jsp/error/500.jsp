<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<html lang="${empty cookie.lang ? 'en' : cookie.lang.value}">
<head>
    <title>Charm Unexpected error</title>
    <%@ include file="../style.jsp" %>
</head>
<body>
<%@ include file="../header.jsp" %>
<div>
    <h3>500 - <spring:message code="unexpected-error"/></h3>

    <c:if test="${not empty errorMessage}">
        <p class="center-text" style="opacity:.8; margin-top: 12px;">
            <spring:message code="${errorMessage}"/>
        </p>
    </c:if>
</div>
<%@ include file="../footer.jsp" %>
</body>
</html>
