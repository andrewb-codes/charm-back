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
        <input type="hidden" name="sortBy" value="${empty param.sortBy ? 'ID' : param.sortBy}">
        <input type="hidden" name="sortOrder"  value="${empty param.sortOrder  ? 'ASC' : param.sortOrder}">

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
            <c:set var="fr" value="${filter != null ? filter.role : null}"/>
            <select id="f-role" name="role">
                <option value=""></option>
                <c:forEach var="r" items="${applicationScope.roles}">
                    <option value="${r}" <c:if test="${r == fr}">selected</c:if>>
                    ${wordBundle.getWord(r)}
                    </option>
                </c:forEach>
            </select>
        </div>

        <div class="field">
            <label for="f-status">${wordBundle.getWord("status")}</label>
            <c:set var="fs" value="${filter != null ? filter.status : null}"/>
            <select id="f-status" name="status">
                <option value=""></option>
                <c:forEach var="st" items="${applicationScope.statuses}">
                    <option value="${st}" <c:if test="${st == fs}">selected</c:if>>
                    ${wordBundle.getWord(st)}
                    </option>
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

    <c:set var="sortBy" value="${empty param.sortBy ? 'ID' : param.sortBy}"/>
    <c:set var="sortOrder"  value="${empty param.sortOrder  ? 'ASC' : param.sortOrder}"/>

    <c:set var="toggleDir">
        <c:choose>
            <c:when test="${sortOrder == 'ASC'}">DESC</c:when>
            <c:otherwise>ASC</c:otherwise>
        </c:choose>
    </c:set>

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
            <c:param name="sortOrder"  value="${sortBy=='ID' ? (sortOrder=='ASC' ? 'DESC' : 'ASC') : 'ASC'}"/>
            <c:param name="emailStartsWith" value="${param.emailStartsWith}"/>
            <c:param name="nameStartsWith" value="${param.nameStartsWith}"/>
            <c:param name="surnameStartsWith" value="${param.surnameStartsWith}"/>
            <c:param name="gteAge" value="${param.gteAge}"/>
            <c:param name="ltAge" value="${param.ltAge}"/>
            <c:param name="role" value="${param.role}"/>
            <c:param name="status" value="${param.status}"/>
        </c:url>

        <c:url var="urlEmail" value="${cpath}/profiles">
            <c:param name="sortBy" value="EMAIL"/>
            <c:param name="sortOrder"  value="${sortBy=='EMAIL' ? (sortOrder=='ASC' ? 'DESC' : 'ASC') : 'ASC'}"/>
            <c:param name="emailStartsWith" value="${param.emailStartsWith}"/>
            <c:param name="nameStartsWith" value="${param.nameStartsWith}"/>
            <c:param name="surnameStartsWith" value="${param.surnameStartsWith}"/>
            <c:param name="gteAge" value="${param.gteAge}"/>
            <c:param name="ltAge" value="${param.ltAge}"/>
            <c:param name="role" value="${param.role}"/>
            <c:param name="status" value="${param.status}"/>
        </c:url>

        <c:url var="urlName" value="${cpath}/profiles">
            <c:param name="sortBy" value="NAME"/>
            <c:param name="sortOrder"  value="${sortBy=='NAME' ? (sortOrder=='ASC' ? 'DESC' : 'ASC') : 'ASC'}"/>
            <c:param name="emailStartsWith" value="${param.emailStartsWith}"/>
            <c:param name="nameStartsWith" value="${param.nameStartsWith}"/>
            <c:param name="surnameStartsWith" value="${param.surnameStartsWith}"/>
            <c:param name="gteAge" value="${param.gteAge}"/>
            <c:param name="ltAge" value="${param.ltAge}"/>
            <c:param name="role" value="${param.role}"/>
            <c:param name="status" value="${param.status}"/>
        </c:url>

        <c:url var="urlSurname" value="${cpath}/profiles">
            <c:param name="sortBy" value="SURNAME"/>
            <c:param name="sortOrder"  value="${sortBy=='SURNAME' ? (sortOrder=='ASC' ? 'DESC' : 'ASC') : 'ASC'}"/>
            <c:param name="emailStartsWith" value="${param.emailStartsWith}"/>
            <c:param name="nameStartsWith" value="${param.nameStartsWith}"/>
            <c:param name="surnameStartsWith" value="${param.surnameStartsWith}"/>
            <c:param name="gteAge" value="${param.gteAge}"/>
            <c:param name="ltAge" value="${param.ltAge}"/>
            <c:param name="role" value="${param.role}"/>
            <c:param name="status" value="${param.status}"/>
        </c:url>

        <c:url var="urlBirthdate" value="${cpath}/profiles">
            <c:param name="sortBy" value="BIRTHDATE"/>
            <c:param name="sortOrder"  value="${sortBy=='BIRTHDATE' ? (sortOrder=='ASC' ? 'DESC' : 'ASC') : 'ASC'}"/>
            <c:param name="emailStartsWith" value="${param.emailStartsWith}"/>
            <c:param name="nameStartsWith" value="${param.nameStartsWith}"/>
            <c:param name="surnameStartsWith" value="${param.surnameStartsWith}"/>
            <c:param name="gteAge" value="${param.gteAge}"/>
            <c:param name="ltAge" value="${param.ltAge}"/>
            <c:param name="role" value="${param.role}"/>
            <c:param name="status" value="${param.status}"/>
        </c:url>

        <c:url var="urlRole" value="${cpath}/profiles">
            <c:param name="sortBy" value="ROLE"/>
            <c:param name="sortOrder"  value="${sortBy=='ROLE' ? (sortOrder=='ASC' ? 'DESC' : 'ASC') : 'ASC'}"/>
            <c:param name="emailStartsWith" value="${param.emailStartsWith}"/>
            <c:param name="nameStartsWith" value="${param.nameStartsWith}"/>
            <c:param name="surnameStartsWith" value="${param.surnameStartsWith}"/>
            <c:param name="gteAge" value="${param.gteAge}"/>
            <c:param name="ltAge" value="${param.ltAge}"/>
            <c:param name="role" value="${param.role}"/>
            <c:param name="status" value="${param.status}"/>
        </c:url>

        <c:url var="urlStatus" value="${cpath}/profiles">
            <c:param name="sortBy" value="STATUS"/>
            <c:param name="sortOrder"  value="${sortBy=='STATUS' ? (sortOrder=='ASC' ? 'DESC' : 'ASC') : 'ASC'}"/>
            <c:param name="emailStartsWith" value="${param.emailStartsWith}"/>
            <c:param name="nameStartsWith" value="${param.nameStartsWith}"/>
            <c:param name="surnameStartsWith" value="${param.surnameStartsWith}"/>
            <c:param name="gteAge" value="${param.gteAge}"/>
            <c:param name="ltAge" value="${param.ltAge}"/>
            <c:param name="role" value="${param.role}"/>
            <c:param name="status" value="${param.status}"/>
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
</div>

<%@ include file="footer.jsp" %>
</body>
</html>