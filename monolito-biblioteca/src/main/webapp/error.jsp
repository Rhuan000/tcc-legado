<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String erro = (String) request.getAttribute("erro");
%>
<html>
<head><title>Erro</title></head>
<body>
    <h1 style="color:red;">⚠️ Erro</h1>
    <p><%= erro != null ? erro : "Ocorreu um erro inesperado." %></p>
    <a href="javascript:history.back()">Voltar</a>
</body>
</html>
