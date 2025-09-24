<%@ page contentType="text/html;charset=UTF-8" %>
<div>
    <h3>Charm <3</h3>
    <form action="${pageContext.request.contextPath}/lang" method="post">
        <button name="lang" value="ru">ru</button>
        <button name="lang" value="en">en</button>
    </form>
    <h3>${requestScope.wordBundle.getWord("lang")}: ${empty cookie["lang"] ? 'en' : cookie["lang"].value}</h3>
    <hr>
</div>