<%@ page import="java.util.List, tcc.legado.model.Livro, tcc.legado.model.Usuario" %>
<%
    List<Livro> livros = (List<Livro>) request.getAttribute("livros");
    List<Usuario> usuarios = (List<Usuario>) request.getAttribute("usuarios");
%>
<html>
<head>
    <meta charset="UTF-8">
    <title>Novo Empréstimo</title>
    <link rel="stylesheet" href="../styles.css">
    <link rel="stylesheet" href="styles.css">
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>? Realizar Empréstimo</h1>
            <p class="subtitle">Associe um livro a um usuário pela matrícula</p>
        </div>

        <div class="content">
            <form action="emprestimo.do?metodo=realizar" method="post" class="form-container">
                <div class="form-group">
                    <label for="idLivro">Livro</label>
                    <select id="idLivro" name="idLivro" class="form-control" required>
                        <option value="">Selecione um livro...</option>
                        <% if (livros != null) { for (Livro l : livros) { %>
                            <option value="<%= l.getId() %>"><%= l.getTitulo() %></option>
                        <% } } %>
                    </select>
                </div>

                <div class="form-group">
                    <label for="matricula">Usuário</label>
                    <input type="text" id="matricula" name="matricula" class="form-control" list="usuarios-lista" autocomplete="off" placeholder="Digite a matrícula" required />
                    <datalist id="usuarios-lista">
                        <% if (usuarios != null) { for (Usuario u : usuarios) { %>
                            <option value="<%= u.getMatricula() %>"><%= u.getNome() %></option>
                        <% } } %>
                    </datalist>
                    <small class="form-hint">Digite a matrícula e selecione o usuário correspondente.</small>
                    <div id="usuario-preview" class="alert alert-info" style="display:none; margin-top:10px;"></div>
                </div>

                <div class="form-actions">
                    <button type="submit" class="btn btn-success">Emprestar</button>
                    <a href="emprestimo.do?metodo=listar" class="btn btn-light">Cancelar</a>
                </div>
            </form>
        </div>
    </div>

    <script>
        (function () {
            var usuarios = [
                <% if (usuarios != null) {
                    for (int i = 0; i < usuarios.size(); i++) {
                        Usuario u = usuarios.get(i);
                %>
                { matricula: '<%= u.getMatricula() %>', nome: '<%= u.getNome().replace("'", "\\'") %>', tipo: '<%= u.getTipo() %>' }<%= i < usuarios.size() - 1 ? "," : "" %>
                <%      }
                   }
                %>
            ];

            var input = document.getElementById('matricula');
            var preview = document.getElementById('usuario-preview');

            input.addEventListener('input', function () {
                var value = this.value.trim();
                if (!value) {
                    preview.style.display = 'none';
                    preview.textContent = '';
                    return;
                }
                var usuario = usuarios.find(function (u) { return u.matricula === value; });
                if (usuario) {
                    preview.style.display = 'block';
                    preview.textContent = 'Usuário encontrado: ' + usuario.nome + ' (' + usuario.tipo + ')';
                } else {
                    preview.style.display = 'block';
                    preview.textContent = 'Matrícula não localizada na lista carregada.';
                }
            });
        })();
    </script>
</body>
</html>