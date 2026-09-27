package tcc.moderno.emprestimo.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record MultaRequestDTO(
        @NotNull LocalDate dataPrevista,
        @NotBlank String tipoUsuario,
        @NotNull LocalDate dataReferencia) {
}
