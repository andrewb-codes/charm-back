<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<html lang="${empty cookie.lang ? 'en' : cookie.lang.value}">
    <head>
      <title>Charm Profile</title>
      <%@ include file="style.jsp" %>
    </head>
    <body>
        <%@ include file="header.jsp" %>
        <c:set var="cpath" value="${pageContext.request.contextPath}"/>

        <div class="container">
            <form method="post" action="${cpath}/profile" enctype="multipart/form-data">
                <input type="hidden" name="_method" value="PUT">
                <input type="hidden" name="id" value="${profile.id}">
                <input type="hidden" name="version" value="${profile.version}">

                <table class="table--form">
                    <tr>
                        <td><h3>${wordBundle.getWord("name")}</h3></td>
                        <td><input type="text" name="name"
                                   value="${(fields != null && fields['name'] != null) ? fields['name'] : profile.name}"></td>
                    </tr>
                    <tr>
                        <td><h3>${wordBundle.getWord("surname")}</h3></td>
                        <td><input type="text" name="surname"
                                   value="${(fields != null && fields['surname'] != null) ? fields['surname'] : profile.surname}"></td>
                    </tr>
                    <tr>
                        <td><h3>${wordBundle.getWord("birthdate")}</h3></td>
                        <td><input type="date" name="birthdate"
                                   value="${profile.birthdate}"></td>
                    </tr>
                    <c:if test="${!empty profile.birthdate}">
                        <tr>
                            <td><h3>${wordBundle.getWord("age")}</h3></td>
                            <td><h3>${profile.age}</h3></td>
                        </tr>
                    </c:if>
                    <tr>
                        <td><h3>${wordBundle.getWord("about")}</h3></td>
                        <td><input type="text" name="about"
                                   value="${(fields != null && fields['about'] != null) ? fields['about'] : profile.about}"></td>
                    </tr>
                    <tr>
                        <td><h3>${wordBundle.getWord("gender")}</h3></td>
                        <td>
                            <c:set var="g" value="${profile.gender}"/>
                            <select name="gender">
                                <option value="" disabled <c:if test="${empty g}">selected</c:if>>
                                    ${wordBundle.getWord("select-gender")}
                                </option>
                                <c:forEach var="gender" items="${genders}">
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
                                <img class="photo" src="${cpath}/content/profile/${profile.id}/${profile.photo}" alt="photo">
                            </c:if>
                            <br>
                            <input type="button" value="${wordBundle.getWord('update')}"
                                   onclick="document.getElementById('file').click();"/>
                            <input type="file" name="photo" id="file" accept="image/*" style="display:none;">
                        </td>
                    </tr>
                </table>

                <!-- Icons panel (save, settings) -->
                <div class="row center mt-2" style="gap: var(--space-3);">
                    <input type="image" class="icon-lg"
                           src="${cpath}/img/floppy-disk.png"
                           alt="${wordBundle.getWord('save')}" title="${wordBundle.getWord('save')}"/>

                    <a class="btn-reset"
                       href="${cpath}/settings"
                       title="${wordBundle.getWord('settings')}" aria-label="${wordBundle.getWord('settings')}">
                        <img class="icon-sm" src="${cpath}/img/settings.png" alt="@">
                    </a>

                    <a class="btn-reset"
                       href="${cpath}/profile/pdf"
                       title="${wordBundle.getWord('pdf')}" aria-label="${wordBundle.getWord('pdf')}">
                        <img class="icon-sm" src="${cpath}/img/pdf-file.png" alt="pdf">
                    </a>
                </div>
            </form>

            <!-- Form errors -->
            <c:if test="${not empty errors}">
                <div style="color:red; margin-top: var(--space-2);">
                    <c:forEach var="error" items="${errors}">
                        <p>${wordBundle.getWord(error)}</p>
                    </c:forEach>
                </div>
            </c:if>
        </div>
        <%@ include file="footer.jsp" %>
    </body>
</html>
