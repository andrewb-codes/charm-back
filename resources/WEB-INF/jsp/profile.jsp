<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<html lang="en">
    <head>
      <title>Charm Profile</title>
    </head>
    <body>
    <%@ include file="header.html" %>
        <div>
            <form method="post" action="${pageContext.request.contextPath}/profile">
                <c:if test="${!empty requestScope.profile.id}">
                    <input type="hidden" name="_method" value="PUT">
                </c:if>
                <input type="hidden" name="id" value="${requestScope.profile.id}">
                <table>
                    <tr>
                        <td><h3>Email</h3></td>
                        <td><input type="email" name="email" value="${requestScope.profile.email}"></td>
                    </tr>
                    <tr>
                        <td><h3>Name</h3></td>
                        <td><input type="text" name="name" value="${requestScope.profile.name}"></td>
                    </tr>
                    <tr>
                        <td><h3>Surname</h3></td>
                        <td><input type="text" name="surname" value="${requestScope.profile.surname}"></td>
                    </tr>
                    <tr>
                        <td><h3>About</h3></td>
                        <td><input type="text" name="about" value="${requestScope.profile.about}"></td>
                    </tr>
                    <tr>
                        <td><h3>Gender</h3></td>
                        <td>
                            <c:set var="g" value="${requestScope.profile.gender}"/>
                            <select name="gender" required>
                                <option value="" disabled <c:if test="${empty g}">selected</c:if>>Select gender</option>
                                <c:forEach var="gender" items="${applicationScope.genders}">
                                    <option value="${gender}" <c:if test="${gender == g}">selected</c:if>>${gender}</option>
                                </c:forEach>
                            </select>
                        </td>
                    </tr>
                </table>
                <button type="submit">Save</button>
            </form>
            <c:if test="${!empty requestScope.profile.id}">
                <form method="post" action="${pageContext.request.contextPath}/profile">
                    <input type="hidden" name="_method" value="DELETE">
                    <input type="hidden" name="id" value="${requestScope.profile.id}">
                    <button type="submit">Delete</button>
                </form>
            </c:if>

        </div>
        <%@ include file="footer.html" %>
    </body>
</html>
