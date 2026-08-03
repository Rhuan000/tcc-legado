<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String erro = (String) request.getAttribute("erro");
%>
<html>
<head>
    <meta charset="UTF-8">
    <title>Erro</title>
    <link rel="stylesheet" href="styles.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/webjars/font-awesome/6.5.2/css/all.min.css">
</head>
<body>
    <div class="container" style="max-width:800px; margin-top:40px;">
        <div class="header">
            <h1 style="color:#e74c3c;"><i class="fa-solid fa-triangle-exclamation page-icon"></i>Erro</h1>
        </div>
        <div class="content">
            <div class="alert alert-error">
                <p><%= erro != null ? erro : "Ocorreu um erro inesperado." %></p>
            </div>
            <div style="margin-top:20px;">
                <a href="javascript:history.back()" class="btn btn-light">Voltar</a>
                <a href="index.jsp" class="btn btn-primary">Início</a>
            </div>
        </div>
    </div>
</body>
</html>