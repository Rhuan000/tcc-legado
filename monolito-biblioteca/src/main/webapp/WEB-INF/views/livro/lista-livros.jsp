<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, tcc.legado.model.Livro" %>
<%
    List<Livro> livros = (List<Livro>) request.getAttribute("lista");
    String mensagem = (String) request.getAttribute("mensagem");
%>
<html>
<head>
    <meta charset="UTF-8">
    <title>Lista de Livros</title>
    <link rel="stylesheet" href="../styles.css">
    <link rel="stylesheet" href="styles.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/webjars/font-awesome/6.5.2/css/all.min.css">
</head>
<body>
    <div class="container">
        <div class="header">
            <h1><i class="fa-solid fa-book-open page-icon"></i>Catálogo de Livros</h1>
            <p class="subtitle">Gerenciamento do acervo da biblioteca</p>
        </div>

        <% if (mensagem != null) { %>
            <div class="alert alert-success"><%= mensagem %></div>
        <% } %>

        <div class="content">
            <div class="action-buttons">
                <a href="livro.do?metodo=novo" class="btn btn-primary"><i class="fa-solid fa-plus"></i> Cadastrar Novo Livro</a>
                <a href="livro.do?metodo=voltar" class="btn btn-light"><i class="fa-solid fa-house"></i> Voltar</a>
            </div>

            <% if (livros != null && !livros.isEmpty()) { %>
                <div class="livro-table">
                    <table>
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Título</th>
                                <th>Autor</th>
                                <th>ISBN</th>
                                <th>Ano</th>
                                <th>Editora</th>
                                <th>Quantidade</th>
                                <th>Ações</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% 
                                String perfil = (String) session.getAttribute("perfil");
                                for (Livro l : livros) { 
                            %>
                                <tr>
                                    <td><%= l.getId() %></td>
                                    <td><strong><%= l.getTitulo() %></strong></td>
                                    <td><%= l.getAutor() %></td>
                                    <td><%= l.getIsbn() %></td>
                                    <td><%= l.getAno() %></td>
                                    <td><%= l.getEditora() %></td>
                                    <td>
                                        <span class="quantidade-badge <%= l.getQuantidade() > 0 ? "quantidade-disponivel" : "quantidade-indisponivel" %>"><%= l.getQuantidade() %></span>
                                    </td>
                                    <td>
                                        <% if ("ADMIN".equals(perfil)) { %>
                                            <a href="livro.do?metodo=editar&id=<%= l.getId() %>" class="btn btn-small btn-warning"><i class="fa-solid fa-pen"></i> Editar</a>
                                            <a href="livro.do?metodo=excluir&id=<%= l.getId() %>" class="btn btn-small btn-danger" onclick="return confirm('Tem certeza?')"><i class="fa-solid fa-trash"></i> Excluir</a>
                                        <% } else { %>
                                            <span class="badge badge-inactive">Visualização apenas</span>
                                        <% } %>
                                    </td>
                                </tr>
                            <% } %>
                        </tbody>
                    </table>
                </div>
            <% } else { %>
                <div class="empty-state">
                    <p>Nenhum livro cadastrado ainda.</p>
                    <a href="livro.do?metodo=novo" class="btn btn-primary"><i class="fa-solid fa-plus"></i> Cadastrar Primeiro Livro</a>
                </div>
            <% } %>
        </div>
    </div>
</body>
</html>