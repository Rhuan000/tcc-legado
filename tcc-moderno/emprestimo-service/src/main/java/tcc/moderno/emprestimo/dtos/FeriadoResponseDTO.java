package tcc.moderno.emprestimo.dtos;

import java.time.LocalDate;

public record FeriadoResponseDTO(LocalDate date, String name, String type) {
}
