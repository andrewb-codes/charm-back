<%@ page contentType="text/html;charset=UTF-8" %>
<html lang="en">
    <head>
      <title>Charm Profile</title>
    </head>
    <body>
    <%@ include file="header.html" %>
        <div>
           <table>
               <tr>
                   <td><h3>Email</h3></td>
                   <td>${requestScope.profile.email}</td>
               </tr>
               <tr>
                   <td><h3>Name</h3></td>
                   <td>${requestScope.profile.name}</td>
               </tr>
               <tr>
                   <td><h3>Surname</h3></td>
                   <td>${requestScope.profile.surname}</td>
               </tr>
               <tr>
                   <td><h3>About</h3></td>
                   <td>${requestScope.profile.about}</td>
               </tr>
           </table>
        </div>
        <%@ include file="footer.html" %>
    </body>
</html>