package tcc.legado.dao;

import tcc.legado.model.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {
    
    private Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return DriverManager.getConnection(
            "jdbc:postgresql://localhost:5432/biblioteca", "postgres", "postgres");
    }

    public List<Usuario> listarTodos() {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT * FROM usuario ORDER BY nome";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Usuario u = new Usuario();
                u.setId(rs.getLong("id"));
                u.setNome(rs.getString("nome"));
                u.setMatricula(rs.getString("matricula"));
                u.setEmail(rs.getString("email"));
                u.setTipo(rs.getString("tipo"));
                usuarios.add(u);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return usuarios;
    }

    public Usuario buscarPorMatricula(String matricula) {
        String sql = "SELECT * FROM usuario WHERE matricula = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, matricula);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Usuario u = new Usuario();
                u.setId(rs.getLong("id"));
                u.setNome(rs.getString("nome"));
                u.setMatricula(rs.getString("matricula"));
                u.setEmail(rs.getString("email"));
                u.setTipo(rs.getString("tipo"));
                return u;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Usuario buscarPorId(Long id) {
        String sql = "SELECT * FROM usuario WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Usuario u = new Usuario();
                u.setId(rs.getLong("id"));
                u.setNome(rs.getString("nome"));
                u.setMatricula(rs.getString("matricula"));
                u.setEmail(rs.getString("email"));
                u.setTipo(rs.getString("tipo"));
                return u;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void salvar(Usuario usuario, String senhaHash) {
        String sql = "INSERT INTO usuario (nome, matricula, email, tipo) VALUES (?, ?, ?, ?)";
        try (Connection conexao = getConnection()) {
            conexao.setAutoCommit(false);
            try (PreparedStatement cadastro = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement credencial = conexao.prepareStatement(
                         "INSERT INTO usuario_auth (matricula, senha_hash, perfil) VALUES (?, ?, 'CONSULTA')")) {
                cadastro.setString(1, usuario.getNome());
                cadastro.setString(2, usuario.getMatricula());
                cadastro.setString(3, usuario.getEmail());
                cadastro.setString(4, usuario.getTipo());
                cadastro.executeUpdate();
                long idGerado;
                try (ResultSet resultado = cadastro.getGeneratedKeys()) {
                    if (!resultado.next()) {
                        throw new SQLException("Cadastro não retornou o ID do usuário.");
                    }
                    idGerado = resultado.getLong(1);
                }
                credencial.setString(1, usuario.getMatricula());
                credencial.setString(2, senhaHash);
                credencial.executeUpdate();
                conexao.commit();
                usuario.setId(idGerado);
            } catch (SQLException falha) {
                try {
                    conexao.rollback();
                } catch (SQLException falhaRollback) {
                    falha.addSuppressed(falhaRollback);
                }
                if ("23505".equals(falha.getSQLState())) {
                    throw new IllegalArgumentException("Matrícula já cadastrada", falha);
                }
                throw falha;
            }
        } catch (SQLException falha) {
            throw new IllegalStateException("Não foi possível cadastrar o usuário e suas credenciais.", falha);
        }
    }

    public void atualizar(Usuario usuario) {
        String sql = "UPDATE usuario SET nome=?, email=?, tipo=? WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario.getNome());
            ps.setString(2, usuario.getEmail());
            ps.setString(3, usuario.getTipo());
            ps.setLong(4, usuario.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void excluir(Long id) {
        String sql = "DELETE FROM usuario WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
