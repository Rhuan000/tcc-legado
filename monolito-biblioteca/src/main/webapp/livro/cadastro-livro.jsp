<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="tcc.legado.model.Livro" %>
<%
    Livro livro = (Livro) request.getAttribute("livro");
    boolean editando = (livro != null && livro.getId() != null);
%>
<html>
<head><title><%= editando ? "Editar" : "Cadastrar" %> Livro</title></head>
<body>
    <h1><%= editando ? "✏️ Editar" : "📖 Cadastrar" %> Livro</h1>
    <form action="livro.do" method="post">
        <input type="hidden" name="metodo" value="<%= editando ? "salvar" : "salvar" %>" />
        <% if (editando) { %>
            <input type="hidden" name="id" value="<%= livro.getId() %>" />
        <% } %>
        <label>Título:</label>
        <input type="text" name="titulo" value="<%= editando ? livro.getTitulo() : "" %>" required/><br/>
        <label>Autor:</label>
        <input type="text" name="autor" value="<%= editando ? livro.getAutor() : "" %>"/><br/>
        <label>ISBN:</label>
        <input type="text" name="isbn" value="<%= editando ? livro.getIsbn() : "" %>"/><br/>
        <label>Ano:</label>
        <input type="text" name="ano" value="<%= editando ? livro.getAno() : "" %>"/><br/>
        <label>Editora:</label>
        <input type="text" name="editora" value="<%= editando ? livro.getEditora() : "" %>"/><br/>
        <label>Quantidade:</label>
        <input type="text" name="quantidade" value="<%= editando ? livro.getQuantidade() : "" %>"/><br/>
        <input type="submit" value="<%= editando ? "Atualizar" : "Cadastrar" %>" />
    </form>
    <a href="livro.do?metodo=listar">Voltar para lista</a>
</body>
</html>
