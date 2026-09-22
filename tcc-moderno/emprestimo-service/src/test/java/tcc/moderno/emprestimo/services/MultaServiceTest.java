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
 * Regras extraídas de EmprestimoService.calcularMulta e isDiaUtil, em
 * monolito-biblioteca/src/main/java/tcc/legado/service/EmprestimoService.java.
 * Primeira fatia do TCC: extração incremental do domínio de empréstimos,
 * iniciada pelas regras de cálculo de multa.
 *
 * <p>Contrato proposto, ainda NÃO implementado:
 * double calcularMulta(LocalDate dataPrevista, String tipoUsuario,
 *                     LocalDate dataReferencia, List<LocalDate> feriados).
 * A lista representa os feriados já obtidos para o ano do vencimento.
 * Consulta externa, cache, conversão de Date e persistência são responsabilidades
 * da integração e não são exercitadas por estes testes do núcleo de cálculo.
 *
 * <p>Testes TDD derivados da leitura do código: não constituem, por si só,
 * comparação executada entre os dois sistemas. Datas e resultados são fixos;
 * não há cópia do algoritmo, rede, banco ou dependência do relógio do sistema.
 * Devem falhar enquanto o contrato moderno não estiver implementado.
 */
@DisplayName("Multas: regras do legado a preservar na modernização")
class MultaServiceTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("cenariosDoLegado")
    void devePreservarResultadoDoLegado(String cenario, String vencimento, String tipo,
                                      String referencia, List<LocalDate> feriados, double esperado) {
        assertEquals(esperado, calcular(new MultaService(), vencimento, tipo, referencia, feriados),
                0.0, cenario);
    }

    static Stream<Arguments> cenariosDoLegado() {
        return Stream.of(
                caso("Devolução antecipada", "2026-03-09", "ALUNO", "2026-03-06", 0),
                caso("Devolução no vencimento", "2026-03-09", "ALUNO", "2026-03-09", 0),
                caso("Aluno: R$ 2 por dia útil, excluindo vencimento e incluindo referência", "2026-03-09", "ALUNO", "2026-03-10", 2),
                caso("Professor: R$ 0,50 por dia útil", "2026-03-09", "PROFESSOR", "2026-03-10", 0.5),
                caso("Bolsista: R$ 1 por dia útil", "2026-03-09", "BOLSISTA", "2026-03-10", 1),
                caso("Cinco dias úteis para aluno", "2026-03-09", "ALUNO", "2026-03-16", 10),
                caso("Cinco dias úteis para professor", "2026-03-09", "PROFESSOR", "2026-03-16", 2.5),
                caso("Cinco dias úteis para bolsista", "2026-03-09", "BOLSISTA", "2026-03-16", 5),
                caso("Sábado após vencimento na sexta", "2026-03-06", "ALUNO", "2026-03-07", 0),
                caso("Domingo após vencimento na sexta", "2026-03-06", "ALUNO", "2026-03-08", 0),
                caso("Segunda após vencimento na sexta", "2026-03-06", "ALUNO", "2026-03-09", 2),
                caso("Vencimento no sábado não é prorrogado pelo cálculo", "2026-03-07", "ALUNO", "2026-03-09", 2),
                caso("Vencimento no domingo não é prorrogado pelo cálculo", "2026-03-08", "ALUNO", "2026-03-09", 2),
                caso("Referência no domingo mantém cobrança da sexta", "2026-03-05", "ALUNO", "2026-03-08", 2),
                caso("Feriado no dia de referência", "2026-04-20", "ALUNO", "2026-04-21", 0, "2026-04-21"),
                caso("Feriado entre vencimento e referência", "2026-04-20", "ALUNO", "2026-04-22", 2, "2026-04-21"),
                caso("Feriado na segunda após fim de semana", "2026-03-06", "ALUNO", "2026-03-10", 2, "2026-03-09"),
                caso("Feriado no sábado não desconta dia útil adicional", "2026-03-06", "ALUNO", "2026-03-09", 2, "2026-03-07"),
                caso("Feriado no domingo não desconta dia útil adicional", "2026-03-06", "ALUNO", "2026-03-09", 2, "2026-03-08"),
                caso("Feriado no vencimento não prorroga contagem", "2026-04-21", "ALUNO", "2026-04-22", 2, "2026-04-21"),
                caso("Feriados fora do intervalo", "2026-03-09", "ALUNO", "2026-03-10", 2, "2026-03-06", "2026-03-11"),
                caso("Feriados duplicados descontados uma só vez", "2026-04-20", "ALUNO", "2026-04-22", 2, "2026-04-21", "2026-04-21"),
                caso("Todos os dias de atraso são feriados", "2026-03-09", "ALUNO", "2026-03-11", 0, "2026-03-10", "2026-03-11"),
                caso("Professor com feriado e fim de semana", "2026-04-17", "PROFESSOR", "2026-04-22", 1, "2026-04-21"),
                caso("Bolsista com feriado e fim de semana", "2026-04-17", "BOLSISTA", "2026-04-22", 2, "2026-04-21"),
                caso("Lista vazia não presume feriados nacionais", "2026-04-20", "ALUNO", "2026-04-22", 4),
                caso("Virada do mês", "2026-03-31", "ALUNO", "2026-04-02", 4),
                caso("29 de fevereiro útil é cobrado", "2024-02-28", "ALUNO", "2024-03-01", 4),
                // O legado consulta só o ano do vencimento. Sem 2027 na lista,
                // 01/01/2027 (sexta) é cobrado apesar de ser feriado nacional.
                caso("Virada do ano preserva limitação do legado", "2026-12-31", "ALUNO", "2027-01-04", 4, "2026-01-01", "2026-12-25"),
                caso("Feriado do ano seguinte respeitado se constar na lista", "2026-12-31", "ALUNO", "2027-01-04", 2, "2027-01-01"),
                caso("Tipo desconhecido usa tarifa padrão", "2026-03-09", "VISITANTE", "2026-03-10", 2),
                caso("Tipo vazio usa tarifa padrão", "2026-03-09", "", "2026-03-10", 2),
                caso("Tipo em minúsculas não é normalizado", "2026-03-09", "professor", "2026-03-10", 2),
                caso("Tipo com espaços não é normalizado", "2026-03-09", " PROFESSOR ", "2026-03-10", 2),
                // O retorno zero ocorre antes do switch, inclusive para tipo nulo.
                caso("Tipo nulo sem atraso", "2026-03-09", null, "2026-03-09", 0),
                caso("Tipo nulo com atraso apenas no fim de semana", "2026-03-06", null, "2026-03-08", 0),
                caso("Tipo nulo com atraso apenas em feriado", "2026-04-20", null, "2026-04-21", 0, "2026-04-21"),
                caso("Mais de uma semana sem teto de valor", "2026-03-02", "ALUNO", "2026-03-16", 20)
        );
    }

    @Test
    @DisplayName("Tipo nulo com dia útil de atraso preserva falha do switch legado")
    void devePreservarFalhaParaTipoNuloComDiaUtilDeAtraso() {
        assertThrows(NullPointerException.class,
                () -> calcular(new MultaService(), "2026-03-09", null, "2026-03-10", List.of()));
    }

    @Test
    @DisplayName("Recalcular com as mesmas entradas não soma a multa novamente")
    void deveRetornarMesmoValorAoRecalcular() {
        MultaService service = new MultaService();
        assertEquals(10.0, calcular(service, "2026-03-09", "ALUNO", "2026-03-16", List.of()), 0.0);
        assertEquals(10.0, calcular(service, "2026-03-09", "ALUNO", "2026-03-16", List.of()), 0.0);
    }

    private static Arguments caso(String nome, String vencimento, String tipo,
                                  String referencia, double esperado, String... feriados) {
        return Arguments.of(nome, vencimento, tipo, referencia,
                Stream.of(feriados).map(LocalDate::parse).toList(), esperado);
    }

    /**
     * Ponte temporária para compilar somente os testes sem criar métodos em
     * src/main. A ausência do contrato é falha explícita: não há skip ou cálculo
     * simulado. Substituir pela chamada direta quando o contrato existir.
     */
    private double calcular(MultaService service, String vencimento, String tipo,
                            String referencia, List<LocalDate> feriados) {
        Method metodo;
        try {
            metodo = MultaService.class.getMethod("calcularMulta",
                    LocalDate.class, String.class, LocalDate.class, List.class);
        } catch (NoSuchMethodException e) {
            return fail("Etapa RED: implementar MultaService.calcularMulta(LocalDate, String, LocalDate, List) "
                    + "para atender às regras de multa do legado.", e);
        }
        assertEquals(double.class, metodo.getReturnType(), "O contrato proposto retorna double, como o legado");
        try {
            return (double) metodo.invoke(service, LocalDate.parse(vencimento), tipo,
                    LocalDate.parse(referencia), feriados);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException runtime) {
                throw runtime;
            }
            if (e.getCause() instanceof Error error) {
                throw error;
            }
            return fail("O cálculo lançou uma exceção inesperada", e.getCause());
        } catch (IllegalAccessException e) {
            return fail("O método de cálculo precisa ser público", e);
        }
    }
}
