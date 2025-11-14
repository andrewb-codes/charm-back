<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<html lang="${empty cookie.lang ? 'en' : cookie.lang.value}">
<head>
    <title>Charm</title>
    <%@ include file="style.jsp" %>
</head>
<body>
<%@ include file="header.jsp" %>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<div class="container" style="max-width:920px;margin-inline:auto;">
    <form method="post" action="${cpath}/charm" style="display:flex;flex-direction:column;gap:16px;">
        <input type="hidden" name="toProfile" value="${next.id}"/>

        <h3 style="margin:0;">
            ${wordBundle.getWord("how-about")}
            <c:out value="${next.name}"/>
            <c:out value="${next.surname}"/>
            <c:if test="${not empty next.age}">, ${next.age}</c:if>
            ?
        </h3>

        <div style="display:flex;gap:20px;align-items:flex-start;flex-wrap:wrap;">
            <div>
                <c:choose>
                    <c:when test="${not empty next.photo}">
                        <img src="${cpath}/content${next.photo}" alt="Profile photo"
                             style="max-height:500px;max-width:100%;object-fit:cover;border-radius:12px;">
                    </c:when>
                    <c:otherwise>
                        <img src="${cpath}/content/app/img/empty_profile.png" alt="No photo"
                             style="max-height:500px;max-width:100%;object-fit:cover;border-radius:12px;">
                    </c:otherwise>
                </c:choose>
            </div>

            <div style="flex:1;min-width:260px;">
                <p style="white-space:pre-wrap;line-height:1.5;margin-top:0;">
                    <c:out value="${next.about}"/>
                </p>
            </div>
        </div>

        <div style="display:flex;gap:24px;justify-content:center;margin-top:8px;">
            <button type="submit" name="action" value="LIKE" class="hiddenButton" title="Like" aria-label="Like">
                <img src="${cpath}/content/app/img/thumb-up.png" width="75" class="icon" alt="Like">
            </button>
            <button type="submit" name="action" value="SKIP" class="hiddenButton" title="Skip" aria-label="Skip">
                <img src="${cpath}/content/app/img/arrow-right.png" width="75" class="icon" alt="Skip">
            </button>
            <button type="submit" name="action" value="DISLIKE" class="hiddenButton" title="Dislike"
                    aria-label="Dislike">
                <img src="${cpath}/content/app/img/thumb-down.png" width="75" class="icon" alt="Dislike">
            </button>
        </div>
    </form>
</div>

<%@ include file="footer.jsp" %>
</body>
</html>