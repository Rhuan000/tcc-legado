package tcc.moderno.emprestimo.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import tcc.moderno.emprestimo.dtos.ContextoUsuarioDTO;
import tcc.moderno.emprestimo.models.Emprestimo;
import tcc.moderno.emprestimo.repositories.EmprestimoRepository;
import static org.junit.jupiter.api.Assertions.*;

class AtualizacaoMultasServiceTest {
    private static final LocalDate DATA = LocalDate.of(2026, 10, 6);
    private final List<String> escritas = new ArrayList<>();
    private final EmprestimoRepository repositorio = new EmprestimoRepository(null) {
        @Override public List<Emprestimo> buscarAtrasados() {
            return List.of(emprestimo(1), emprestimo(2));
        }
    };
    private final ComandosEmprestimoService comandos = new ComandosEmprestimoService() {
        @Override public Emprestimo atualizarMulta(long id, ContextoUsuarioDTO contexto) {
            assertEquals(DATA, contexto.getDataReferencia());
            escritas.add(id + ":" + contexto.getTipoUsuario());
            return emprestimo(id);
        }
    };

    private static Emprestimo emprestimo(long id) {
        return new Emprestimo(id, 7L, id, DATA.minusDays(14), DATA.minusDays(4), null, BigDecimal.TEN);
    }

    @Test void falhaDeConsultaPreservaMultaContinuaOutrosERetomaComTipoAtual() {
        var tipo = new AtomicReference<String>();
        var usuarios = new ContextoUsuarioService(null) {
            @Override public String consultarTipoAtual(long id) {
                if (id == 1 && tipo.get() == null) throw new IllegalStateException("Indisponivel");
                return id == 1 ? tipo.get() : "ALUNO";
            }
        };
        var servico = new AtualizacaoMultasService(repositorio, usuarios, comandos);
        assertThrows(IllegalStateException.class, () -> servico.atualizar(DATA));
        assertEquals(List.of("2:ALUNO"), escritas, "Falha de consulta nao deve chamar a escrita daquele emprestimo");
        tipo.set("PROFESSOR");
        servico.atualizar(DATA);
        assertEquals(List.of("2:ALUNO", "1:PROFESSOR", "2:ALUNO"), escritas);
    }

    @Test void falhaDeEscritaNaoAbortaDemaisEmprestimos() {
        var usuarios = new ContextoUsuarioService(null) {
            @Override public String consultarTipoAtual(long id) { return "BOLSISTA"; }
        };
        var comandosComFalha = new ComandosEmprestimoService() {
            @Override public Emprestimo atualizarMulta(long id, ContextoUsuarioDTO contexto) {
                if (id == 1) throw new IllegalStateException("Falha SQL");
                return comandos.atualizarMulta(id, contexto);
            }
        };
        var servico = new AtualizacaoMultasService(repositorio, usuarios, comandosComFalha);
        assertThrows(IllegalStateException.class, () -> servico.atualizar(DATA));
        assertEquals(List.of("2:BOLSISTA"), escritas);
    }
}
