package tcc.moderno.emprestimo.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Caracterização derivada da leitura de EmprestimoService.criarEmprestimo,
 * calcularDataPrevistaComFeriados e isDiaUtil no monólito.
 * Não é uma comparação executada entre as duas aplicações.
 *
 * <p>Contrato proposto: LocalDate PrazoService.calcularDataPrevista(
 * LocalDate dataEmprestimo, String tipoUsuario,
 * IntFunction&lt;List&lt;LocalDate&gt;&gt; feriadosPorAno).
 * A data explícita substitui LocalDate.now() para permitir testes determinísticos.
 * A integração fornece os feriados de cada ano atravessado pelo prazo.
 * Consulta externa, cache, fuso e conversão para Date ficam fora destes testes.
 *
 * <p>Resultados fixos, sem rede, banco, relógio ou algoritmo simulado.
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
                caso("Bibliotecário: sete dias úteis", "2026-03-09", "BIBLIOTECARIO", "2026-03-18"),
                caso("Bibliotecário: feriado durante o prazo", "2026-03-09", "BIBLIOTECARIO", "2026-03-19", "2026-03-10"),
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
                caso("Virada do ano sem feriados informados para o novo ano", "2026-12-24", "ALUNO", "2027-01-05", "2026-01-01", "2026-12-25"),
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

    @Test
    @DisplayName("Consulta os feriados de cada ano atravessado pelo prazo")
    void deveConsultarFeriadosDeCadaAnoPercorrido() {
        Map<Integer, List<LocalDate>> feriados = Map.of(
                2026, List.of(LocalDate.of(2026, 12, 25)),
                2027, List.of(LocalDate.of(2027, 1, 1)));
        LinkedHashSet<Integer> anosConsultados = new LinkedHashSet<>();

        LocalDate dataPrevista = new PrazoService().calcularDataPrevista(
                LocalDate.of(2026, 12, 22), "ALUNO",
                ano -> {
                    anosConsultados.add(ano);
                    return feriados.getOrDefault(ano, List.of());
                });

        assertEquals(LocalDate.of(2027, 1, 4), dataPrevista);
        assertEquals(List.of(2026, 2027), anosConsultados.stream().toList());
    }

    private static Arguments caso(String nome, String inicio, String tipo,
                                  String esperado, String... feriados) {
        return Arguments.of(nome, inicio, tipo,
                Stream.of(feriados).map(LocalDate::parse).toList(), esperado);
    }

    private LocalDate calcular(String inicio, String tipo, List<LocalDate> feriados) {
        return new PrazoService().calcularDataPrevista(LocalDate.parse(inicio), tipo,
                ano -> feriados.stream()
                        .filter(feriado -> feriado.getYear() == ano)
                        .toList());
    }
}
