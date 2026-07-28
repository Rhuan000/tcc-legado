<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, tcc.legado.model.Livro" %>
<%
    List<Livro> livros = (List<Livro>) request.getAttribute("lista");
    String mensagem = (String) request.getAttribute("mensagem");
%>
<html>
<head><title>Lista de Livros</title></head>
<body>
    <h1>📚 Lista de Livros</h1>
    <% if (mensagem != null) { %>
        <p style="color:green;"><%= mensagem %></p>
    <% } %>
    <table border="1">
        <tr><th>ID</th><th>Título</th><th>Autor</th><th>ISBN</th><th>Ano</th><th>Editora</th><th>Qtd</th></tr>
        <%
            if (livros != null) {
                for (Livro l : livros) {
        %>
        <tr>
            <td><%= l.getId() %></td>
            <td><%= l.getTitulo() %></td>
            <td><%= l.getAutor() %></td>
            <td><%= l.getIsbn() %></td>
            <td><%= l.getAno() %></td>
            <td><%= l.getEditora() %></td>
            <td><%= l.getQuantidade() %></td>
        </tr>
        <%
                }
            }
        %>
    </table>
    <br/>
    <a href="livro.do?metodo=novo">Cadastrar novo livro</a> |
    <a href="index.jsp">Voltar</a>
</body>
</html>
