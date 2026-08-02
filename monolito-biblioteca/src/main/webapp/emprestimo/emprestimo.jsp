<%@ page import="java.util.List, tcc.legado.model.Livro, tcc.legado.model.Usuario" %>
<%
    List<Livro> livros = (List<Livro>) request.getAttribute("livros");
    List<Usuario> usuarios = (List<Usuario>) request.getAttribute("usuarios");
%>
<html>
<head><title>Novo Empréstimo</title></head>
<body>
    <h1>Realizar Empréstimo</h1>
    <form action="emprestimo.do?metodo=realizar" method="post">
        <label>Livro:</label>
        <select name="idLivro">
            <% for (Livro l : livros) { %>
                <option value="<%= l.getId() %>"><%= l.getTitulo() %></option>
            <% } %>
        </select><br/>
        <label>Usuário (Matrícula):</label>
        <select name="matricula">
            <% for (Usuario u : usuarios) { %>
                <option value="<%= u.getMatricula() %>"><%= u.getNome() %></option>
            <% } %>
        </select><br/>
        <input type="submit" value="Emprestar"/>
    </form>
    <a href="emprestimo.do?metodo=listar">Voltar</a>
</body>
</html>
