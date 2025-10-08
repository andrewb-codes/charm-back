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