package tcc.legado.dao;

import tcc.legado.model.Emprestimo;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.Date;

public class EmprestimoDAO {

    private Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return DriverManager.getConnection(
            "jdbc:postgresql://localhost:5432/biblioteca", "postgres", "postgres");
    }

    public void salvar(Emprestimo emprestimo) {
        String sql = "INSERT INTO emprestimo (id_livro, id_usuario, data_emprestimo, data_prevista_devolucao, multa) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, emprestimo.getIdLivro());
            ps.setLong(2, emprestimo.getIdUsuario());
            ps.setDate(3, new java.sql.Date(emprestimo.getDataEmprestimo().getTime()));
            ps.setDate(4, new java.sql.Date(emprestimo.getDataPrevistaDevolucao().getTime()));
            ps.setDouble(5, emprestimo.getMulta() != null ? emprestimo.getMulta() : 0.0);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                emprestimo.setId(rs.getLong(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void atualizarDevolucao(Long idEmprestimo, Date dataDevolucao, double multa) {
        String sql = "UPDATE emprestimo SET data_devolucao_real = ?, multa = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, new java.sql.Date(dataDevolucao.getTime()));
            ps.setDouble(2, multa);
            ps.setLong(3, idEmprestimo);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void atualizarMulta(Long idEmprestimo, double multa) {
        String sql = "UPDATE emprestimo SET multa = ? WHERE id = ? AND data_devolucao_real IS NULL";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, multa);
            ps.setLong(2, idEmprestimo);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Falha ao atualizar multa do emprestimo " + idEmprestimo, e);
        }
    }
}
