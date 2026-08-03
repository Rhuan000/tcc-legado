<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="tcc.legado.model.Usuario" %>
<%
    Usuario usuario = (Usuario) request.getAttribute("usuario");
    boolean editando = (usuario != null && usuario.getId() != null);
    String titulo = editando ? "Editar Usuário" : "Cadastrar Usuário";
    String submitText = editando ? "Atualizar" : "Cadastrar";
%>
<html>
<head>
    <meta charset="UTF-8">
    <title><%= titulo %></title>
    <link rel="stylesheet" href="../styles.css">
    <link rel="stylesheet" href="styles.css">
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>? <%= titulo %></h1>
            <p class="subtitle">Cadastro e edição de usuários</p>
        </div>

        <div class="content">
            <form action="usuario.do" method="post" class="usuario-form">
                <input type="hidden" name="metodo" value="salvar" />
                <% if (editando) { %>
                    <input type="hidden" name="id" value="<%= usuario.getId() %>" />
                <% } %>

                <div class="form-group">
                    <label for="nome">Nome *</label>
                    <input type="text" id="nome" name="nome" class="form-control" required value="<%= editando ? usuario.getNome() : "" %>" />
                </div>
                <div class="form-group">
                    <label for="matricula">Matrícula *</label>
                    <input type="text" id="matricula" name="matricula" class="form-control" required value="<%= editando ? usuario.getMatricula() : "" %>" <%= editando ? "readonly" : "" %> />
                    <% if (editando) { %><small class="form-hint">A matrícula não pode ser alterada</small><% } %>
                </div>
                <div class="form-group">
                    <label for="email">Email</label>
                    <input type="email" id="email" name="email" class="form-control" value="<%= editando ? usuario.getEmail() : "" %>" />
                </div>
                <div class="form-group">
                    <label for="tipo">Tipo</label>
                    <select id="tipo" name="tipo" class="form-control">
                        <option value="ALUNO" <%= editando && "ALUNO".equals(usuario.getTipo()) ? "selected" : "" %>>Aluno</option>
                        <option value="PROFESSOR" <%= editando && "PROFESSOR".equals(usuario.getTipo()) ? "selected" : "" %>>Professor</option>
                        <option value="BOLSISTA" <%= editando && "BOLSISTA".equals(usuario.getTipo()) ? "selected" : "" %>>Bolsista</option>
                    </select>
                </div>

                <div class="form-actions">
                    <button type="submit" class="btn btn-success"><%= submitText %></button>
                    <a href="usuario.do?metodo=listar" class="btn btn-light">Cancelar</a>
                </div>
            </form>
        </div>
    </div>
</body>
</html>