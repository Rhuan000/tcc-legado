package tcc.moderno.emprestimo.dtos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Objects;

public final class MultaRequestDTO {

    @NotNull
    private final LocalDate dataPrevista;
    @NotBlank
    private final String tipoUsuario;
    @NotNull
    private final LocalDate dataReferencia;

    @JsonCreator
    public MultaRequestDTO(@JsonProperty("dataPrevista") LocalDate dataPrevista,
            @JsonProperty("tipoUsuario") String tipoUsuario,
            @JsonProperty("dataReferencia") LocalDate dataReferencia) {
        this.dataPrevista = dataPrevista;
        this.tipoUsuario = tipoUsuario;
        this.dataReferencia = dataReferencia;
    }

    public LocalDate getDataPrevista() {
        return dataPrevista;
    }

    public String getTipoUsuario() {
        return tipoUsuario;
    }

    public LocalDate getDataReferencia() {
        return dataReferencia;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof MultaRequestDTO that)) return false;
        return Objects.equals(dataPrevista, that.dataPrevista)
                && Objects.equals(tipoUsuario, that.tipoUsuario)
                && Objects.equals(dataReferencia, that.dataReferencia);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataPrevista, tipoUsuario, dataReferencia);
    }
}
