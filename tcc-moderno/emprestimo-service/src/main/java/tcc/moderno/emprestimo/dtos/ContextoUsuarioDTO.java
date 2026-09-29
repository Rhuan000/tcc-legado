package tcc.moderno.emprestimo.dtos;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
public final class ContextoUsuarioDTO {
    @NotBlank private final String tipoUsuario;
    @NotNull private final LocalDate dataReferencia;
    @JsonCreator
    public ContextoUsuarioDTO(@JsonProperty("tipoUsuario") String tipoUsuario, @JsonProperty("dataReferencia") LocalDate dataReferencia) {
        this.tipoUsuario=tipoUsuario;
        this.dataReferencia=dataReferencia;
    }
    public String getTipoUsuario(){return tipoUsuario;}
    public LocalDate getDataReferencia(){return dataReferencia;}
}
