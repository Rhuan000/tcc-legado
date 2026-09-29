package tcc.moderno.emprestimo.dtos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.Objects;

public final class PrazoResponseDTO {

    private final LocalDate dataPrevista;

    @JsonCreator
    public PrazoResponseDTO(@JsonProperty("dataPrevista") LocalDate dataPrevista) {
        this.dataPrevista = dataPrevista;
    }

    public LocalDate getDataPrevista() {
        return dataPrevista;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof PrazoResponseDTO that)) return false;
        return Objects.equals(dataPrevista, that.dataPrevista);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataPrevista);
    }
}
