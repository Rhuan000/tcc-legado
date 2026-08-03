package tcc.legado.dao;

import tcc.legado.model.LivroDestaque;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.Dependent;

/**
 * DAO para o domínio de Livros em Destaque/Promoções
 */
@Dependent
public class LivroDestaqueDAO {
    
    // Conexão hardcoded (mesmo padrão do projeto)
    private Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return DriverManager.getConnection(
        "jdbc:postgresql://localhost:5432/biblioteca", "postgres", "postgres");
    }

    public List<LivroDestaque> listarTodos() {
        List<LivroDestaque> destaques = new ArrayList<>();
        String sql = "SELECT * FROM livro_destaque ORDER BY data_inicio DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                LivroDestaque ld = mapResultSetToLivroDestaque(rs);
                destaques.add(ld);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return destaques;
    }

    public List<LivroDestaque> listarAtivos() {
        List<LivroDestaque> destaques = new ArrayList<>();
        String sql = "SELECT * FROM livro_destaque WHERE ativo = true ORDER BY data_inicio DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                LivroDestaque ld = mapResultSetToLivroDestaque(rs);
                destaques.add(ld);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return destaques;
    }

    public LivroDestaque buscarPorId(Long id) {
        String sql = "SELECT * FROM livro_destaque WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToLivroDestaque(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void salvar(LivroDestaque livroDestaque) {
        String sql = "INSERT INTO livro_destaque (id_livro, titulo, descricao, desconto, categoria, data_inicio, data_fim, ativo, visualizacoes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, livroDestaque.getIdLivro());
            ps.setString(2, livroDestaque.getTitulo());
            ps.setString(3, livroDestaque.getDescricao());
            ps.setDouble(4, livroDestaque.getDesconto() != null ? livroDestaque.getDesconto() : 0);
            ps.setString(5, livroDestaque.getCategoria());
            ps.setDate(6, livroDestaque.getDataInicio() != null ? new java.sql.Date(livroDestaque.getDataInicio().getTime()) : null);
            ps.setDate(7, livroDestaque.getDataFim() != null ? new java.sql.Date(livroDestaque.getDataFim().getTime()) : null);
            ps.setBoolean(8, livroDestaque.getAtivo() != null ? livroDestaque.getAtivo() : true);
            ps.setInt(9, livroDestaque.getVisualizacoes() != null ? livroDestaque.getVisualizacoes() : 0);
            ps.executeUpdate();
            
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                livroDestaque.setId(rs.getLong(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void atualizar(LivroDestaque livroDestaque) {
        String sql = "UPDATE livro_destaque SET id_livro=?, titulo=?, descricao=?, desconto=?, categoria=?, " +
                     "data_inicio=?, data_fim=?, ativo=? WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, livroDestaque.getIdLivro());
            ps.setString(2, livroDestaque.getTitulo());
            ps.setString(3, livroDestaque.getDescricao());
            ps.setDouble(4, livroDestaque.getDesconto() != null ? livroDestaque.getDesconto() : 0);
            ps.setString(5, livroDestaque.getCategoria());
            ps.setDate(6, livroDestaque.getDataInicio() != null ? new java.sql.Date(livroDestaque.getDataInicio().getTime()) : null);
            ps.setDate(7, livroDestaque.getDataFim() != null ? new java.sql.Date(livroDestaque.getDataFim().getTime()) : null);
            ps.setBoolean(8, livroDestaque.getAtivo() != null ? livroDestaque.getAtivo() : true);
            ps.setLong(9, livroDestaque.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void excluir(Long id) {
        String sql = "DELETE FROM livro_destaque WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void incrementarVisualizacoes(Long id) {
        String sql = "UPDATE livro_destaque SET visualizacoes = visualizacoes + 1 WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private LivroDestaque mapResultSetToLivroDestaque(ResultSet rs) throws SQLException {
        LivroDestaque ld = new LivroDestaque();
        ld.setId(rs.getLong("id"));
        ld.setIdLivro(rs.getLong("id_livro"));
        ld.setTitulo(rs.getString("titulo"));
        ld.setDescricao(rs.getString("descricao"));
        ld.setDesconto(rs.getDouble("desconto"));
        ld.setCategoria(rs.getString("categoria"));

        Date inicio = rs.getDate("data_inicio");
        if (inicio != null) {
            ld.setDataInicio(new java.util.Date(inicio.getTime()));
        }
        Date fim = rs.getDate("data_fim");
        if (fim != null) {
            ld.setDataFim(new java.util.Date(fim.getTime()));
        }

        ld.setAtivo(rs.getBoolean("ativo"));
        ld.setVisualizacoes(rs.getInt("visualizacoes"));
        return ld;
    }
}