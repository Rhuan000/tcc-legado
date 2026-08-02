package tcc.legado.dao;

import tcc.legado.model.Emprestimo;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;

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

    public List<Emprestimo> listarTodos() {
        List<Emprestimo> lista = new ArrayList<>();
        String sql = "SELECT * FROM emprestimo ORDER BY id";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Emprestimo e = new Emprestimo();
                e.setId(rs.getLong("id"));
                e.setIdLivro(rs.getLong("id_livro"));
                e.setIdUsuario(rs.getLong("id_usuario"));
                e.setDataEmprestimo(rs.getDate("data_emprestimo"));
                e.setDataPrevistaDevolucao(rs.getDate("data_prevista_devolucao"));
                e.setDataDevolucaoReal(rs.getDate("data_devolucao_real"));
                e.setMulta(rs.getDouble("multa"));
                lista.add(e);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return lista;
    }

    public Emprestimo buscarPorId(Long id) {
        String sql = "SELECT * FROM emprestimo WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Emprestimo e = new Emprestimo();
                e.setId(rs.getLong("id"));
                e.setIdLivro(rs.getLong("id_livro"));
                e.setIdUsuario(rs.getLong("id_usuario"));
                e.setDataEmprestimo(rs.getDate("data_emprestimo"));
                e.setDataPrevistaDevolucao(rs.getDate("data_prevista_devolucao"));
                e.setDataDevolucaoReal(rs.getDate("data_devolucao_real"));
                e.setMulta(rs.getDouble("multa"));
                return e;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public List<Emprestimo> buscarAtrasados() {
        List<Emprestimo> atrasados = new ArrayList<>();
        String sql = "SELECT * FROM emprestimo WHERE data_devolucao_real IS NULL AND data_prevista_devolucao < CURRENT_DATE";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Emprestimo e = new Emprestimo();
                e.setId(rs.getLong("id"));
                e.setIdLivro(rs.getLong("id_livro"));
                e.setIdUsuario(rs.getLong("id_usuario"));
                e.setDataEmprestimo(rs.getDate("data_emprestimo"));
                e.setDataPrevistaDevolucao(rs.getDate("data_prevista_devolucao"));
                e.setDataDevolucaoReal(rs.getDate("data_devolucao_real"));
                e.setMulta(rs.getDouble("multa"));
                atrasados.add(e);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return atrasados;
    }
}
