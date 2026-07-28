<%@ page import="tcc.legado.model.UsuarioAuth" %>
<%
    UsuarioAuth user = (UsuarioAuth) session.getAttribute("usuarioLogado");
%>
<html>
<head><title>Biblioteca Legada</title></head>
<body>
    <h1>🏛️ Sistema de Biblioteca Legado</h1>
    <p>Bem-vindo, <strong><%= user != null ? user.getMatricula() : "Visitante" %></strong> (Perfil: <%= user != null ? user.getPerfil() : "N/A" %>)</p>
    <ul>
        <li><a href="livro.do?metodo=listar">📚 Listar Livros</a></li>
        <li><a href="usuario.do?metodo=listar">👤 Listar Usuários</a></li>
        <li><a href="auth.do?metodo=logout">🚪 Sair</a></li>
    </ul>
</body>
</html>
