<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<html lang="${empty cookie.lang ? 'en' : cookie.lang.value}">
<head>
    <title>Charm Profiles</title>
    <%@ include file="style.jsp" %>
</head>
<body>
<%@ include file="header.jsp" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<div class="container">
    <h3 class="center-text" style="color: red">${wordBundle.getWord("email-warning")}</h3>

    <form method="post" action="${cpath}/email?id=${profile.id}">
        <input type="hidden" name="_method" value="put"/>

        <table class="table--form">
            <tr>
                <td><h3>${wordBundle.getWord("email")}</h3></td>
                <td>
                    <input type="email" name="email"
                           value="${(fields != null && fields['email'] != null) ? fields['email'] : profile.email}">
                </td>
            </tr>
        </table>

        <div class="row center" style="gap: var(--space-3); margin-top: var(--space-2);">
            <input type="image"
                   class="icon-lg"
                   src="${cpath}/content/app/img/floppy-disk.png"
                   alt="${wordBundle.getWord('save')}"
                   title="${wordBundle.getWord('save')}"/>
        </div>
    </form>

    <c:if test="${!empty profile.id}">
        <form method="post"
              action="${cpath}/registration"
              style="margin-top: var(--space-2);">
            <input type="hidden" name="_method" value="delete">
            <input type="hidden" name="id" value="${profile.id}">
            <div class="row center">
                <input type="image"
                       class="icon-lg"
                       src="${cpath}/content/app/img/cross.png"
                       alt="${wordBundle.getWord('delete')}"
                       title="${wordBundle.getWord('delete')}"
                       onclick="return confirm('Delete this profile?');"/>
            </div>
        </form>
    </c:if>

    <c:if test="${not empty errors}">
        <div style="color: red; margin-top: var(--space-2);">
            <c:forEach var="error" items="${errors}">
                <p>${wordBundle.getWord(error)}</p>
            </c:forEach>
        </div>
    </c:if>
</div>
<%@ include file="footer.jsp" %>
</body>
</html>