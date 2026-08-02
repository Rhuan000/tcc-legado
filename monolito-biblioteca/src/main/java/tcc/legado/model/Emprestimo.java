package tcc.legado.model;

import java.util.Date;

public class Emprestimo {
    private Long id;
    private Long idLivro;
    private Long idUsuario;
    private Date dataEmprestimo;
    private Date dataPrevistaDevolucao;
    private Date dataDevolucaoReal;
    private Double multa;
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getIdLivro() {
        return idLivro;
    }
    public void setIdLivro(Long idLivro) {
        this.idLivro = idLivro;
    }
    public Long getIdUsuario() {
        return idUsuario;
    }
    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }
    public Date getDataEmprestimo() {
        return dataEmprestimo;
    }
    public void setDataEmprestimo(Date dataEmprestimo) {
        this.dataEmprestimo = dataEmprestimo;
    }
    public Date getDataPrevistaDevolucao() {
        return dataPrevistaDevolucao;
    }
    public void setDataPrevistaDevolucao(Date dataPrevistaDevolucao) {
        this.dataPrevistaDevolucao = dataPrevistaDevolucao;
    }
    public Date getDataDevolucaoReal() {
        return dataDevolucaoReal;
    }
    public void setDataDevolucaoReal(Date dataDevolucaoReal) {
        this.dataDevolucaoReal = dataDevolucaoReal;
    }
    public Double getMulta() {
        return multa;
    }
    public void setMulta(Double multa) {
        this.multa = multa;
    }
}
