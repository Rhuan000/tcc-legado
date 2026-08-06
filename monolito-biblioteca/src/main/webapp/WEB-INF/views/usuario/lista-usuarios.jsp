<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, tcc.legado.model.Usuario" %>
<%
    List<Usuario> usuarios = (List<Usuario>) request.getAttribute("lista");
    String mensagem = (String) request.getAttribute("mensagem");
%>
<html>
<head>
    <meta charset="UTF-8">
    <title>Lista de Usuários</title>
    <link rel="stylesheet" href="../styles.css">
    <link rel="stylesheet" href="styles.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/webjars/font-awesome/6.5.2/css/all.min.css">
</head>
<body>
    <div class="container">
        <div class="header">
            <h1><i class="fa-solid fa-users page-icon"></i>Gerenciamento de Usuários</h1>
            <p class="subtitle">Alunos, Professores e Bolsistas</p>
        </div>

        <% if (mensagem != null) { %>
            <div class="alert alert-success"><%= mensagem %></div>
        <% } %>

        <div class="content">
            <div class="action-buttons">
                <a href="usuario.do?metodo=novo" class="btn btn-primary"><i class="fa-solid fa-plus"></i> Novo Usuário</a>
                <a href="usuario.do?metodo=voltar" class="btn btn-light"><i class="fa-solid fa-house"></i> Voltar</a>
            </div>

            <% if (usuarios != null && !usuarios.isEmpty()) { %>
                <div style="overflow-x: auto;">
                    <table>
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Nome</th>
                                <th>Matrícula</th>
                                <th>Email</th>
                                <th>Tipo</th>
                                <th>Ações</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% for (Usuario u : usuarios) { %>
                            <tr>
                                <td><%= u.getId() %></td>
                                <td><strong><%= u.getNome() %></strong></td>
                                <td><%= u.getMatricula() %></td>
                                <td><%= u.getEmail() %></td>
                                <td>
                                    <% 
                                        String tipoClass = "tipo-aluno";
                                        if ("PROFESSOR".equals(u.getTipo())) {
                                            tipoClass = "tipo-professor";
                                        } else if ("BOLSISTA".equals(u.getTipo())) {
                                            tipoClass = "tipo-bolsista";
                                        }
                                    %>
                                    <span class="usuario-tipo <%= tipoClass %>"><%= u.getTipo() %></span>
                                </td>
                                <td>
                                    <a href="usuario.do?metodo=editar&id=<%= u.getId() %>" class="btn btn-small btn-warning"><i class="fa-solid fa-pen"></i> Editar</a>
                                    <a href="usuario.do?metodo=excluir&id=<%= u.getId() %>" class="btn btn-small btn-danger" onclick="return confirm('Tem certeza?')"><i class="fa-solid fa-trash"></i> Excluir</a>
                                </td>
                            </tr>
                            <% } %>
                        </tbody>
                    </table>
                </div>
            <% } else { %>
                <div class="empty-state">
                    <p>Nenhum usuário cadastrado ainda.</p>
                    <a href="usuario.do?metodo=novo" class="btn btn-primary"><i class="fa-solid fa-plus"></i> Cadastrar Primeiro Usuário</a>
                </div>
            <% } %>
        </div>
    </div>
</body>
</html>