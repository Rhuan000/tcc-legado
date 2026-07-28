<%@ page import="tcc.legado.model.Usuario" %>
<%
    Usuario usuario = (Usuario) request.getAttribute("usuario");
    boolean editando = (usuario != null && usuario.getId() != null);
%>
<html>
<head><title><%= editando ? "Editar" : "Cadastrar" %> Usuário</title></head>
<body>
    <h1><%= editando ? "✏️ Editar" : "📝 Cadastrar" %> Usuário</h1>
    <form action="usuario.do" method="post">
        <input type="hidden" name="metodo" value="salvar" />
        <% if (editando) { %>
            <input type="hidden" name="id" value="<%= usuario.getId() %>" />
        <% } %>
        <label>Nome:</label>
        <input type="text" name="nome" value="<%= editando ? usuario.getNome() : "" %>" required/><br/>
        <label>Matrícula:</label>
        <input type="text" name="matricula" value="<%= editando ? usuario.getMatricula() : "" %>" <%= editando ? "readonly" : "" %> required/><br/>
        <label>Email:</label>
        <input type="email" name="email" value="<%= editando ? usuario.getEmail() : "" %>"/><br/>
        <label>Tipo:</label>
        <select name="tipo">
            <option value="ALUNO" <%= editando && "ALUNO".equals(usuario.getTipo()) ? "selected" : "" %>>Aluno</option>
            <option value="PROFESSOR" <%= editando && "PROFESSOR".equals(usuario.getTipo()) ? "selected" : "" %>>Professor</option>
            <option value="BOLSISTA" <%= editando && "BOLSISTA".equals(usuario.getTipo()) ? "selected" : "" %>>Bolsista</option>
        </select><br/>
        <input type="submit" value="<%= editando ? "Atualizar" : "Cadastrar" %>" />
    </form>
    <a href="usuario.do?metodo=listar">🔙 Voltar</a>
</body>
</html>
