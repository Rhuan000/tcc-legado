<%@ page import="java.util.List, tcc.legado.model.Emprestimo" %>
<%
    List<Emprestimo> emprestimos = (List<Emprestimo>) request.getAttribute("emprestimos");
%>
<html>
<head><title>Devolução</title></head>
<body>
    <h1>📥 Registrar Devolução</h1>
    <form action="emprestimo.do?metodo=devolver" method="post">
        <label>Empréstimo:</label>
        <select name="idEmprestimo">
            <% for (Emprestimo e : emprestimos) { %>
                <option value="<%= e.getId() %>">ID <%= e.getId() %> - Livro <%= e.getIdLivro() %></option>
            <% } %>
        </select><br/>
        <input type="submit" value="Devolver"/>
    </form>
    <a href="emprestimo.do?metodo=listar">🔙 Voltar</a>
</body>
</html>
