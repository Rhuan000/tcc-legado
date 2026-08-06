<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, tcc.legado.model.LivroDestaque" %>
<%
    List<LivroDestaque> destaques = (List<LivroDestaque>) request.getAttribute("lista");
    String mensagem = (String) request.getAttribute("mensagem");
    List<LivroDestaque> maisEmprestados = (List<LivroDestaque>) request.getAttribute("listaMaisEmprestados");
%>
<html>
<head>
    <meta charset="UTF-8">
    <title>Livros em Destaque</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/webjars/font-awesome/6.5.2/css/all.min.css">
    <style>
        /* Estilos específicos para a página */
        body { font-family: 'Segoe UI', sans-serif; background: #f5f7fa; margin: 0; padding: 20px; }
        .container { max-width: 1200px; margin: 0 auto; }
        .header { margin-bottom: 30px; }
        .header h1 { font-size: 2rem; color: #2c3e50; display: flex; align-items: center; gap: 10px; }
        .header p { color: #7f8c8d; }
        .alert-success { background: #d4edda; color: #155724; padding: 10px; border-radius: 4px; margin-bottom: 20px; }
        .action-buttons { margin-bottom: 25px; display: flex; gap: 10px; flex-wrap: wrap; }
        .btn { display: inline-block; padding: 8px 16px; border-radius: 6px; text-decoration: none; font-weight: 600; transition: .3s; }
        .btn-primary { background: #2c3e50; color: white; }
        .btn-primary:hover { background: #1a252f; }
        .btn-secondary { background: #e74c3c; color: white; }
        .btn-secondary:hover { background: #c0392b; }
        .btn-light { background: #ecf0f1; color: #2c3e50; border: 1px solid #bdc3c7; }
        .btn-light:hover { background: #d5dbdb; }
        .destaques-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 20px; }
        .destaque-card { background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.05); transition: .3s; border: 1px solid #e9ecef; }
        .destaque-card:hover { transform: translateY(-4px); box-shadow: 0 8px 24px rgba(0,0,0,0.1); }
        .destaque-header { background: #2c3e50; color: white; padding: 12px 16px; display: flex; justify-content: space-between; align-items: center; }
        .badge { padding: 4px 10px; border-radius: 20px; font-size: 0.75rem; font-weight: 600; }
        .badge-active { background: #27ae60; color: white; }
        .badge-inactive { background: #95a5a6; color: white; }
        .badge-discount { background: #f39c12; color: white; }
        .destaque-body { padding: 16px; }
        .destaque-body h3 { margin: 0 0 8px; font-size: 1.2rem; color: #2c3e50; }
        .destaque-body .category { margin-bottom: 10px; color: #7f8c8d; }
        .destaque-body .description { color: #34495e; font-size: 0.95rem; margin-bottom: 12px; }
        .destaque-info { display: grid; grid-template-columns: 1fr 1fr; gap: 6px; font-size: 0.9rem; color: #7f8c8d; padding-top: 10px; border-top: 1px solid #ecf0f1; }
        .destaque-actions { padding: 12px 16px; border-top: 1px solid #ecf0f1; display: flex; gap: 8px; flex-wrap: wrap; }
        .btn-small { padding: 6px 12px; font-size: 0.8rem; border-radius: 4px; text-decoration: none; display: inline-flex; align-items: center; gap: 4px; }
        .btn-info { background: #3498db; color: white; }
        .btn-warning { background: #f39c12; color: white; }
        .btn-danger { background: #e74c3c; color: white; }
        .empty-state { text-align: center; padding: 60px 20px; background: white; border-radius: 12px; border: 1px dashed #bdc3c7; }
        .section-title { margin-top: 40px; font-size: 1.5rem; color: #2c3e50; border-bottom: 2px solid #ecf0f1; padding-bottom: 10px; display: flex; align-items: center; gap: 10px; }
        .tag { background: #ecf0f1; padding: 2px 10px; border-radius: 12px; font-size: 0.8rem; }
    </style>
</head>
<body>
<div class="container">
    <div class="header">
        <h1><i class="fa-solid fa-star" style="color: #f1c40f;"></i> Livros em Destaque</h1>
        <p>Promoções, recomendações e lançamentos</p>
    </div>

    <% if (mensagem != null) { %>
        <div class="alert-success"><%= mensagem %></div>
    <% } %>

    <div class="action-buttons">
        <a href="destaque.do?metodo=novo" class="btn btn-primary"><i class="fa-solid fa-plus"></i> Novo Destaque</a>
        <a href="destaque.do?metodo=listar&tipo=ativos" class="btn btn-secondary"><i class="fa-solid fa-filter"></i> Ativos</a>
        <a href="destaque.do?metodo=voltar" class="btn btn-light"><i class="fa-solid fa-house"></i> Voltar</a>
    </div>

    <% if (destaques != null && !destaques.isEmpty()) { %>
        <div class="destaques-grid">
            <% for (LivroDestaque d : destaques) { %>
                <div class="destaque-card">
                    <div class="destaque-header">
                        <span><%= d.getTitulo() %></span>
                        <span>
                            <span class="badge <%= Boolean.TRUE.equals(d.getAtivo()) ? "badge-active" : "badge-inactive" %>">
                                <%= Boolean.TRUE.equals(d.getAtivo()) ? "Ativo" : "Inativo" %>
                            </span>
                            <% if (d.getDesconto() != null && d.getDesconto() > 0) { %>
                                <span class="badge badge-discount">-<%= d.getDesconto().intValue() %>%</span>
                            <% } %>
                        </span>
                    </div>
                    <div class="destaque-body">
                        <h3><%= d.getTitulo() %></h3>
                        <div class="category"><strong>Categoria:</strong> <span class="tag"><%= d.getCategoria() != null ? d.getCategoria() : "Geral" %></span></div>
                        <div class="description"><%= d.getDescricao() != null ? d.getDescricao() : "Sem descrição" %></div>
                        <div class="destaque-info">
                            <div><strong>Livro ID:</strong> <%= d.getIdLivro() %></div>
                            <div><strong>Visualizações:</strong> <%= d.getVisualizacoes() %></div>
                            <% if (d.getDataInicio() != null) { %>
                                <div><strong>Início:</strong> <%= new java.text.SimpleDateFormat("dd/MM/yyyy").format(d.getDataInicio()) %></div>
                            <% } %>
                            <% if (d.getDataFim() != null) { %>
                                <div><strong>Fim:</strong> <%= new java.text.SimpleDateFormat("dd/MM/yyyy").format(d.getDataFim()) %></div>
                            <% } %>
                        </div>
                    </div>
                    <div class="destaque-actions">
                        <a href="destaque.do?metodo=visualizar&id=<%= d.getId() %>" class="btn-small btn-info"><i class="fa-solid fa-eye"></i> Ver</a>
                        <a href="destaque.do?metodo=editar&id=<%= d.getId() %>" class="btn-small btn-warning"><i class="fa-solid fa-pen"></i> Editar</a>
                        <a href="destaque.do?metodo=excluir&id=<%= d.getId() %>" class="btn-small btn-danger" onclick="return confirm('Tem certeza?')"><i class="fa-solid fa-trash"></i> Excluir</a>
                    </div>
                </div>
            <% } %>
        </div>
    <% } else { %>
        <div class="empty-state">
            <p>Nenhum destaque cadastrado ainda.</p>
            <a href="destaque.do?metodo=novo" class="btn btn-primary"><i class="fa-solid fa-plus"></i> Criar primeiro destaque</a>
        </div>
    <% } %>

    <!-- Seção de Mais Emprestados (opcional) -->
    <% if (maisEmprestados != null && !maisEmprestados.isEmpty()) { %>
        <div class="section-title">
            <i class="fa-solid fa-chart-line" style="color: #e67e22;"></i> Mais Emprestados do Mês
        </div>
        <div class="destaques-grid" style="margin-top: 15px;">
            <% for (LivroDestaque d : maisEmprestados) { %>
                <div class="destaque-card" style="border-left: 4px solid #e67e22;">
                    <div class="destaque-header" style="background: #e67e22;">
                        <span>🔥 Mais Pedido</span>
                        <span class="badge badge-active">Ativo</span>
                    </div>
                    <div class="destaque-body">
                        <h3><%= d.getTitulo() %></h3>
                        <div class="description"><%= d.getDescricao() != null ? d.getDescricao() : "Sem detalhes" %></div>
                        <div class="destaque-info">
                            <div><strong>Livro ID:</strong> <%= d.getIdLivro() %></div>
                            <div><strong>Visualizações:</strong> <%= d.getVisualizacoes() %></div>
                        </div>
                    </div>
                    <div class="destaque-actions">
                        <a href="destaque.do?metodo=visualizar&id=<%= d.getId() %>" class="btn-small btn-info"><i class="fa-solid fa-eye"></i> Ver</a>
                    </div>
                </div>
            <% } %>
        </div>
    <% } %>
</div>
</body>
</html>