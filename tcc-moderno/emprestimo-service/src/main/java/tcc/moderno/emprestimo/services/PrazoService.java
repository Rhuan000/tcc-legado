package tcc.moderno.emprestimo.services;

import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.util.List;
import java.util.function.IntFunction;

@ApplicationScoped
public class PrazoService {

    /**
     * Calcula o vencimento contando apenas dias úteis posteriores ao empréstimo.
     * Os feriados de cada ano percorrido são fornecidos pela integração.
     */
    public LocalDate calcularDataPrevista(LocalDate dataEmprestimo, String tipoUsuario,
                                          IntFunction<List<LocalDate>> feriadosPorAno) {
        int diasUteis = switch (tipoUsuario) {
            case "PROFESSOR" -> 14;
            case "BOLSISTA" -> 10;
            case "ALUNO", "BIBLIOTECARIO" -> 7;
            default -> 7;
        };

        LocalDate dataPrevista = dataEmprestimo;
        while (diasUteis > 0) {
            dataPrevista = dataPrevista.plusDays(1);
            if (dataPrevista.getDayOfWeek().getValue() <= 5
                    && !feriadosPorAno.apply(dataPrevista.getYear()).contains(dataPrevista)) {
                diasUteis--;
            }
        }
        return dataPrevista;
    }
}
