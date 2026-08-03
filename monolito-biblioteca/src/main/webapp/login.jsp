<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String erro = (String) request.getAttribute("erro");
%>
<html>
<head>
    <meta charset="UTF-8">
    <title>Login - Biblioteca Legada</title>
    <link rel="stylesheet" href="styles.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/webjars/font-awesome/6.5.2/css/all.min.css">
</head>
<body class="login-page">
    <div class="login-shell">
        <div class="login-card">
            <div class="login-brand">
                <div class="login-icon"><i class="fa-solid fa-book-open-reader"></i></div>
                <h1>Biblioteca Legada</h1>
                <p>Reestruturação de sistemas legados para microsserviços</p>
            </div>

            <form action="auth.do?metodo=login" method="post" class="login-form">
                <div class="form-group">
                    <label for="matricula">Matrícula</label>
                    <input id="matricula" type="text" name="matricula" required class="form-control" placeholder="Digite sua matrícula" />
                </div>

                <div class="form-group">
                    <label for="senha">Senha</label>
                    <div class="password-field">
                        <input id="senha" type="password" name="senha" required class="form-control" placeholder="Digite sua senha" />
                        <button type="button" class="toggle-password" onclick="togglePassword()" aria-label="Mostrar ou ocultar senha">
                            <i class="fa-solid fa-eye"></i>
                        </button>
                    </div>
                </div>

                <button type="submit" class="btn btn-primary btn-full"><i class="fa-solid fa-right-to-bracket"></i> Entrar</button>

                <% if (erro != null) { %>
                    <div class="alert alert-error" style="margin-top:15px;"><%= erro %></div>
                <% } %>
            </form>
        </div>
    </div>

    <script>
        function togglePassword() {
            var senha = document.getElementById('senha');
            var icon = document.querySelector('.toggle-password i');
            var visible = senha.type === 'password';
            senha.type = visible ? 'text' : 'password';
            icon.className = visible ? 'fa-solid fa-eye-slash' : 'fa-solid fa-eye';
        }
    </script>
</body>
</html>