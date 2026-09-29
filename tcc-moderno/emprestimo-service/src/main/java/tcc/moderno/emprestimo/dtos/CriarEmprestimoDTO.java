package tcc.moderno.emprestimo.dtos;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
public final class CriarEmprestimoDTO {
    @NotBlank @Pattern(regexp="[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}") private final String chave;
    @NotNull @Positive private final Long idLivro;
    @NotNull @Positive private final Long idUsuario;
    @NotBlank private final String tipoUsuario;
    @NotNull private final LocalDate dataEmprestimo;
    @JsonCreator
    public CriarEmprestimoDTO(@JsonProperty("chave") String chave, @JsonProperty("idLivro") Long idLivro, @JsonProperty("idUsuario") Long idUsuario, @JsonProperty("tipoUsuario") String tipoUsuario, @JsonProperty("dataEmprestimo") LocalDate dataEmprestimo) {
        this.chave=chave;
        this.idLivro=idLivro;
        this.idUsuario=idUsuario;
        this.tipoUsuario=tipoUsuario;
        this.dataEmprestimo=dataEmprestimo;
    }
    public String getChave(){return chave;}
    public Long getIdLivro(){return idLivro;}
    public Long getIdUsuario(){return idUsuario;}
    public String getTipoUsuario(){return tipoUsuario;}
    public LocalDate getDataEmprestimo(){return dataEmprestimo;}
}
