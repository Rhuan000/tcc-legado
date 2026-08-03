<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="tcc.legado.model.LivroDestaque" %>
<%
    LivroDestaque destaque = (LivroDestaque) request.getAttribute("destaque");
    String titulo = destaque != null ? "Editar Destaque" : "Novo Destaque";
    String submitText = destaque != null ? "Atualizar" : "Cadastrar";
%>
<html>
<head>
    <meta charset="UTF-8">
    <title><%= titulo %></title>
    <link rel="stylesheet" href="../styles.css">
    <link rel="stylesheet" href="styles.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/webjars/font-awesome/6.5.2/css/all.min.css">
</head>
<body>
    <div class="container">
        <div class="header">
            <h1><i class="fa-solid fa-star page-icon"></i><%= titulo %></h1>
            <p class="subtitle">Destaques, promoções e recomendações de livros</p>
        </div>

        <div class="content">
            <form method="POST" action="destaque.do?metodo=salvar" class="form-container">
                <% if (destaque != null) { %>
                    <input type="hidden" name="id" value="<%= destaque.getId() %>">
                <% } %>

                <div class="form-group">
                    <label for="idLivro">ID do Livro *</label>
                    <input type="number" id="idLivro" name="idLivro" required class="form-control" value="<%= destaque != null ? destaque.getIdLivro() : "" %>">
                    <small>Referência ao ID do livro na base</small>
                </div>

                <div class="form-group">
                    <label for="titulo">Título *</label>
                    <input type="text" id="titulo" name="titulo" required class="form-control" value="<%= destaque != null ? destaque.getTitulo() : "" %>" placeholder="Ex: Promoção de Verão">
                </div>

                <div class="form-group">
                    <label for="descricao">Descrição</label>
                    <textarea id="descricao" name="descricao" rows="4" class="form-control" placeholder="Descreva o destaque"><%= destaque != null && destaque.getDescricao() != null ? destaque.getDescricao() : "" %></textarea>
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label for="categoria">Categoria</label>
                        <select id="categoria" name="categoria" class="form-control">
                            <option value="">Selecione...</option>
                            <option value="Promoção" <%= destaque != null && "Promoção".equals(destaque.getCategoria()) ? "selected" : "" %>>Promoção</option>
                            <option value="Bestseller" <%= destaque != null && "Bestseller".equals(destaque.getCategoria()) ? "selected" : "" %>>Bestseller</option>
                            <option value="Novo Lançamento" <%= destaque != null && "Novo Lançamento".equals(destaque.getCategoria()) ? "selected" : "" %>>Novo Lançamento</option>
                            <option value="Recomendado" <%= destaque != null && "Recomendado".equals(destaque.getCategoria()) ? "selected" : "" %>>Recomendado</option>
                            <option value="Clássico" <%= destaque != null && "Clássico".equals(destaque.getCategoria()) ? "selected" : "" %>>Clássico</option>
                        </select>
                    </div>

                    <div class="form-group">
                        <label for="desconto">Desconto (%)</label>
                        <input type="number" id="desconto" name="desconto" min="0" max="100" step="0.01" class="form-control" value="<%= destaque != null && destaque.getDesconto() != null ? destaque.getDesconto() : "" %>" placeholder="0">
                    </div>
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label for="dataInicio">Data de Início</label>
                        <input type="date" id="dataInicio" name="dataInicio" class="form-control" value="<%= destaque != null && destaque.getDataInicio() != null ? new java.text.SimpleDateFormat("yyyy-MM-dd").format(destaque.getDataInicio()) : "" %>">
                    </div>

                    <div class="form-group">
                        <label for="dataFim">Data de Fim</label>
                        <input type="date" id="dataFim" name="dataFim" class="form-control" value="<%= destaque != null && destaque.getDataFim() != null ? new java.text.SimpleDateFormat("yyyy-MM-dd").format(destaque.getDataFim()) : "" %>">
                    </div>
                </div>

                <div class="form-group form-checkbox">
                    <label>
                        <input type="checkbox" id="ativo" name="ativo" <%= destaque != null && Boolean.TRUE.equals(destaque.getAtivo()) ? "checked" : "" %>>
                        <span>Ativo</span>
                    </label>
                </div>

                <div class="form-actions">
                    <button type="submit" class="btn btn-success"><%= submitText %></button>
                    <a href="destaque.do?metodo=listar" class="btn btn-light">Cancelar</a>
                </div>
            </form>
        </div>
    </div>
</body>
</html>