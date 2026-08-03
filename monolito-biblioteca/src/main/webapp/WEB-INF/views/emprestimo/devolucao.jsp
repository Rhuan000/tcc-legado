<%@ page import="java.util.List, tcc.legado.model.Emprestimo" %>
<%
    List<Emprestimo> emprestimos = (List<Emprestimo>) request.getAttribute("emprestimos");
%>
<html>
<head>
    <meta charset="UTF-8">
    <title>Devolução</title>
    <link rel="stylesheet" href="../styles.css">
    <link rel="stylesheet" href="styles.css">
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>? Registrar Devolução</h1>
            <p class="subtitle">Selecione o empréstimo para registrar a devolução</p>
        </div>

        <div class="content">
            <form action="emprestimo.do?metodo=devolver" method="post" class="form-container">
                <div class="form-group">
                    <label for="idEmprestimo">Empréstimo</label>
                    <select id="idEmprestimo" name="idEmprestimo" class="form-control">
                        <% if (emprestimos != null) { for (Emprestimo e : emprestimos) { %>
                            <option value="<%= e.getId() %>">ID <%= e.getId() %> - Livro <%= e.getIdLivro() %></option>
                        <% } } %>
                    </select>
                </div>

                <div class="form-actions">
                    <button type="submit" class="btn btn-success">Devolver</button>
                    <a href="emprestimo.do?metodo=listar" class="btn btn-light">Cancelar</a>
                </div>
            </form>
        </div>
    </div>
</body>
</html>