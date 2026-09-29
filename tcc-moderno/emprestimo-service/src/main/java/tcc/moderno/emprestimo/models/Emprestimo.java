package tcc.moderno.emprestimo.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public final class Emprestimo {

    private final Long id;
    private final Long idLivro;
    private final Long idUsuario;
    private final LocalDate dataEmprestimo;
    private final LocalDate dataPrevistaDevolucao;
    private final LocalDate dataDevolucaoReal;
    private final BigDecimal multa;

    @JsonCreator
    public Emprestimo(@JsonProperty("id") Long id,
            @JsonProperty("idLivro") Long idLivro,
            @JsonProperty("idUsuario") Long idUsuario,
            @JsonProperty("dataEmprestimo") LocalDate dataEmprestimo,
            @JsonProperty("dataPrevistaDevolucao") LocalDate dataPrevistaDevolucao,
            @JsonProperty("dataDevolucaoReal") LocalDate dataDevolucaoReal,
            @JsonProperty("multa") BigDecimal multa) {
        this.id = id;
        this.idLivro = idLivro;
        this.idUsuario = idUsuario;
        this.dataEmprestimo = dataEmprestimo;
        this.dataPrevistaDevolucao = dataPrevistaDevolucao;
        this.dataDevolucaoReal = dataDevolucaoReal;
        this.multa = multa;
    }

    public Long getId() {
        return id;
    }

    public Long getIdLivro() {
        return idLivro;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public LocalDate getDataEmprestimo() {
        return dataEmprestimo;
    }

    public LocalDate getDataPrevistaDevolucao() {
        return dataPrevistaDevolucao;
    }

    public LocalDate getDataDevolucaoReal() {
        return dataDevolucaoReal;
    }

    public BigDecimal getMulta() {
        return multa;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Emprestimo that)) return false;
        return Objects.equals(id, that.id)
                && Objects.equals(idLivro, that.idLivro)
                && Objects.equals(idUsuario, that.idUsuario)
                && Objects.equals(dataEmprestimo, that.dataEmprestimo)
                && Objects.equals(dataPrevistaDevolucao, that.dataPrevistaDevolucao)
                && Objects.equals(dataDevolucaoReal, that.dataDevolucaoReal)
                && Objects.equals(multa, that.multa);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, idLivro, idUsuario, dataEmprestimo, dataPrevistaDevolucao, dataDevolucaoReal, multa);
    }
}
