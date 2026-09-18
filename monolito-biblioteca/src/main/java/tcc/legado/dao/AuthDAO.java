package tcc.legado.dao;

import tcc.legado.model.Usuario;
import tcc.legado.util.HashUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthDAO {

    private Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        // Conexão hardcoded – veneno proposital (mantido)
        return DriverManager.getConnection(
            "jdbc:postgresql://localhost:5432/biblioteca", "postgres", "postgres");
    }

    /**
     * Autentica o usuário comparando a senha com o hash armazenado.
     * Retorna um objeto Usuario com todos os dados de negócio se bem-sucedido.
     */
    public Usuario autenticar(String matricula, String senha) {
        // Aplica SHA-256 na senha digitada
        String senhaHash = HashUtil.sha256(senha);

        // JOIN entre usuario e usuario_auth para validar e obter perfil
        String sql = "SELECT u.id, u.nome, u.matricula, u.email, u.tipo, a.perfil " +
                     "FROM usuario u " +
                     "INNER JOIN usuario_auth a ON u.matricula = a.matricula " +
                     "WHERE u.matricula = ? AND a.senha_hash = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, matricula);
            ps.setString(2, senhaHash);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Usuario usuario = new Usuario();
                usuario.setId(rs.getLong("id"));
                usuario.setNome(rs.getString("nome"));
                usuario.setMatricula(rs.getString("matricula"));
                usuario.setEmail(rs.getString("email"));
                usuario.setTipo(rs.getString("tipo"));
                usuario.setPerfil(rs.getString("perfil"));
                return usuario;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
