<%@ page import="java.util.List, tcc.legado.model.Usuario" %>
<%
    List<Usuario> usuarios = (List<Usuario>) request.getAttribute("lista");
    String mensagem = (String) request.getAttribute("mensagem");
%>
<html>
<head><title>Lista de Usuários</title></head>
<body>
    <h1>Usuários</h1>
    <% if (mensagem != null) { %>
        <p style="color:green;"><%= mensagem %></p>
    <% } %>
    <table border="1">
       	<tr><th>ID</th><th>Nome</th><th>Matrícula</th><th>Email</th><th>Tipo</th><th>Ações</th></tr>
        <%
            for (Usuario u : usuarios) {
        %>
        <tr>
            <td><%= u.getId() %></td>
            <td><%= u.getNome() %></td>
            <td><%= u.getMatricula() %></td>
            <td><%= u.getEmail() %></td>
            <td><%= u.getTipo() %></td>
            <td>
                <a href="usuario.do?metodo=editar&id=<%= u.getId() %>">Editar</a>
                <a href="usuario.do?metodo=excluir&id=<%= u.getId() %>">Excluir</a>
            </td>
        </tr>
        <%
            }
        %>
    </table>
    <a href="usuario.do?metodo=novo">Cadastrar</a>
    <a href="index.jsp">Voltar</a>
</body>
</html>
