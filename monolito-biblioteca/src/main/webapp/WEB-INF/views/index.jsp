<%@ page import="tcc.legado.model.UsuarioAuth" %>
<%
    UsuarioAuth user = (UsuarioAuth) session.getAttribute("usuarioLogado");
%>
<html>
<head>
    <meta charset="UTF-8">
    <title>Biblioteca Legada - Home</title>
    <link rel="stylesheet" href="styles.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/webjars/font-awesome/6.5.2/css/all.min.css">
</head>
<body>
    <div class="container">
        <div class="header">
            <h1><i class="fa-solid fa-book-open page-icon"></i>Biblioteca Legada</h1>
            <p class="subtitle">Bem-vindo, <strong><%= user != null ? user.getMatricula() : "Visitante" %></strong> (Perfil: <%= user != null ? user.getPerfil() : "N/A" %>)</p>
        </div>

        <div class="content">
            <div style="margin-bottom: 30px;">
                <h2 style="color: #2c3e50; margin-bottom: 20px;">Domínios do Sistema</h2>
            </div>

            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 20px; margin-bottom: 40px;">
                <div style="background: white; padding: 25px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); border-top: 4px solid #16a085;">
                    <h3 style="color: #16a085; margin-bottom: 15px;"><i class="fa-solid fa-book page-icon"></i>Domínio: Livros</h3>
                    <p style="color: #7f8c8d; margin-bottom: 15px;">Gerenciamento do acervo da biblioteca.</p>
                    <a href="livro.do?metodo=listar" class="btn btn-primary" style="display: block; text-align: center; margin: 0;"><i class="fa-solid fa-rectangle-list"></i> Gerenciar Livros</a>
                </div>

                <div style="background: white; padding: 25px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); border-top: 4px solid #c0392b;">
                    <h3 style="color: #c0392b; margin-bottom: 15px;"><i class="fa-solid fa-arrow-right-arrow-left page-icon"></i>Domínio: Empréstimos</h3>
                    <p style="color: #7f8c8d; margin-bottom: 15px;">Realização de empréstimos, devoluções e cálculo de multas.</p>
                    <a href="emprestimo.do?metodo=listar" class="btn btn-secondary" style="display: block; text-align: center; margin: 0;"><i class="fa-solid fa-hand-holding-book"></i> Gerenciar Empréstimos</a>
                </div>

                <div style="background: white; padding: 25px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); border-top: 4px solid #667eea;">
                    <h3 style="color: #667eea; margin-bottom: 15px;"><i class="fa-solid fa-star page-icon"></i>Domínio: Livros em Destaque</h3>
                    <p style="color: #7f8c8d; margin-bottom: 15px;">Promoções, lançamentos e recomendações especiais.</p>
                    <a href="destaque.do?metodo=listar" class="btn btn-info" style="display: block; text-align: center; margin: 0;"><i class="fa-solid fa-bullhorn"></i> Gerenciar Destaques</a>
                </div>

                <div style="background: white; padding: 25px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); border-top: 4px solid #2980b9;">
                    <h3 style="color: #2980b9; margin-bottom: 15px;"><i class="fa-solid fa-users page-icon"></i>Gerenciamento: Usuários</h3>
                    <p style="color: #7f8c8d; margin-bottom: 15px;">Cadastro e administração de usuários.</p>
                    <a href="usuario.do?metodo=listar" class="btn btn-primary" style="display: block; text-align: center; margin: 0;"><i class="fa-solid fa-user-gear"></i> Gerenciar Usuários</a>
                </div>
            </div>

            <div style="text-align: center; padding-top: 20px; border-top: 1px solid #ecf0f1;">
                <a href="auth.do?metodo=logout" class="btn btn-light"><i class="fa-solid fa-right-from-bracket"></i> Sair do Sistema</a>
            </div>
        </div>

        <div class="footer">
            <p>Sistema de Biblioteca Legada - Monolito preparado para migração em microsserviços</p>
        </div>
    </div>
</body>
</html>