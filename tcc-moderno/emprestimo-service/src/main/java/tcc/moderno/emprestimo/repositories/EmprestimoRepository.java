package tcc.moderno.emprestimo.repositories;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import tcc.moderno.emprestimo.models.Emprestimo;

@ApplicationScoped
public class EmprestimoRepository {

    private static final String SELECT = """
            SELECT id, id_livro, id_usuario, data_emprestimo,
                   data_prevista_devolucao, data_devolucao_real, multa
            FROM emprestimo
            """;

    private final DataSource dataSource;

    @Inject
    public EmprestimoRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Emprestimo> listarTodos() {
        return consultar(SELECT + " ORDER BY id");
    }

    public Optional<Emprestimo> buscarPorId(long id) {
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(SELECT + " WHERE id = ?")) {
            statement.setLong(1, id);
            try (var result = statement.executeQuery()) {
                return result.next() ? Optional.of(mapear(result)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Falha ao consultar emprestimo", e);
        }
    }

    public List<Emprestimo> buscarAtrasados() {
        return consultar(SELECT + """
                 WHERE data_devolucao_real IS NULL
                   AND data_prevista_devolucao < CURRENT_DATE
                 ORDER BY id
                """);
    }

    private List<Emprestimo> consultar(String sql) {
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(sql);
             var result = statement.executeQuery()) {
            List<Emprestimo> emprestimos = new ArrayList<>();
            while (result.next()) {
                emprestimos.add(mapear(result));
            }
            return emprestimos;
        } catch (SQLException e) {
            throw new IllegalStateException("Falha ao consultar emprestimos", e);
        }
    }

    private Emprestimo mapear(ResultSet result) throws SQLException {
        BigDecimal multa = result.getBigDecimal("multa");
        // O DAO legado devolve zero para multa SQL NULL (ResultSet.getDouble).
        return new Emprestimo(result.getLong("id"), result.getLong("id_livro"),
                result.getLong("id_usuario"), result.getObject("data_emprestimo", LocalDate.class),
                result.getObject("data_prevista_devolucao", LocalDate.class),
                result.getObject("data_devolucao_real", LocalDate.class),
                multa == null ? BigDecimal.ZERO : multa);
    }
}
