package tcc.moderno.emprestimo.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record PrazoRequestDTO(
        @NotNull LocalDate dataEmprestimo,
        @NotBlank String tipoUsuario) {
}
