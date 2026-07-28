package tcc.legado.model;

public class Usuario {
    private Long id;
    private String nome;
    private String matricula;
    private String email;
    private String tipo; // ALUNO, PROFESSOR, BOLSISTA

    public Usuario() {
    }

    public Usuario(Long id, String nome, String matricula, String email, String tipo) {
        this.id = id;
        this.nome = nome;
        this.matricula = matricula;
        this.email = email;
        this.tipo = tipo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", matricula='" + matricula + '\'' +
                ", email='" + email + '\'' +
                ", tipo='" + tipo + '\'' +
                '}';
    }
}
