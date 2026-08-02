<%@ page import="java.util.List, tcc.legado.model.Emprestimo" %>
<%
    List<Emprestimo> lista = (List<Emprestimo>) request.getAttribute("lista");
    String mensagem = (String) request.getAttribute("mensagem");
%>
<html>
<head><title>Empréstimos</title></head>
<body>
    <h1>Empréstimos</h1>
    <% if (mensagem != null) { %>
        <p style="color:green;"><%= mensagem %></p>
    <% } %>
    <table border="1">
        <tr><th>ID</th><th>Livro</th><th>Usuário</th><th>Empréstimos</th><th>Previsão</th><th>Devolução</th><th>Multa</th></tr>
        <% for (Emprestimo e : lista) { %>
        <tr>
            <td><%= e.getId() %></td>
            <td><%= e.getIdLivro() %></td>
            <td><%= e.getIdUsuario() %></td>
            <td><%= e.getDataEmprestimo() %></td>
            <td><%= e.getDataPrevistaDevolucao() %></td>
            <td><%= e.getDataDevolucaoReal() != null ? e.getDataDevolucaoReal() : "Pendente" %></td>
            <td><%= e.getMulta() %></td>
        </tr>
        <% } %>
    </table>
    <a href="emprestimo.do?metodo=novo">Novo Empréstimos</a>
    <a href="emprestimo.do?metodo=devolver">Registrar Devolução</a>
    <a href="index.jsp">Voltar</a>
</body>
</html>
