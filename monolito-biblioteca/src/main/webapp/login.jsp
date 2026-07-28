<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head><title>Login - Biblioteca Legada</title></head>
<body>
    <h1>🔐 Login</h1>
    <form action="auth.do?metodo=login" method="post">
        <label>Matrícula: <input type="text" name="matricula" required/></label><br/>
        <label>Senha: <input type="password" name="senha" required/></label><br/>
        <input type="submit" value="Entrar"/>
    </form>
    <%
        String erro = (String) request.getAttribute("erro");
        if (erro != null) {
    %>
        <p style="color:red;"><%= erro %></p>
    <% } %>
</body>
</html>
