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
            <table>
                <tr>
                    <td><h3>id</h3></td>
                    <td><h3>${requestScope.wordBundle.getWord("email")}</h3></td>
                    <td><h3>${requestScope.wordBundle.getWord("name")}</h3></td>
                    <td><h3>${requestScope.wordBundle.getWord("surname")}</h3></td>
                    <td><h3>${requestScope.wordBundle.getWord("age")}</h3></td>
                    <td><h3>${requestScope.wordBundle.getWord("status")}</h3></td>
                </tr>
                <c:forEach var="profile" items="${requestScope.profiles}">
                    <tr>
                        <td><h4>${profile.id}</h4></td>
                        <td><h4>${profile.email}</h4></td>
                        <td><h4>${profile.name}</h4></td>
                        <td><h4>${profile.surname}</h4></td>
                        <td><h4>${profile.age}</h4></td>
                        <td>
                            <form action="${pageContext.request.contextPath}/profile" method="post">
                                <input type="hidden" name="_method" value="put"/>
                                <input type="hidden" name="id" value="${profile.id}">
                                <c:set var="s" value="${profile.status}"/>
                                <select name="status">
                                    <option value="" disabled <c:if test="${empty s}">selected</c:if> hidden>
                                        ${requestScope.wordBundle.getWord(profile.status)}
                                    </option>
                                    <c:forEach var="status" items="${applicationScope.statuses}">
                                        <option value="${status}" <c:if test="${status == s}">selected</c:if>>
                                            ${requestScope.wordBundle.getWord(status)}
                                        </option>
                                    </c:forEach>
                                </select>
                                <button type="submit">${requestScope.wordBundle.getWord("save")}</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
            </table>
        </div>
        <%@ include file="footer.jsp" %>
    </body>
</html>