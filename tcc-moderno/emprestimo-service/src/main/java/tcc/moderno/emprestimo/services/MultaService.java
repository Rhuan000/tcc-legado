package tcc.moderno.emprestimo.services;

import java.time.LocalDate;
import java.util.List;

public class MultaService {

    /**
     * Calcula a multa nos dias úteis após o vencimento, incluindo a referência.
     * Os feriados são fornecidos pela integração, sem consulta externa aqui.
     */
    public double calcularMulta(LocalDate dataPrevista, String tipoUsuario,
                                LocalDate dataReferencia, List<LocalDate> feriados) {
        if (!dataReferencia.isAfter(dataPrevista)) {
            return 0.0;
        }

        long diasUteisAtraso = 0;
        LocalDate cursor = dataPrevista;
        while (cursor.isBefore(dataReferencia)) {
            cursor = cursor.plusDays(1);
            if (cursor.getDayOfWeek().getValue() <= 5 && !feriados.contains(cursor)) {
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
            default -> 2.00;
        };
        return diasUteisAtraso * valorDia;
    }
}
