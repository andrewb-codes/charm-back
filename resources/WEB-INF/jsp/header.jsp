<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<div class="container">
    <header class="header-bar">
        <a href="${cpath}/" aria-label="Home">
            <img src="${cpath}/content/app/img/heart.png" alt="">
        </a>

        <form method="post" action="${cpath}/lang">
            <button class="btn-reset" type="submit" name="lang" value="ru" aria-label="Русский">
                <img src="${cpath}/content/app/img/ru.png" alt="">
            </button>
            <button class="btn-reset" type="submit" name="lang" value="en" aria-label="English">
                <img src="${cpath}/content/app/img/en.png" alt="">
            </button>
        </form>
    </header>
    <hr class="divider">
</div>