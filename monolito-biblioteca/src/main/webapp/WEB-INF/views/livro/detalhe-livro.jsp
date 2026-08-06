<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="tcc.legado.model.Livro" %>
<%
    Livro livro = (Livro) request.getAttribute("livro");
    if (livro == null) {
        response.sendRedirect(request.getContextPath() + "/livro.do?metodo=listar");
        return;
    }
%>
<html>
<head>
    <meta charset="UTF-8">
    <title>Detalhes do Livro</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/webjars/font-awesome/6.5.2/css/all.min.css">
    <style>
        body { font-family: 'Segoe UI', sans-serif; background: #f5f7fa; margin: 0; padding: 20px; }
        .container { max-width: 800px; margin: 0 auto; background: white; border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.05); padding: 30px; }
        h1 { color: #2c3e50; display: flex; align-items: center; gap: 10px; }
        .detail-row { display: flex; padding: 12px 0; border-bottom: 1px solid #ecf0f1; }
        .detail-label { font-weight: 600; width: 150px; color: #7f8c8d; }
        .detail-value { flex: 1; color: #2c3e50; }
        .badge { padding: 4px 12px; border-radius: 20px; font-weight: 600; }
        .badge-active { background: #d4edda; color: #155724; }
        .badge-inactive { background: #f8d7da; color: #721c24; }
        .actions { margin-top: 30px; display: flex; gap: 10px; }
        .btn { display: inline-block; padding: 8px 16px; border-radius: 6px; text-decoration: none; font-weight: 600; transition: .3s; }
        .btn-primary { background: #2c3e50; color: white; }
        .btn-primary:hover { background: #1a252f; }
        .btn-secondary { background: #3498db; color: white; }
        .btn-secondary:hover { background: #2980b9; }
        .btn-light { background: #ecf0f1; color: #2c3e50; border: 1px solid #bdc3c7; }
        .btn-light:hover { background: #d5dbdb; }
    </style>
</head>
<body>
<div class="container">
    <h1><i class="fa-solid fa-book" style="color: #16a085;"></i> Detalhes do Livro</h1>
    <div class="detail-row">
        <div class="detail-label">ID</div>
        <div class="detail-value"><%= livro.getId() %></div>
    </div>
    <div class="detail-row">
        <div class="detail-label">Título</div>
        <div class="detail-value"><strong><%= livro.getTitulo() %></strong></div>
    </div>
    <div class="detail-row">
        <div class="detail-label">Autor</div>
        <div class="detail-value"><%= livro.getAutor() != null ? livro.getAutor() : "-" %></div>
    </div>
    <div class="detail-row">
        <div class="detail-label">ISBN</div>
        <div class="detail-value"><%= livro.getIsbn() != null ? livro.getIsbn() : "-" %></div>
    </div>
    <div class="detail-row">
        <div class="detail-label">Ano</div>
        <div class="detail-value"><%= livro.getAno() != null ? livro.getAno() : "-" %></div>
    </div>
    <div class="detail-row">
        <div class="detail-label">Editora</div>
        <div class="detail-value"><%= livro.getEditora() != null ? livro.getEditora() : "-" %></div>
    </div>
    <div class="detail-row">
        <div class="detail-label">Quantidade</div>
        <div class="detail-value">
            <span class="badge <%= livro.getQuantidade() > 0 ? "badge-active" : "badge-inactive" %>">
                <%= livro.getQuantidade() %>
            </span>
        </div>
    </div>
    <div class="actions">
        <a href="livro.do?metodo=listar" class="btn btn-light"><i class="fa-solid fa-arrow-left"></i> Voltar</a>
        <a href="livro.do?metodo=editar&id=<%= livro.getId() %>" class="btn btn-secondary"><i class="fa-solid fa-pen"></i> Editar</a>
    </div>
</div>
</body>
</html>