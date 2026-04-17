<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<html lang="${empty cookie.lang ? 'en' : cookie.lang.value}">
    <head>
      <title>Charm Profile</title>
      <%@ include file="style.jsp" %>
    </head>
    <body>
        <%@ include file="header.jsp" %>
        <c:set var="cpath" value="${pageContext.request.contextPath}"/>

        <div class="container">
            <form id="profile-form" method="post" action="${cpath}${profileAction}" enctype="multipart/form-data">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                <input type="hidden" name="_method" value="PUT">
                <input type="hidden" name="version" value="${profileUpdateRequest.version}">

                <table class="table--form">
                    <tr>
                        <td><h3><spring:message code="name"/></h3></td>
                        <td><input type="text" name="name" value="${profileUpdateRequest.name}"></td>
                    </tr>
                    <tr>
                        <td><h3><spring:message code="surname"/></h3></td>
                        <td><input type="text" name="surname" value="${profileUpdateRequest.surname}"></td>
                    </tr>
                    <tr>
                        <td><h3><spring:message code="birthdate"/></h3></td>
                        <td><input type="date" name="birthdate" value="${profileUpdateRequest.birthdate}"></td>
                    </tr>
                    <c:if test="${!empty profileGetDto.birthdate}">
                        <tr>
                            <td><h3><spring:message code="age"/></h3></td>
                            <td><h3>${profileGetDto.age}</h3></td>
                        </tr>
                    </c:if>
                    <tr>
                        <td><h3><spring:message code="about"/></h3></td>
                        <td><input type="text" name="about" value="${profileUpdateRequest.about}"></td>
                    </tr>
                    <tr>
                        <td><h3><spring:message code="gender"/></h3></td>
                        <td>
                            <c:set var="g" value="${profileUpdateRequest.gender}"/>
                            <select name="gender">
                                <option value="" disabled <c:if test="${empty g}">selected</c:if>>
                                    <spring:message code="select-gender"/>
                                </option>
                                <c:forEach var="gender" items="${genders}">
                                    <option value="${gender}" <c:if test="${gender == g}">selected</c:if>>
                                        <spring:message code="${gender}"/>
                                    </option>
                                </c:forEach>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td><h3><spring:message code="photo"/></h3></td>
                        <td>
                            <c:if test="${not empty profileGetDto.photo}">
                                <div class="photo-frame">
                                    <img class="photo" src="${cpath}/content/profile/${profileGetDto.id}/${profileGetDto.photo}" alt="photo">
                                </div>
                            </c:if>
                            <br>
                            <input type="button" value="<spring:message code='update'/>"
                                   onclick="document.getElementById('file').click();"/>
                            <input type="file" name="photo" id="file" accept="image/*" style="display:none;">
                        </td>
                    </tr>
                </table>
            </form>

            <!-- Icons panel (save, settings) -->
            <div class="row center mt-2" style="gap: var(--space-3);">
                <button class="btn-reset" type="submit" form="profile-form"
                        title="<spring:message code='save'/>" aria-label="<spring:message code='save'/>">
                    <img class="icon-lg" src="${cpath}/img/floppy-disk.png" alt="<spring:message code='save'/>">
                </button>

                <c:if test="${showSettingsLink}">
                    <a class="btn-reset"
                       href="${cpath}/settings"
                       title="<spring:message code='settings'/>" aria-label="<spring:message code='settings'/>">
                        <img class="icon-sm" src="${cpath}/img/settings.png" alt="@">
                    </a>
                </c:if>

                <a class="btn-reset"
                   href="${cpath}${profilePdfUrl}"
                   title="<spring:message code='pdf'/>" aria-label="<spring:message code='pdf'/>">
                    <img class="icon-sm" src="${cpath}/img/pdf-file.png" alt="pdf">
                </a>
            </div>

            <!-- Delete button -->
            <c:if test="${showDeleteButton}">
                <div class="row center mt-2">
                    <form method="post" action="${cpath}${deleteAction}" style="display:flex; align-items:center; margin:0;">
                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                        <input type="hidden" name="_method" value="delete">
                        <button class="btn-reset" type="submit"
                                title="<spring:message code='delete'/>"
                                aria-label="<spring:message code='delete'/>"
                                onclick="return confirm('<spring:message code='delete-confirmation'/>?');">
                            <img class="icon-lg" src="${cpath}/img/cross.png" alt="delete">
                        </button>
                    </form>
                </div>
            </c:if>

            <!-- Form errors -->
            <c:if test="${not empty errors}">
                <div style="color:red; margin-top: var(--space-2);">
                    <c:forEach var="error" items="${errors}">
                        <p><spring:message code="${error}"/></p>
                    </c:forEach>
                </div>
            </c:if>
        </div>
        <%@ include file="footer.jsp" %>
    </body>
</html>
