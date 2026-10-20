package tcc.moderno.emprestimo.services;

import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.util.List;
import java.util.function.IntFunction;

@ApplicationScoped
public class MultaService {

    /**
     * Calcula a multa nos dias úteis após o vencimento, incluindo a referência.
     * Os feriados de cada ano percorrido são fornecidos pela integração.
     */
    public double calcularMulta(LocalDate dataPrevista, String tipoUsuario,
                                LocalDate dataReferencia,
                                IntFunction<List<LocalDate>> feriadosPorAno) {
        if (!dataReferencia.isAfter(dataPrevista)) {
            return 0.0;
        }

        long diasUteisAtraso = 0;
        LocalDate cursor = dataPrevista;
        while (cursor.isBefore(dataReferencia)) {
            cursor = cursor.plusDays(1);
            if (cursor.getDayOfWeek().getValue() <= 5
                    && !feriadosPorAno.apply(cursor.getYear()).contains(cursor)) {
                diasUteisAtraso++;
            }
        }

        // Preserva o retorno antecipado do legado, inclusive para tipo nulo.
        if (diasUteisAtraso == 0) {
            return 0.0;
        }

        double valorDia = switch (tipoUsuario) {
            case "PROFESSOR" -> 0.50;
            case "BOLSISTA" -> 1.00;
            case "ALUNO", "BIBLIOTECARIO" -> 2.00;
            default -> 2.00;
        };
        return diasUteisAtraso * valorDia;
    }
}
