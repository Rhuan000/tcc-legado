package tcc.moderno.emprestimo.services;

import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class PrazoService {

    /**
     * Calcula o vencimento contando apenas dias úteis posteriores ao empréstimo.
     * Os feriados são fornecidos pela integração, sem consulta externa aqui.
     */
    public LocalDate calcularDataPrevista(LocalDate dataEmprestimo, String tipoUsuario,
                                          List<LocalDate> feriados) {
        int diasUteis = switch (tipoUsuario) {
            case "PROFESSOR" -> 14;
            case "BOLSISTA" -> 10;
            default -> 7;
        };

        LocalDate dataPrevista = dataEmprestimo;
        while (diasUteis > 0) {
            dataPrevista = dataPrevista.plusDays(1);
            if (dataPrevista.getDayOfWeek().getValue() <= 5
                    && !feriados.contains(dataPrevista)) {
                diasUteis--;
            }
        }
        return dataPrevista;
    }
}
