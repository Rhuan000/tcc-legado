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
import tcc.moderno.emprestimo.dtos.ContagemEmprestimosDTO;

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

    public void atualizarMulta(long id, BigDecimal multa) {
        try (var c = dataSource.getConnection(); var s = c.prepareStatement("""
                UPDATE emprestimo SET multa=? WHERE id=? AND data_devolucao_real IS NULL
                AND NOT EXISTS (SELECT 1 FROM emprestimo_operacao o
                                WHERE o.id_emprestimo=emprestimo.id AND o.tipo='DEVOLVER' AND o.estado='PENDENTE')
                """)) {
            s.setBigDecimal(1, multa); s.setLong(2, id); s.executeUpdate();
        } catch (SQLException e) { throw new IllegalStateException("Falha ao atualizar multa", e); }
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

    public List<ContagemEmprestimosDTO> buscarMaisEmprestadosNoMes(int limite) {
        String sql = """
                SELECT id_livro, COUNT(*) AS total_emprestimos
                FROM emprestimo
                WHERE data_emprestimo >= date_trunc('month', CURRENT_DATE)::date
                  AND data_emprestimo < (date_trunc('month', CURRENT_DATE) + INTERVAL '1 month')::date
                GROUP BY id_livro
                ORDER BY total_emprestimos DESC, id_livro ASC
                LIMIT ?
                """;
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limite);
            try (var result = statement.executeQuery()) {
                List<ContagemEmprestimosDTO> contagens = new ArrayList<>();
                while (result.next()) {
                    contagens.add(new ContagemEmprestimosDTO(
                            result.getLong("id_livro"), result.getLong("total_emprestimos")));
                }
                return contagens;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Falha ao consultar contagem mensal de emprestimos", e);
        }
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
