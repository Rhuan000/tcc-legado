package tcc.legado.dao;

import tcc.legado.model.Livro;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.Dependent;

@Dependent
public class LivroDAO {
    
    // Conexão hardcoded (isso é propositalmente ruim para gerar acoplamento)
    private Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return DriverManager.getConnection(
        "jdbc:postgresql://localhost:5432/biblioteca", "postgres", "postgres");
    }

    public List<Livro> listarTodos() {
        List<Livro> livros = new ArrayList<>();
        String sql = "SELECT * FROM livro ORDER BY titulo";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Livro l = new Livro();
                l.setId(rs.getLong("id"));
                l.setTitulo(rs.getString("titulo"));
                l.setAutor(rs.getString("autor"));
                l.setIsbn(rs.getString("isbn"));
                l.setAno(rs.getInt("ano"));
                l.setEditora(rs.getString("editora"));
                l.setQuantidade(rs.getInt("quantidade"));
                livros.add(l);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return livros;
    }

    public Livro buscarPorId(Long id) {
        String sql = "SELECT * FROM livro WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Livro l = new Livro();
                l.setId(rs.getLong("id"));
                l.setTitulo(rs.getString("titulo"));
                l.setAutor(rs.getString("autor"));
                l.setIsbn(rs.getString("isbn"));
                l.setAno(rs.getInt("ano"));
                l.setEditora(rs.getString("editora"));
                l.setQuantidade(rs.getInt("quantidade"));
                return l;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void salvar(Livro livro) {
        String sql = "INSERT INTO livro (titulo, autor, isbn, ano, editora, quantidade) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, livro.getTitulo());
            ps.setString(2, livro.getAutor());
            ps.setString(3, livro.getIsbn());
            ps.setInt(4, livro.getAno());
            ps.setString(5, livro.getEditora());
            ps.setInt(6, livro.getQuantidade());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                livro.setId(rs.getLong(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void atualizar(Livro livro) {
        String sql = "UPDATE livro SET titulo=?, autor=?, isbn=?, ano=?, editora=?, quantidade=? WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, livro.getTitulo());
            ps.setString(2, livro.getAutor());
            ps.setString(3, livro.getIsbn());
            ps.setInt(4, livro.getAno());
            ps.setString(5, livro.getEditora());
            ps.setInt(6, livro.getQuantidade());
            ps.setLong(7, livro.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
