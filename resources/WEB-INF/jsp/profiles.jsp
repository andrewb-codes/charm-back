<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<html lang="${empty cookie.lang ? 'en' : cookie.lang.value}">
<head>
    <title>Charm Profiles</title>
    <%@ include file="style.jsp" %>
</head>
<body>
<%@ include file="header.jsp" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<div class="container">
    <!-- Filtration -->
    <form method="get" action="${cpath}/profiles" class="filters">
        <input type="hidden" name="sortBy" value="${filter.sortBy}">
        <input type="hidden" name="sortOrder"  value="${filter.sortOrder}">
        <input type="hidden" name="page" value="1">


        <div class="field">
            <label for="f-email">${wordBundle.getWord("email")}</label>
            <input id="f-email" type="text" name="emailStartsWith"
                   value="${filter != null ? filter.emailStartsWith : ''}">
        </div>

        <div class="field">
            <label for="f-name">${wordBundle.getWord("name")}</label>
            <input id="f-name" type="text" name="nameStartsWith"
                   value="${filter != null ? filter.nameStartsWith : ''}">
        </div>

        <div class="field">
            <label for="f-surname">${wordBundle.getWord("surname")}</label>
            <input id="f-surname" type="text" name="surnameStartsWith"
                   value="${filter != null ? filter.surnameStartsWith : ''}">
        </div>

        <div class="field field--age">
            <label for="f-gteAge">${wordBundle.getWord("age")}</label>
            <input id="f-gteAge" type="number" name="gteAge" min="18" max="120"
                   value="${filter != null && filter.greaterAndEqualAgeBound != null ? filter.greaterAndEqualAgeBound : ''}"
                   style="width:72px;">
            <span>–</span>
            <input id="f-ltAge" type="number" name="ltAge" min="18" max="120"
                   value="${filter != null && filter.lowerAgeBound != null ? filter.lowerAgeBound : ''}"
                   style="width:72px;">
        </div>

        <div class="field">
            <label for="f-role">${wordBundle.getWord("role")}</label>
            <select id="f-role" name="role">
                <option value=""></option>
                <c:forEach var="r" items="${applicationScope.roles}">
                    <option value="${r}" <c:if test="${r == filter.role}">selected</c:if>>
                        ${wordBundle.getWord(r)}
                    </option>
                </c:forEach>
            </select>
        </div>

        <div class="field">
            <label for="f-status">${wordBundle.getWord("status")}</label>
            <select id="f-status" name="status">
                <option value=""></option>
                <c:forEach var="st" items="${applicationScope.statuses}">
                    <option value="${st}" <c:if test="${st == filter.status}">selected</c:if>>
                        ${wordBundle.getWord(st)}
                    </option>
                </c:forEach>
            </select>
        </div>

        <div class="field">
            <label for="f-pageSize">${wordBundle.getWord("pageSize")}</label>
            <select id="f-pageSize" name="pageSize" style="width:88px;">
                <c:forEach var="size" items="${applicationScope.availablePageSizes}">
                    <option value="${size}" <c:if test="${size == filter.pageSize}">selected</c:if>>${size}</option>
                </c:forEach>
            </select>
        </div>

        <div class="actions">
            <button type="submit" class="btn-reset" title="${wordBundle.getWord('update')}" aria-label="Apply">
                <img class="icon-lg" src="${cpath}/content/app/img/filter.png" alt="">
            </button>
            <a class="btn-reset" href="${cpath}/profiles" title="Reset" aria-label="Reset">
                <img class="icon-lg" src="${cpath}/content/app/img/cross.png" alt="x">
            </a>
        </div>
    </form>

    <c:set var="sortBy" value="${filter.sortBy}"/>
    <c:set var="sortOrder"  value="${filter.sortOrder}"/>

    <table class="table--list">
        <colgroup>
            <col style="width:6ch">     <!-- id -->
            <col style="width:20%">     <!-- email -->
            <col style="width:14%">     <!-- name -->
            <col style="width:14%">     <!-- surname -->
            <col style="width:7ch">     <!-- age -->
            <col style="width:10%">     <!-- role -->
            <col>                       <!-- status -->
        </colgroup>

        <c:url var="urlId" value="${cpath}/profiles">
            <c:param name="sortBy" value="ID"/>
            <c:param name="sortOrder" value="${sortBy=='ID' ? (sortOrder=='ASC' ? 'DESC' : 'ASC') : 'ASC'}"/>
            <c:param name="emailStartsWith" value="${filter.emailStartsWith}"/>
            <c:param name="nameStartsWith" value="${filter.nameStartsWith}"/>
            <c:param name="surnameStartsWith" value="${filter.surnameStartsWith}"/>
            <c:param name="gteAge" value="${filter.greaterAndEqualAgeBound}"/>
            <c:param name="ltAge" value="${filter.lowerAgeBound}"/>
            <c:param name="role" value="${filter.role}"/>
            <c:param name="status" value="${filter.status}"/>
            <c:param name="page" value="1"/>
            <c:param name="pageSize" value="${filter.pageSize}"/>
        </c:url>

        <c:url var="urlEmail" value="${cpath}/profiles">
            <c:param name="sortBy" value="EMAIL"/>
            <c:param name="sortOrder" value="${sortBy=='EMAIL' ? (sortOrder=='ASC' ? 'DESC' : 'ASC') : 'ASC'}"/>
            <c:param name="emailStartsWith" value="${filter.emailStartsWith}"/>
            <c:param name="nameStartsWith" value="${filter.nameStartsWith}"/>
            <c:param name="surnameStartsWith" value="${filter.surnameStartsWith}"/>
            <c:param name="gteAge" value="${filter.greaterAndEqualAgeBound}"/>
            <c:param name="ltAge" value="${filter.lowerAgeBound}"/>
            <c:param name="role" value="${filter.role}"/>
            <c:param name="status" value="${filter.status}"/>
            <c:param name="page" value="1"/>
            <c:param name="pageSize" value="${filter.pageSize}"/>
        </c:url>

        <c:url var="urlName" value="${cpath}/profiles">
            <c:param name="sortBy" value="NAME"/>
            <c:param name="sortOrder" value="${sortBy=='NAME' ? (sortOrder=='ASC' ? 'DESC' : 'ASC') : 'ASC'}"/>
            <c:param name="emailStartsWith" value="${filter.emailStartsWith}"/>
            <c:param name="nameStartsWith" value="${filter.nameStartsWith}"/>
            <c:param name="surnameStartsWith" value="${filter.surnameStartsWith}"/>
            <c:param name="gteAge" value="${filter.greaterAndEqualAgeBound}"/>
            <c:param name="ltAge" value="${filter.lowerAgeBound}"/>
            <c:param name="role" value="${filter.role}"/>
            <c:param name="status" value="${filter.status}"/>
            <c:param name="page" value="1"/>
            <c:param name="pageSize" value="${filter.pageSize}"/>
        </c:url>

        <c:url var="urlSurname" value="${cpath}/profiles">
            <c:param name="sortBy" value="SURNAME"/>
            <c:param name="sortOrder" value="${sortBy=='SURNAME' ? (sortOrder=='ASC' ? 'DESC' : 'ASC') : 'ASC'}"/>
            <c:param name="emailStartsWith" value="${filter.emailStartsWith}"/>
            <c:param name="nameStartsWith" value="${filter.nameStartsWith}"/>
            <c:param name="surnameStartsWith" value="${filter.surnameStartsWith}"/>
            <c:param name="gteAge" value="${filter.greaterAndEqualAgeBound}"/>
            <c:param name="ltAge" value="${filter.lowerAgeBound}"/>
            <c:param name="role" value="${filter.role}"/>
            <c:param name="status" value="${filter.status}"/>
            <c:param name="page" value="1"/>
            <c:param name="pageSize" value="${filter.pageSize}"/>
        </c:url>

        <c:url var="urlBirthdate" value="${cpath}/profiles">
            <c:param name="sortBy" value="BIRTHDATE"/>
            <c:param name="sortOrder" value="${sortBy=='BIRTHDATE' ? (sortOrder=='ASC' ? 'DESC' : 'ASC') : 'ASC'}"/>
            <c:param name="emailStartsWith" value="${filter.emailStartsWith}"/>
            <c:param name="nameStartsWith" value="${filter.nameStartsWith}"/>
            <c:param name="surnameStartsWith" value="${filter.surnameStartsWith}"/>
            <c:param name="gteAge" value="${filter.greaterAndEqualAgeBound}"/>
            <c:param name="ltAge" value="${filter.lowerAgeBound}"/>
            <c:param name="role" value="${filter.role}"/>
            <c:param name="status" value="${filter.status}"/>
            <c:param name="page" value="1"/>
            <c:param name="pageSize" value="${filter.pageSize}"/>
        </c:url>

        <c:url var="urlRole" value="${cpath}/profiles">
            <c:param name="sortBy" value="ROLE"/>
            <c:param name="sortOrder" value="${sortBy=='ROLE' ? (sortOrder=='ASC' ? 'DESC' : 'ASC') : 'ASC'}"/>
            <c:param name="emailStartsWith" value="${filter.emailStartsWith}"/>
            <c:param name="nameStartsWith" value="${filter.nameStartsWith}"/>
            <c:param name="surnameStartsWith" value="${filter.surnameStartsWith}"/>
            <c:param name="gteAge" value="${filter.greaterAndEqualAgeBound}"/>
            <c:param name="ltAge" value="${filter.lowerAgeBound}"/>
            <c:param name="role" value="${filter.role}"/>
            <c:param name="status" value="${filter.status}"/>
            <c:param name="page" value="1"/>
            <c:param name="pageSize" value="${filter.pageSize}"/>
        </c:url>

        <c:url var="urlStatus" value="${cpath}/profiles">
            <c:param name="sortBy" value="STATUS"/>
            <c:param name="sortOrder" value="${sortBy=='STATUS' ? (sortOrder=='ASC' ? 'DESC' : 'ASC') : 'ASC'}"/>
            <c:param name="emailStartsWith" value="${filter.emailStartsWith}"/>
            <c:param name="nameStartsWith" value="${filter.nameStartsWith}"/>
            <c:param name="surnameStartsWith" value="${filter.surnameStartsWith}"/>
            <c:param name="gteAge" value="${filter.greaterAndEqualAgeBound}"/>
            <c:param name="ltAge" value="${filter.lowerAgeBound}"/>
            <c:param name="role" value="${filter.role}"/>
            <c:param name="status" value="${filter.status}"/>
            <c:param name="page" value="1"/>
            <c:param name="pageSize" value="${filter.pageSize}"/>
        </c:url>

        <tr>
            <td><a class="th-sort" href="${urlId}">id
                <span class="arrow"><c:if test="${sortBy=='ID'}">${sortOrder=='ASC'?'▲':'▼'}</c:if></span>
            </a></td>
            <td><a class="th-sort" href="${urlEmail}">${wordBundle.getWord('email')}
                <span class="arrow"><c:if test="${sortBy=='EMAIL'}">${sortOrder=='ASC'?'▲':'▼'}</c:if></span>
            </a></td>
            <td><a class="th-sort" href="${urlName}">${wordBundle.getWord('name')}
                <span class="arrow"><c:if test="${sortBy=='NAME'}">${sortOrder=='ASC'?'▲':'▼'}</c:if></span>
            </a></td>
            <td><a class="th-sort" href="${urlSurname}">${wordBundle.getWord('surname')}
                <span class="arrow"><c:if test="${sortBy=='SURNAME'}">${sortOrder=='ASC'?'▲':'▼'}</c:if></span>
            </a></td>
            <td><a class="th-sort" href="${urlBirthdate}">${wordBundle.getWord('age')}
                <span class="arrow"><c:if test="${sortBy=='BIRTHDATE'}">${sortOrder=='ASC'?'▲':'▼'}</c:if></span>
            </a></td>
            <td><a class="th-sort" href="${urlRole}">${wordBundle.getWord('role')}
                <span class="arrow"><c:if test="${sortBy=='ROLE'}">${sortOrder=='ASC'?'▲':'▼'}</c:if></span>
            </a></td>
            <td><a class="th-sort" href="${urlStatus}">${wordBundle.getWord('status')}
                <span class="arrow"><c:if test="${sortBy=='STATUS'}">${sortOrder=='ASC'?'▲':'▼'}</c:if></span>
            </a></td>
        </tr>

        <c:forEach var="profile" items="${profiles}">
            <tr>
                <td><h4>${profile.id}</h4></td>
                <td><h4>${profile.email}</h4></td>
                <td><h4>${profile.name}</h4></td>
                <td><h4>${profile.surname}</h4></td>
                <td><h4>${profile.age}</h4></td>
                <td><h4>${wordBundle.getWord(profile.role)}</h4></td>
                <td>
                    <form action="${cpath}/profile" method="post" class="row" style="gap:12px; align-items:center;">
                        <input type="hidden" name="_method" value="put"/>
                        <input type="hidden" name="id" value="${profile.id}">
                        <input type="hidden" name="from" value="list"/>

                        <c:set var="qs" value="${pageContext.request.queryString}" />
                        <input type="hidden" name="back" value="${cpath}/profiles${empty qs ? '' : '?'}${qs}" />

                        <c:set var="s" value="${profile.status}"/>
                        <select name="status">
                            <option value="" disabled <c:if test="${empty s}">selected</c:if> hidden>
                            ${wordBundle.getWord(profile.status)}
                            </option>
                            <c:forEach var="status" items="${applicationScope.statuses}">
                                <option value="${status}" <c:if test="${status == s}">selected</c:if>>
                                ${wordBundle.getWord(status)}
                                </option>
                            </c:forEach>
                        </select>

                        <input type="image" class="icon-lg"
                               src="${cpath}/content/app/img/floppy-disk.png"
                               alt="${wordBundle.getWord('save')}" title="${wordBundle.getWord('save')}"/>
                    </form>
                </td>
            </tr>
        </c:forEach>
    </table>

    <!-- Go to page (manual) -->
    <form method="get" action="${cpath}/profiles" class="row" style="gap:12px; align-items:center; margin:12px 0;">
        <input type="hidden" name="sortBy" value="${filter.sortBy}">
        <input type="hidden" name="sortOrder" value="${filter.sortOrder}">
        <input type="hidden" name="emailStartsWith" value="${filter.emailStartsWith}">
        <input type="hidden" name="nameStartsWith" value="${filter.nameStartsWith}">
        <input type="hidden" name="surnameStartsWith" value="${filter.surnameStartsWith}">
        <input type="hidden" name="gteAge" value="${filter.greaterAndEqualAgeBound}">
        <input type="hidden" name="ltAge" value="${filter.lowerAgeBound}">
        <input type="hidden" name="role" value="${filter.role}">
        <input type="hidden" name="status" value="${filter.status}">
        <input type="hidden" name="pageSize" value="${filter.pageSize}">

        <label for="go-page">${wordBundle.getWord('page')}</label>
        <input id="go-page" type="number" name="page" min="1" value="${filter.page}" style="width:72px;">
        <button type="submit" class="btn-reset">${wordBundle.getWord('update')}</button>
    </form>

    <!-- Pager -->
    <c:url var="prevUrl" value="${cpath}/profiles">
        <c:param name="sortBy" value="${filter.sortBy}"/>
        <c:param name="sortOrder" value="${filter.sortOrder}"/>
        <c:param name="emailStartsWith" value="${filter.emailStartsWith}"/>
        <c:param name="nameStartsWith" value="${filter.nameStartsWith}"/>
        <c:param name="surnameStartsWith" value="${filter.surnameStartsWith}"/>
        <c:param name="gteAge" value="${filter.greaterAndEqualAgeBound}"/>
        <c:param name="ltAge" value="${filter.lowerAgeBound}"/>
        <c:param name="role" value="${filter.role}"/>
        <c:param name="status" value="${filter.status}"/>
        <c:param name="page" value="${filter.page - 1}"/>
        <c:param name="pageSize" value="${filter.pageSize}"/>
    </c:url>

    <c:url var="nextUrl" value="${cpath}/profiles">
        <c:param name="sortBy" value="${filter.sortBy}"/>
        <c:param name="sortOrder" value="${filter.sortOrder}"/>
        <c:param name="emailStartsWith" value="${filter.emailStartsWith}"/>
        <c:param name="nameStartsWith" value="${filter.nameStartsWith}"/>
        <c:param name="surnameStartsWith" value="${filter.surnameStartsWith}"/>
        <c:param name="gteAge" value="${filter.greaterAndEqualAgeBound}"/>
        <c:param name="ltAge" value="${filter.lowerAgeBound}"/>
        <c:param name="role" value="${filter.role}"/>
        <c:param name="status" value="${filter.status}"/>
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
</div>

<%@ include file="footer.jsp" %>


</body>
</html>