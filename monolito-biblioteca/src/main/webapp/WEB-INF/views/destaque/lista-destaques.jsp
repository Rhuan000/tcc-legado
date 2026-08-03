<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, tcc.legado.model.LivroDestaque" %>
<%
    List<LivroDestaque> destaques = (List<LivroDestaque>) request.getAttribute("lista");
    String mensagem = (String) request.getAttribute("mensagem");
%>
<html>
<head>
    <meta charset="UTF-8">
    <title>Livros em Destaque</title>
    <link rel="stylesheet" href="../styles.css">
    <link rel="stylesheet" href="styles.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/webjars/font-awesome/6.5.2/css/all.min.css">
</head>
<body>
    <div class="container">
        <div class="header">
            <h1><i class="fa-solid fa-star page-icon"></i>Livros em Destaque</h1>
            <p class="subtitle">Promoções, recomendações e lançamentos</p>
        </div>

        <% if (mensagem != null) { %>
            <div class="alert alert-success"><%= mensagem %></div>
        <% } %>

        <div class="content">
            <div class="action-buttons">
                <a href="destaque.do?metodo=novo" class="btn btn-primary"><i class="fa-solid fa-plus"></i> Novo Destaque</a>
                <a href="destaque.do?metodo=listar&tipo=ativos" class="btn btn-secondary"><i class="fa-solid fa-filter"></i> Somente ativos</a>
                <a href="index.jsp" class="btn btn-light"><i class="fa-solid fa-house"></i> Voltar</a>
            </div>

            <% if (destaques != null && !destaques.isEmpty()) { %>
                <div class="destaques-grid">
                    <% for (LivroDestaque d : destaques) { %>
                        <div class="destaque-card">
                            <div class="destaque-header">
                                <span class="badge <%= Boolean.TRUE.equals(d.getAtivo()) ? "badge-active" : "badge-inactive" %>"><%= Boolean.TRUE.equals(d.getAtivo()) ? "Ativo" : "Inativo" %></span>
                                <% if (d.getDesconto() != null && d.getDesconto() > 0) { %>
                                    <span class="badge badge-discount">-<%= d.getDesconto().intValue() %>%</span>
                                <% } %>
                            </div>
                            <div class="destaque-body">
                                <h3><%= d.getTitulo() %></h3>
                                <p class="category"><strong>Categoria:</strong> <span class="tag"><%= d.getCategoria() != null ? d.getCategoria() : "Geral" %></span></p>
                                <p class="description"><%= d.getDescricao() != null ? d.getDescricao() : "Sem descrição" %></p>
                                <div class="destaque-info">
                                    <div class="info-item"><strong>Livro ID:</strong> <%= d.getIdLivro() %></div>
                                    <div class="info-item"><strong>Visualizações:</strong> <%= d.getVisualizacoes() %></div>
                                    <% if (d.getDataInicio() != null) { %>
                                        <div class="info-item"><strong>Início:</strong> <%= new java.text.SimpleDateFormat("dd/MM/yyyy").format(d.getDataInicio()) %></div>
                                    <% } %>
                                    <% if (d.getDataFim() != null) { %>
                                        <div class="info-item"><strong>Fim:</strong> <%= new java.text.SimpleDateFormat("dd/MM/yyyy").format(d.getDataFim()) %></div>
                                    <% } %>
                                </div>
                            </div>
                            <div class="destaque-actions">
                                <a href="destaque.do?metodo=visualizar&id=<%= d.getId() %>" class="btn btn-small btn-info"><i class="fa-solid fa-eye"></i> Ver</a>
                                <a href="destaque.do?metodo=editar&id=<%= d.getId() %>" class="btn btn-small btn-warning"><i class="fa-solid fa-pen"></i> Editar</a>
                                <a href="destaque.do?metodo=excluir&id=<%= d.getId() %>" class="btn btn-small btn-danger" onclick="return confirm('Tem certeza?')"><i class="fa-solid fa-trash"></i> Excluir</a>
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
        </div>
    </div>
</body>
</html>