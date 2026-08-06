<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="tcc.legado.model.LivroDestaque" %>
<%
    LivroDestaque destaque = (LivroDestaque) request.getAttribute("destaque");
    if (destaque == null) {
        response.sendRedirect(request.getContextPath() + "/destaque.do?metodo=listar");
        return;
    }
%>
<html>
<head>
    <meta charset="UTF-8">
    <title>Detalhes do Destaque</title>
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
        .badge-discount { background: #f39c12; color: white; }
        .actions { margin-top: 30px; display: flex; gap: 10px; }
        .btn { display: inline-block; padding: 8px 16px; border-radius: 6px; text-decoration: none; font-weight: 600; transition: .3s; }
        .btn-light { background: #ecf0f1; color: #2c3e50; border: 1px solid #bdc3c7; }
        .btn-light:hover { background: #d5dbdb; }
        .btn-warning { background: #f39c12; color: white; }
        .btn-warning:hover { background: #d68910; }
    </style>
</head>
<body>
<div class="container">
    <h1><i class="fa-solid fa-star" style="color: #f1c40f;"></i> Detalhes do Destaque</h1>
    <div class="detail-row">
        <div class="detail-label">ID</div>
        <div class="detail-value"><%= destaque.getId() %></div>
    </div>
    <div class="detail-row">
        <div class="detail-label">Título</div>
        <div class="detail-value"><strong><%= destaque.getTitulo() %></strong></div>
    </div>
    <div class="detail-row">
        <div class="detail-label">Livro ID</div>
        <div class="detail-value"><%= destaque.getIdLivro() %></div>
    </div>
    <div class="detail-row">
        <div class="detail-label">Descrição</div>
        <div class="detail-value"><%= destaque.getDescricao() != null ? destaque.getDescricao() : "-" %></div>
    </div>
    <div class="detail-row">
        <div class="detail-label">Categoria</div>
        <div class="detail-value"><%= destaque.getCategoria() != null ? destaque.getCategoria() : "Geral" %></div>
    </div>
    <div class="detail-row">
        <div class="detail-label">Desconto</div>
        <div class="detail-value">
            <% if (destaque.getDesconto() != null && destaque.getDesconto() > 0) { %>
                <span class="badge badge-discount"><%= destaque.getDesconto().intValue() %>%</span>
            <% } else { %>
                0%
            <% } %>
        </div>
    </div>
    <div class="detail-row">
        <div class="detail-label">Data Início</div>
        <div class="detail-value"><%= destaque.getDataInicio() != null ? destaque.getDataInicio() : "-" %></div>
    </div>
    <div class="detail-row">
        <div class="detail-label">Data Fim</div>
        <div class="detail-value"><%= destaque.getDataFim() != null ? destaque.getDataFim() : "-" %></div>
    </div>
    <div class="detail-row">
        <div class="detail-label">Ativo</div>
        <div class="detail-value">
            <span class="badge <%= Boolean.TRUE.equals(destaque.getAtivo()) ? "badge-active" : "badge-inactive" %>">
                <%= Boolean.TRUE.equals(destaque.getAtivo()) ? "Sim" : "Não" %>
            </span>
        </div>
    </div>
    <div class="detail-row">
        <div class="detail-label">Visualizações</div>
        <div class="detail-value"><%= destaque.getVisualizacoes() != null ? destaque.getVisualizacoes() : 0 %></div>
    </div>
    <div class="actions">
        <a href="destaque.do?metodo=listar" class="btn btn-light"><i class="fa-solid fa-arrow-left"></i> Voltar</a>
        <a href="destaque.do?metodo=editar&id=<%= destaque.getId() %>" class="btn btn-warning"><i class="fa-solid fa-pen"></i> Editar</a>
    </div>
</div>
</body>
</html>