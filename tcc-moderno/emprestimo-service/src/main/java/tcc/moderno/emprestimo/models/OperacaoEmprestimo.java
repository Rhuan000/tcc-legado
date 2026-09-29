package tcc.moderno.emprestimo.models;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Estado duravel de recuperacao; nao e um contrato HTTP. */
public final class OperacaoEmprestimo {
    public String chave;
    public String tipo;
    public String estado = "PENDENTE";
    public long idEmprestimo;
    public long idLivro;
    public long idUsuario;
    public String tipoUsuario;
    public LocalDate dataEmprestimo;
    public LocalDate dataPrevista;
    public LocalDate dataReferencia;
    public BigDecimal multa;
}
