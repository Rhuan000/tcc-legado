package tcc.moderno.emprestimo.repositories;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Optional;

import tcc.moderno.emprestimo.models.OperacaoEmprestimo;

@ApplicationScoped
public class OperacaoEmprestimoRepository {
    private final DataSource dataSource;
    private final ObjectMapper mapper;

    @Inject
    public OperacaoEmprestimoRepository(DataSource dataSource, ObjectMapper mapper) {
        this.dataSource = dataSource;
        this.mapper = mapper;
    }

    public Optional<OperacaoEmprestimo> buscar(String chave) {
        try (var c = dataSource.getConnection();
             var s = c.prepareStatement("SELECT dados, estado FROM emprestimo_operacao WHERE chave=?")) {
            s.setString(1, chave);
            try (var r = s.executeQuery()) { return r.next() ? Optional.of(mapear(r)) : Optional.empty(); }
        } catch (Exception e) { throw new IllegalStateException("Falha ao consultar operacao", e); }
    }

    public OperacaoEmprestimo registrar(OperacaoEmprestimo op) {
        try (var c = dataSource.getConnection()) {
            if (op.idEmprestimo == 0) {
                try (var s = c.createStatement();
                     var r = s.executeQuery("SELECT nextval(pg_get_serial_sequence('emprestimo','id'))")) {
                    r.next(); op.idEmprestimo = r.getLong(1);
                }
            }
            try (var s = c.prepareStatement("""
                    INSERT INTO emprestimo_operacao(chave,tipo,id_emprestimo,dados,estado)
                    VALUES (?,?,?,?,'PENDENTE') ON CONFLICT (chave) DO NOTHING
                    """)) {
                s.setString(1, op.chave); s.setString(2, op.tipo); s.setLong(3, op.idEmprestimo);
                s.setString(4, mapper.writeValueAsString(op)); s.executeUpdate();
            }
        } catch (Exception e) { throw new IllegalStateException("Falha ao registrar operacao", e); }
        // Libera a conexao antes da releitura, inclusive sob concorrencia no limite do pool.
        return buscar(op.chave).orElseThrow();
    }

    public OperacaoEmprestimo processar(String chave) {
        try (var c = dataSource.getConnection()) {
            c.setAutoCommit(false);
            try {
                OperacaoEmprestimo op;
                try (var s = c.prepareStatement("SELECT dados, estado FROM emprestimo_operacao WHERE chave=? FOR UPDATE")) {
                    s.setString(1, chave);
                    try (var r = s.executeQuery()) {
                        if (!r.next()) throw new IllegalStateException("Operacao inexistente");
                        op = mapear(r);
                    }
                }
                if ("PENDENTE".equals(op.estado)) {
                    persistirEmprestimo(c, op);
                    op.estado = "CONCLUIDA";
                    try (var s = c.prepareStatement("UPDATE emprestimo_operacao SET estado=? WHERE chave=?")) {
                        s.setString(1, op.estado); s.setString(2, chave); s.executeUpdate();
                    }
                }
                c.commit();
                return op;
            } catch (Exception e) { c.rollback(); throw e; }
        } catch (Exception e) { throw new IllegalStateException("Operacao pendente de confirmacao", e); }
    }

    private void persistirEmprestimo(Connection c, OperacaoEmprestimo op) throws Exception {
        if ("CRIAR".equals(op.tipo)) {
            try (var s = c.prepareStatement("""
                    INSERT INTO emprestimo(id,id_livro,id_usuario,data_emprestimo,data_prevista_devolucao,multa)
                    VALUES (?,?,?,?,?,?)
                    """)) {
                s.setLong(1, op.idEmprestimo); s.setLong(2, op.idLivro); s.setLong(3, op.idUsuario);
                s.setObject(4, op.dataEmprestimo); s.setObject(5, op.dataPrevista); s.setBigDecimal(6, op.multa);
                s.executeUpdate();
            }
        } else if ("DEVOLVER".equals(op.tipo)) {
            try (var s = c.prepareStatement("UPDATE emprestimo SET data_devolucao_real=?, multa=? WHERE id=? AND data_devolucao_real IS NULL")) {
                s.setObject(1, op.dataReferencia); s.setBigDecimal(2, op.multa); s.setLong(3, op.idEmprestimo);
                if (s.executeUpdate() != 1) throw new IllegalStateException("Emprestimo nao disponivel para devolucao");
            }
        } else { throw new IllegalStateException("Tipo de operacao desconhecido"); }
    }

    private OperacaoEmprestimo mapear(ResultSet r) throws Exception {
        OperacaoEmprestimo op = mapper.readValue(r.getString("dados"), OperacaoEmprestimo.class);
        op.estado = r.getString("estado");
        return op;
    }
}
