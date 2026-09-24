package tcc.moderno.emprestimo.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Caracterização derivada da leitura de EmprestimoService.criarEmprestimo,
 * calcularDataPrevistaComFeriados e isDiaUtil no monólito.
 * Não é uma comparação executada entre as duas aplicações.
 *
 * <p>Contrato proposto: LocalDate PrazoService.calcularDataPrevista(
 * LocalDate dataEmprestimo, String tipoUsuario, List<LocalDate> feriados).
 * A data explícita substitui LocalDate.now() para permitir testes determinísticos.
 * A integração fornecerá os feriados do ano de início, como no legado, mesmo
 * quando o prazo atravessar o ano. O núcleo respeita somente a lista recebida.
 * Consulta externa, cache, fuso e conversão para Date ficam fora destes testes.
 *
 * <p>Etapa RED: resultados fixos, sem rede, banco, relógio ou algoritmo simulado.
 * A reflexão permite compilar antes da implementação; deve ser substituída por
 * chamada direta quando o contrato existir, preservando os cenários.
 */
@DisplayName("Prazos: regras do legado a preservar na modernização")
class PrazoServiceTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("cenariosDoLegado")
    void devePreservarPrazoDoLegado(String cenario, String inicio, String tipo,
                                   List<LocalDate> feriados, String esperado) {
        assertEquals(LocalDate.parse(esperado), calcular(inicio, tipo, feriados), cenario);
    }

    static Stream<Arguments> cenariosDoLegado() {
        return Stream.of(
                caso("Aluno: sete dias úteis, excluindo o início", "2026-03-09", "ALUNO", "2026-03-18"),
                caso("Professor: quatorze dias úteis", "2026-03-09", "PROFESSOR", "2026-03-27"),
                caso("Bolsista: dez dias úteis", "2026-03-09", "BOLSISTA", "2026-03-23"),
                caso("Início na sexta atravessa dois fins de semana", "2026-03-06", "ALUNO", "2026-03-17"),
                caso("Início no sábado", "2026-03-07", "ALUNO", "2026-03-17"),
                caso("Início no domingo", "2026-03-08", "ALUNO", "2026-03-17"),
                caso("Feriado no início não aumenta prazo", "2026-03-09", "ALUNO", "2026-03-18", "2026-03-09"),
                caso("Feriado durante o prazo", "2026-03-09", "ALUNO", "2026-03-19", "2026-03-10"),
                caso("Feriado no vencimento desloca para próximo dia útil", "2026-03-09", "ALUNO", "2026-03-19", "2026-03-18"),
                caso("Feriados consecutivos seguidos de fim de semana", "2026-03-09", "ALUNO", "2026-03-23", "2026-03-18", "2026-03-19", "2026-03-20"),
                caso("Feriado no sábado não aumenta prazo", "2026-03-09", "ALUNO", "2026-03-18", "2026-03-14"),
                caso("Feriado no domingo não aumenta prazo", "2026-03-09", "ALUNO", "2026-03-18", "2026-03-15"),
                caso("Feriado duplicado contado uma só vez", "2026-03-09", "ALUNO", "2026-03-19", "2026-03-10", "2026-03-10"),
                caso("Feriados fora do intervalo", "2026-03-09", "ALUNO", "2026-03-18", "2026-03-06", "2026-03-20"),
                caso("Lista vazia não presume feriados nacionais", "2026-04-20", "ALUNO", "2026-04-29"),
                caso("Feriado informado é excluído", "2026-04-20", "ALUNO", "2026-04-30", "2026-04-21"),
                caso("Virada do mês", "2026-03-31", "ALUNO", "2026-04-09"),
                caso("Ano bissexto conta 29 de fevereiro útil", "2024-02-28", "ALUNO", "2024-03-08"),
                // Sem feriados do novo ano na lista, 01/01/2027 é contado.
                caso("Virada do ano preserva consulta apenas do ano inicial", "2026-12-24", "ALUNO", "2027-01-05", "2026-01-01", "2026-12-25"),
                caso("Início no último dia do ano", "2026-12-31", "ALUNO", "2027-01-11", "2026-01-01", "2026-12-25"),
                caso("Núcleo respeita feriado do próximo ano se fornecido", "2026-12-31", "ALUNO", "2027-01-12", "2027-01-01"),
                caso("Tipo desconhecido usa sete dias", "2026-03-09", "VISITANTE", "2026-03-18"),
                caso("Tipo vazio usa sete dias", "2026-03-09", "", "2026-03-18"),
                caso("Tipo em minúsculas não é normalizado", "2026-03-09", "professor", "2026-03-18"),
                caso("Tipo com espaços não é normalizado", "2026-03-09", " PROFESSOR ", "2026-03-18")
        );
    }

    @Test
    @DisplayName("Tipo nulo preserva falha do switch antes do cálculo de prazo")
    void devePreservarFalhaParaTipoNulo() {
        assertThrows(NullPointerException.class,
                () -> calcular("2026-03-09", null, List.of()));
    }

    private static Arguments caso(String nome, String inicio, String tipo,
                                  String esperado, String... feriados) {
        return Arguments.of(nome, inicio, tipo,
                Stream.of(feriados).map(LocalDate::parse).toList(), esperado);
    }

    private LocalDate calcular(String inicio, String tipo, List<LocalDate> feriados) {
        try {
            Class<?> classe = Class.forName("tcc.moderno.emprestimo.services.PrazoService");
            Method metodo = classe.getMethod("calcularDataPrevista",
                    LocalDate.class, String.class, List.class);
            assertEquals(LocalDate.class, metodo.getReturnType());
            return (LocalDate) metodo.invoke(classe.getConstructor().newInstance(),
                    LocalDate.parse(inicio), tipo, feriados);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException runtime) {
                throw runtime;
            }
            if (e.getCause() instanceof Error error) {
                throw error;
            }
            return fail("O cálculo lançou uma exceção inesperada", e.getCause());
        } catch (ReflectiveOperationException e) {
            return fail("Etapa RED: implementar PrazoService.calcularDataPrevista"
                    + "(LocalDate, String, List) com retorno LocalDate e construtor público sem argumentos.", e);
        }
    }
}
