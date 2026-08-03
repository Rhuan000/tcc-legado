<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, tcc.legado.model.Emprestimo" %>
<%
    List<Emprestimo> lista = (List<Emprestimo>) request.getAttribute("lista");
    String mensagem = (String) request.getAttribute("mensagem");
%>
<html>
<head>
    <meta charset="UTF-8">
    <title>Empréstimos</title>
    <link rel="stylesheet" href="../styles.css">
    <link rel="stylesheet" href="styles.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/webjars/font-awesome/6.5.2/css/all.min.css">
</head>
<body>
    <div class="container">
        <div class="header">
            <h1><i class="fa-solid fa-arrow-right-arrow-left page-icon"></i>Gerenciamento de Empréstimos</h1>
            <p class="subtitle">Controle de empréstimos, devoluções e multas</p>
        </div>

        <% if (mensagem != null) { %>
            <div class="alert alert-success"><%= mensagem %></div>
        <% } %>

        <div class="content">
            <div class="action-buttons">
                <a href="emprestimo.do?metodo=novo" class="btn btn-primary"><i class="fa-solid fa-plus"></i> Novo Empréstimo</a>
                <a href="emprestimo.do?metodo=devolver" class="btn btn-secondary"><i class="fa-solid fa-rotate-left"></i> Registrar Devolução</a>
                <a href="index.jsp" class="btn btn-light"><i class="fa-solid fa-house"></i> Voltar</a>
            </div>

            <% if (lista != null && !lista.isEmpty()) { %>
                <div class="emprestimo-table">
                    <table>
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Livro</th>
                                <th>Usuário</th>
                                <th>Data Empréstimo</th>
                                <th>Previsão Devolução</th>
                                <th>Devolução Real</th>
                                <th>Multa (R$)</th>
                                <th>Ações</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% for (Emprestimo e : lista) { 
                                boolean devolvido = e.getDataDevolucaoReal() != null;
                            %>
                            <tr>
                                <td><%= e.getId() %></td>
                                <td><%= e.getIdLivro() %></td>
                                <td><%= e.getIdUsuario() %></td>
                                <td><%= e.getDataEmprestimo() %></td>
                                <td><%= e.getDataPrevistaDevolucao() %></td>
                                <td>
                                    <% if (devolvido) { %>
                                        <%= e.getDataDevolucaoReal() %>
                                    <% } else { %>
                                        <span class="badge badge-active">Pendente</span>
                                    <% } %>
                                </td>
                                <td>
                                    <% if (e.getMulta() != null && e.getMulta() > 0) { %>
                                        <span class="badge badge-discount">R$ <%= String.format("%.2f", e.getMulta()) %></span>
                                    <% } else { %>
                                        <span>-</span>
                                    <% } %>
                                </td>
                                <td>
                                    <a href="emprestimo.do?metodo=devolver&id=<%= e.getId() %>" class="btn btn-small btn-info"><i class="fa-solid fa-circle-info"></i> Detalhes</a>
                                </td>
                            </tr>
                            <% } %>
                        </tbody>
                    </table>
                </div>
            <% } else { %>
                <div class="empty-state">
                    <p>Nenhum empréstimo cadastrado ainda.</p>
                    <a href="emprestimo.do?metodo=novo" class="btn btn-primary"><i class="fa-solid fa-plus"></i> Fazer Primeiro Empréstimo</a>
                </div>
            <% } %>
        </div>
    </div>
</body>
</html>