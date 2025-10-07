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
            <form method="post" action="${pageContext.request.contextPath}/profile" enctype="multipart/form-data">
                <input type="hidden" name="_method" value="PUT">
                <input type="hidden" name="id" value="${profile.id}">
                <table>
                    <tr>
                        <td><h3>${wordBundle.getWord("email")}</h3></td>
                        <td><a href="${pageContext.request.contextPath}/email?id=${profile.id}">${profile.email}</a></td>
                    </tr>
                    <tr>
                        <td><h3>${wordBundle.getWord("name")}</h3></td>
                        <td><input type="text" name="name" required
                                   value="${(fields != null && fields['name'] != null) ? fields['name'] : profile.name}"></td>
                    </tr>
                    <tr>
                        <td><h3>${wordBundle.getWord("surname")}</h3></td>
                        <td><input type="text" name="surname" required
                                   value="${(fields != null && fields['surname'] != null) ? fields['surname'] : profile.surname}"></td>
                    </tr>
                    <tr>
                        <td><h3>${wordBundle.getWord("birthdate")}</h3></td>
                        <td><input type="date" name="birthDate" required
                                   value="${profile.birthDate}"></td>
                    </tr>
                    <c:if test="${!empty profile.birthDate}">
                        <tr>
                            <td><h3>${wordBundle.getWord("age")}</h3></td>
                            <td><h3>${profile.age}</h3></td>
                        </tr>
                    </c:if>
                    <tr>
                        <td><h3>${wordBundle.getWord("about")}</h3></td>
                        <td><input type="text" name="about" required
                                   value="${(fields != null && fields['about'] != null) ? fields['about'] : profile.about}"></td>
                    </tr>
                    <tr>
                        <td><h3>${wordBundle.getWord("gender")}</h3></td>
                        <td>
                            <c:set var="g" value="${profile.gender}"/>
                            <select name="gender" required>
                                <option value="" disabled <c:if test="${empty g}">selected</c:if>>
                                    ${wordBundle.getWord("select-gender")}
                                </option>
                                <c:forEach var="gender" items="${applicationScope.genders}">
                                    <option value="${gender}" <c:if test="${gender == g}">selected</c:if>>
                                        ${wordBundle.getWord(gender)}
                                    </option>
                                </c:forEach>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td><h3>${wordBundle.getWord("photo")}</h3></td>
                        <td>
                            <c:if test="${not empty profile.photo}">
                                <img src="${pageContext.request.contextPath}/content/profile/${profile.id}/${profile.photo}"
                                     alt="photo" height="300">
                            </c:if>
                            <br>
                            <input type="button" value="${wordBundle.getWord('update')}"
                                   onclick="document.getElementById('file').click();"/>
                            <input type="file" name="photo" id="file" accept="image/*" style="display:none;">
                        </td>
                    </tr>
                </table>
                <button type="submit">${wordBundle.getWord("save")}</button>
            </form>

            <c:if test="${!empty profile.id}">
                <form method="post" action="${pageContext.request.contextPath}/registration">
                    <input type="hidden" name="_method" value="delete">
                    <input type="hidden" name="id" value="${profile.id}">
                    <button type="submit">${wordBundle.getWord("delete")}</button>
                </form>
            </c:if>

            <c:if test="${not empty errors}">
                <div style="color:red">
                    <c:forEach var="error" items="${errors}">
                        <p>${wordBundle.getWord(error)}</p>
                    </c:forEach>
                </div>
            </c:if>
        </div>
        <%@ include file="footer.jsp" %>
    </body>
</html>
