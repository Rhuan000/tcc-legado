<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="tcc.legado.model.Livro" %>
<%
    Livro livro = (Livro) request.getAttribute("livro");
    boolean editando = (livro != null && livro.getId() != null);
    String titulo = editando ? "Editar Livro" : "Cadastrar Livro";
    String submitText = editando ? "Atualizar" : "Cadastrar";
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
            <h1><i class="fa-solid fa-book page-icon"></i><%= titulo %></h1>
            <p class="subtitle">Gerencie os dados do livro</p>
        </div>

        <div class="content">
            <form action="livro.do" method="post" class="livro-form">
                <input type="hidden" name="metodo" value="salvar" />
                <% if (editando) { %>
                    <input type="hidden" name="id" value="<%= livro.getId() %>" />
                <% } %>

                <div class="form-group">
                    <label for="titulo">Título *</label>
                    <input id="titulo" type="text" name="titulo" class="form-control" required
                           value="<%= editando ? livro.getTitulo() : "" %>">
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label for="autor">Autor</label>
                        <input id="autor" type="text" name="autor" class="form-control" value="<%= editando ? livro.getAutor() : "" %>">
                    </div>

                    <div class="form-group">
                        <label for="isbn">ISBN</label>
                        <input id="isbn" type="text" name="isbn" class="form-control" value="<%= editando ? livro.getIsbn() : "" %>">
                    </div>
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label for="ano">Ano</label>
                        <input id="ano" type="number" name="ano" class="form-control" value="<%= editando && livro.getAno() != null ? livro.getAno() : "" %>">
                    </div>

                    <div class="form-group">
                        <label for="editora">Editora</label>
                        <input id="editora" type="text" name="editora" class="form-control" value="<%= editando ? livro.getEditora() : "" %>">
                    </div>

                    <div class="form-group">
                        <label for="quantidade">Quantidade</label>
                        <input id="quantidade" type="number" name="quantidade" class="form-control" value="<%= editando && livro.getQuantidade() != null ? livro.getQuantidade() : "0" %>">
                    </div>
                </div>

                <div class="form-actions">
                    <button type="submit" class="btn btn-success"><%= submitText %></button>
                    <a href="livro.do?metodo=listar" class="btn btn-light">Cancelar</a>
                </div>
            </form>
        </div>
    </div>
</body>
</html>