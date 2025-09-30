<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<html lang="${empty cookie.lang ? 'en' : cookie.lang.value}">
    <head>
      <title>Charm Profile</title>
      <%@ include file="style.jsp" %>
    </head>
    <body>
        <%@ include file="header.jsp" %>
        <div>
            <form method="post" action="${pageContext.request.contextPath}/profile">
                <input type="hidden" name="_method" value="PUT">
                <input type="hidden" name="id" value="${requestScope.profile.id}">
                <table>
                    <tr>
                        <td><h3>${requestScope.wordBundle.getWord("email")}</h3></td>
                        <td><a href="${pageContext.request.contextPath}/email?id=${requestScope.profile.id}">${requestScope.profile.email}</a></td>
                    </tr>
                    <tr>
                        <td><h3>${requestScope.wordBundle.getWord("name")}</h3></td>
                        <td><input type="text" name="name" required value="${requestScope.profile.name}"></td>
                    </tr>
                    <tr>
                        <td><h3>${requestScope.wordBundle.getWord("surname")}</h3></td>
                        <td><input type="text" name="surname" required value="${requestScope.profile.surname}"></td>
                    </tr>
                    <tr>
                        <td><h3>${requestScope.wordBundle.getWord("birthdate")}</h3></td>
                        <td><input type="date" name="birthDate" required value="${requestScope.profile.birthDate}"></td>
                    </tr>
                    <c:if test="${!empty requestScope.profile.birthDate}">
                        <tr>
                            <td><h3>${requestScope.wordBundle.getWord("age")}</h3></td>
                            <td><h3>${requestScope.profile.age}</h3></td>
                        </tr>
                    </c:if>
                    <tr>
                        <td><h3>${requestScope.wordBundle.getWord("about")}</h3></td>
                        <td><input type="text" name="about" required value="${requestScope.profile.about}"></td>
                    </tr>
                    <tr>
                        <td><h3>${requestScope.wordBundle.getWord("gender")}</h3></td>
                        <td>
                            <c:set var="g" value="${requestScope.profile.gender}"/>
                            <select name="gender" required>
                                <option value="" disabled <c:if test="${empty g}">selected</c:if>>
                                    ${requestScope.wordBundle.getWord("select-gender")}
                                </option>
                                <c:forEach var="gender" items="${applicationScope.genders}">
                                    <option value="${gender}" <c:if test="${gender == g}">selected</c:if>>
                                        ${requestScope.wordBundle.getWord(gender)}
                                    </option>
                                </c:forEach>
                            </select>
                        </td>
                    </tr>
                </table>
                <button type="submit">${requestScope.wordBundle.getWord("save")}</button>
            </form>
            <c:if test="${!empty requestScope.profile.id}">
                <form method="post" action="${pageContext.request.contextPath}/registration">
                    <input type="hidden" name="_method" value="delete">
                    <input type="hidden" name="id" value="${requestScope.profile.id}">
                    <button type="submit">${requestScope.wordBundle.getWord("delete")}</button>
                </form>
            </c:if>

        </div>
        <%@ include file="footer.jsp" %>
    </body>
</html>
