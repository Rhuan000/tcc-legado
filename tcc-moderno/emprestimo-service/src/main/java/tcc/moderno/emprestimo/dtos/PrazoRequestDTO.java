package tcc.moderno.emprestimo.dtos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Objects;

public final class PrazoRequestDTO {

    @NotNull
    private final LocalDate dataEmprestimo;
    @NotBlank
    private final String tipoUsuario;

    @JsonCreator
    public PrazoRequestDTO(@JsonProperty("dataEmprestimo") LocalDate dataEmprestimo,
            @JsonProperty("tipoUsuario") String tipoUsuario) {
        this.dataEmprestimo = dataEmprestimo;
        this.tipoUsuario = tipoUsuario;
    }

    public LocalDate getDataEmprestimo() {
        return dataEmprestimo;
    }

    public String getTipoUsuario() {
        return tipoUsuario;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof PrazoRequestDTO that)) return false;
        return Objects.equals(dataEmprestimo, that.dataEmprestimo)
                && Objects.equals(tipoUsuario, that.tipoUsuario);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataEmprestimo, tipoUsuario);
    }
}
