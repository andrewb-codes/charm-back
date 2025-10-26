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
    <!-- Filtration -->
    <form method="get" action="${cpath}/profiles" class="filters">
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

        <div class="actions">
            <button type="submit" class="btn-reset" title="${wordBundle.getWord('update')}" aria-label="Apply">
                <img class="icon-lg" src="${cpath}/content/app/img/filter.png" alt="">
            </button>
            <a class="btn-reset" href="${cpath}/profiles" title="Reset" aria-label="Reset">
                <img class="icon-lg" src="${cpath}/content/app/img/cross.png" alt="x">
            </a>
        </div>
    </form>

    <table class="table--list">
        <colgroup>
            <col style="width:6ch">          <!-- id -->
            <col style="width:28%">          <!-- email -->
            <col style="width:16%">          <!-- name -->
            <col style="width:16%">          <!-- surname -->
            <col style="width:8ch">          <!-- age -->
            <col style="width:26%">          <!-- status -->
        </colgroup>

        <tr>
            <td><h3>id</h3></td>
            <td><h3>${wordBundle.getWord("email")}</h3></td>
            <td><h3>${wordBundle.getWord("name")}</h3></td>
            <td><h3>${wordBundle.getWord("surname")}</h3></td>
            <td><h3>${wordBundle.getWord("age")}</h3></td>
            <td><h3>${wordBundle.getWord("status")}</h3></td>
        </tr>

        <c:forEach var="profile" items="${profiles}">
            <tr>
                <td><h4>${profile.id}</h4></td>
                <td><h4>${profile.email}</h4></td>
                <td><h4>${profile.name}</h4></td>
                <td><h4>${profile.surname}</h4></td>
                <td><h4>${profile.age}</h4></td>
                <td>
                    <form action="${cpath}/profile" method="post" class="row" style="gap:12px; align-items:center;">
                        <input type="hidden" name="_method" value="put"/>
                        <input type="hidden" name="id" value="${profile.id}">
                        <input type="hidden" name="from" value="list"/>

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