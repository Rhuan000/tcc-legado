package tcc.legado.model;

import java.util.Date;

/**
 * Domínio de Livros em Destaque/Promoções
 * Representa livros que estão em promoção ou destaque na biblioteca
 */
public class LivroDestaque {
    private Long id;
    private Long idLivro;
    private String titulo;
    private String descricao;
    private Double desconto; // Percentual de desconto (0-100)
    private String categoria; // Ex: "Promoção", "Bestseller", "Novo Lançamento"
    private Date dataInicio;
    private Date dataFim;
    private Boolean ativo;
    private Integer visualizacoes;

    // Constructors
    public LivroDestaque() {}

    public LivroDestaque(Long idLivro, String titulo, String descricao, Double desconto) {
        this.idLivro = idLivro;
        this.titulo = titulo;
        this.descricao = descricao;
        this.desconto = desconto;
        this.ativo = true;
        this.visualizacoes = 0;
    }

    // Getters e Setters
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

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Double getDesconto() {
        return desconto;
    }

    public void setDesconto(Double desconto) {
        this.desconto = desconto;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Date getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(Date dataInicio) {
        this.dataInicio = dataInicio;
    }

    public Date getDataFim() {
        return dataFim;
    }

    public void setDataFim(Date dataFim) {
        this.dataFim = dataFim;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public Integer getVisualizacoes() {
        return visualizacoes;
    }

    public void setVisualizacoes(Integer visualizacoes) {
        this.visualizacoes = visualizacoes;
    }
}
