package tcc.legado.model;

public class Livro {
    private Long id;
    private String titulo;
    private String autor;
    private String isbn;
    private Integer ano;
    private String editora;
    private Integer quantidade;

    // Construtor vazio
    public Livro() {}

    // Getters e Setters (gere via IDE)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public Integer getAno() { return ano; }
    public void setAno(Integer ano) { this.ano = ano; }
    public String getEditora() { return editora; }
    public void setEditora(String editora) { this.editora = editora; }
    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }
}


