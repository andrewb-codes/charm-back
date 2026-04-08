<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<html lang="${empty cookie.lang ? 'en' : cookie.lang.value}">
<head>
    <title>Charm Matches</title>
    <%@ include file="style.jsp" %>
</head>
<body>
<%@ include file="header.jsp" %>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<div class="container">
    <form method="get" action="${cpath}/matches" class="filters">
        <input type="hidden" name="page" value="1"/>

        <div class="field">
            <label for="f-pageSize">${wordBundle.getWord('pageSize')}</label>
            <select id="f-pageSize" name="pageSize" style="width:88px;">
                <c:forEach var="size" items="${availablePageSizes}">
                    <option value="${size}" <c:if test="${size == filter.pageSize}">selected</c:if>>${size}</option>
                </c:forEach>
            </select>
        </div>

        <div class="actions">
            <button type="submit" class="btn-reset" title="${wordBundle.getWord('update')}" aria-label="Apply">
                <img class="icon-lg" src="${cpath}/img/filter.png" alt="">
            </button>
            <a class="btn-reset" href="${cpath}/matches" title="Reset" aria-label="Reset">
                <img class="icon-lg" src="${cpath}/img/cross.png" alt="x">
            </a>
        </div>
    </form>

    <c:choose>
        <c:when test="${empty matches}">
            <p style="opacity:.7;margin:12px 0;">
                ${wordBundle.getWord('no-matches-yet')}
            </p>
        </c:when>

        <c:otherwise>
            <table class="table--list">
                <colgroup>
                    <col style="width:20%">     <!-- email -->
                    <col style="width:14%">     <!-- name -->
                    <col style="width:14%">     <!-- surname -->
                    <col style="width:7ch">     <!-- age -->
                    <col style="width:12%">     <!-- photo -->
                    <col>                       <!-- about -->
                </colgroup>

                <tr>
                    <td><h3>${wordBundle.getWord('email')}</h3></td>
                    <td><h3>${wordBundle.getWord('name')}</h3></td>
                    <td><h3>${wordBundle.getWord('surname')}</h3></td>
                    <td><h3>${wordBundle.getWord('age')}</h3></td>
                    <td><h3>${wordBundle.getWord('photo')}</h3></td>
                    <td><h3>${wordBundle.getWord('about')}</h3></td>
                </tr>

                <c:forEach var="m" items="${matches}">
                    <tr>
                        <td><h4><c:out value="${m.email}"/></h4></td>
                        <td><h4><c:out value="${m.name}"/></h4></td>
                        <td><h4><c:out value="${m.surname}"/></h4></td>
                        <td><h4><c:out value="${m.age}"/></h4></td>
                        <td>
                            <c:if test="${not empty m.photo}">
                                <details>
                                    <summary>${wordBundle.getWord('show')}</summary>
                                    <img src="${cpath}/content/profile/${m.id}/${m.photo}" style="max-height:250px;max-width:100%;object-fit:cover;border-radius:8px;">
                                </details>
                            </c:if>
                        </td>
                        <td>
                            <details>
                                <summary>${wordBundle.getWord('show')}</summary>
                                <textarea cols="100" rows="5" wrap="soft" disabled
                                          style="width:100%;resize:vertical;"><c:out value="${m.about}"/></textarea>
                            </details>
                        </td>
                    </tr>
                </c:forEach>
            </table>

            <!-- Go to page -->
            <form method="get" action="${cpath}/matches" class="row" style="gap:12px; align-items:center; margin:12px 0;">
                <input type="hidden" name="pageSize" value="${filter.pageSize}">
                <label for="go-page">${wordBundle.getWord('page')}</label>
                <input id="go-page" type="number" name="page" min="1" value="${filter.page}" style="width:72px;">
                <button type="submit" class="btn-reset">${wordBundle.getWord('update')}</button>
            </form>

            <!-- Pager -->
            <c:url var="prevUrl" value="${cpath}/matches">
                <c:param name="page" value="${filter.page - 1}"/>
                <c:param name="pageSize" value="${filter.pageSize}"/>
            </c:url>
            <c:url var="nextUrl" value="${cpath}/matches">
                <c:param name="page" value="${filter.page + 1}"/>
                <c:param name="pageSize" value="${filter.pageSize}"/>
            </c:url>

            <div class="row" style="gap:12px; align-items:center;margin-top:12px;">
                <c:choose>
                    <c:when test="${hasPrev}">
                        <a class="btn-reset" href="${prevUrl}" title="Prev" aria-label="Prev">
                            ← ${wordBundle.getWord('prev')}
                        </a>
                    </c:when>
                    <c:otherwise>
                        <button class="btn-reset" disabled>← ${wordBundle.getWord('prev')}</button>
                    </c:otherwise>
                </c:choose>

                <span>${wordBundle.getWord('page')}: <b>${filter.page}</b></span>

                <c:choose>
                    <c:when test="${hasNext}">
                        <a class="btn-reset" href="${nextUrl}" title="Next" aria-label="Next">
                            ${wordBundle.getWord('next')} →
                        </a>
                    </c:when>
                    <c:otherwise>
                        <button class="btn-reset" disabled>${wordBundle.getWord('next')} →</button>
                    </c:otherwise>
                </c:choose>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="footer.jsp" %>
</body>
</html>