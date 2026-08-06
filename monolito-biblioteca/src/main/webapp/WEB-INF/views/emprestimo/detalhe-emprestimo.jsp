<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="tcc.legado.model.Emprestimo, tcc.legado.model.Livro, tcc.legado.model.Usuario" %>
<%
    Emprestimo emp = (Emprestimo) request.getAttribute("emprestimo");
    Livro livro = (Livro) request.getAttribute("livro");
    Usuario usuario = (Usuario) request.getAttribute("usuario");
    if (emp == null) {
        response.sendRedirect(request.getContextPath() + "/emprestimo.do?metodo=listar");
        return;
    }
%>
<html>
<head>
    <meta charset="UTF-8">
    <title>Detalhes do Empréstimo</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/webjars/font-awesome/6.5.2/css/all.min.css">
    <style>
        body { font-family: 'Segoe UI', sans-serif; background: #f5f7fa; margin: 0; padding: 20px; }
        .container { max-width: 800px; margin: 0 auto; background: white; border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.05); padding: 30px; }
        h1 { color: #2c3e50; display: flex; align-items: center; gap: 10px; }
        .detail-row { display: flex; padding: 12px 0; border-bottom: 1px solid #ecf0f1; }
        .detail-label { font-weight: 600; width: 150px; color: #7f8c8d; }
        .detail-value { flex: 1; color: #2c3e50; }
        .badge { padding: 4px 12px; border-radius: 20px; font-weight: 600; }
        .badge-pendente { background: #fff3cd; color: #856404; }
        .badge-devolvido { background: #d4edda; color: #155724; }
        .badge-multa { background: #f8d7da; color: #721c24; }
        .actions { margin-top: 30px; display: flex; gap: 10px; }
        .btn { display: inline-block; padding: 8px 16px; border-radius: 6px; text-decoration: none; font-weight: 600; transition: .3s; }
        .btn-light { background: #ecf0f1; color: #2c3e50; border: 1px solid #bdc3c7; }
        .btn-light:hover { background: #d5dbdb; }
        .btn-secondary { background: #3498db; color: white; }
        .btn-secondary:hover { background: #2980b9; }
        .btn-success { background: #27ae60; color: white; }
        .btn-success:hover { background: #1e8449; }
    </style>
</head>
<body>
<div class="container">
    <h1><i class="fa-solid fa-hand-holding-heart" style="color: #c0392b;"></i> Detalhes do Empréstimo</h1>
    <div class="detail-row">
        <div class="detail-label">ID</div>
        <div class="detail-value"><%= emp.getId() %></div>
    </div>
    <div class="detail-row">
        <div class="detail-label">Livro</div>
        <div class="detail-value">
            <%= livro != null ? livro.getTitulo() : "ID " + emp.getIdLivro() %>
        </div>
    </div>
    <div class="detail-row">
        <div class="detail-label">Usuário</div>
        <div class="detail-value">
            <%= usuario != null ? usuario.getNome() + " (" + usuario.getMatricula() + ")" : "ID " + emp.getIdUsuario() %>
        </div>
    </div>
    <div class="detail-row">
        <div class="detail-label">Data Empréstimo</div>
        <div class="detail-value"><%= emp.getDataEmprestimo() %></div>
    </div>
    <div class="detail-row">
        <div class="detail-label">Previsão Devolução</div>
        <div class="detail-value"><%= emp.getDataPrevistaDevolucao() %></div>
    </div>
    <div class="detail-row">
        <div class="detail-label">Devolução Real</div>
        <div class="detail-value">
            <% if (emp.getDataDevolucaoReal() != null) { %>
                <%= emp.getDataDevolucaoReal() %>
                <span class="badge badge-devolvido">Devolvido</span>
            <% } else { %>
                <span class="badge badge-pendente">Pendente</span>
            <% } %>
        </div>
    </div>
    <div class="detail-row">
        <div class="detail-label">Multa</div>
        <div class="detail-value">
            <% if (emp.getMulta() != null && emp.getMulta() > 0) { %>
                <span class="badge badge-multa">R$ <%= String.format("%.2f", emp.getMulta()) %></span>
            <% } else { %>
                R$ 0,00
            <% } %>
        </div>
    </div>
    <div class="actions">
        <a href="emprestimo.do?metodo=listar" class="btn btn-light"><i class="fa-solid fa-arrow-left"></i> Voltar</a>
        <% if (emp.getDataDevolucaoReal() == null) { %>
            <a href="emprestimo.do?metodo=devolverForm&id=<%= emp.getId() %>" class="btn btn-success"><i class="fa-solid fa-rotate-left"></i> Registrar Devolução</a>
        <% } %>
    </div>
</div>
</body>
</html>