package tcc.legado.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import tcc.legado.model.Emprestimo;
import tcc.legado.util.CacheGlobal;

import java.lang.reflect.Method;
import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class EmprestimoServiceCalendarioTest {

    private final EmprestimoService service = new EmprestimoService();

    @Before
    public void prepararCalendario() {
        CacheGlobal.clearFeriados();
        CacheGlobal.setFeriados(2026, Collections.singletonList(LocalDate.of(2026, 12, 25)));
        CacheGlobal.setFeriados(2027, Collections.singletonList(LocalDate.of(2027, 1, 1)));
    }

    @After
    public void limparCalendario() {
        CacheGlobal.clearFeriados();
    }

    @Test
    public void multaUsaOsFeriadosDeCadaAnoPercorrido() throws Exception {
        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setDataPrevistaDevolucao(Date.valueOf("2026-12-31"));

        Method calcularMulta = EmprestimoService.class.getDeclaredMethod(
                "calcularMulta", Emprestimo.class, String.class, LocalDate.class);
        calcularMulta.setAccessible(true);

        double multa = (double) calcularMulta.invoke(
                service, emprestimo, "ALUNO", LocalDate.of(2027, 1, 4));

        assertEquals(2.0, multa, 0.001);
    }

    @Test
    public void prazoUsaOsFeriadosDeCadaAnoPercorrido() {
        java.util.Date resultado = service.calcularDataPrevistaComFeriados(
                LocalDate.of(2026, 12, 22), 7);
        LocalDate dataPrevista = resultado.toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDate();

        assertEquals(LocalDate.of(2027, 1, 4), dataPrevista);
        assertEquals(Collections.singletonList(LocalDate.of(2026, 12, 25)),
                CacheGlobal.getFeriados(2026));
        assertEquals(Collections.singletonList(LocalDate.of(2027, 1, 1)),
                CacheGlobal.getFeriados(2027));
    }
}
