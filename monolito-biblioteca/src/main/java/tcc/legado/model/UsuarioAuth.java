package tcc.legado.model;

public class UsuarioAuth {
    private String matricula;
    private String senha;
    private String perfil;

    public UsuarioAuth() {}

    public UsuarioAuth(String matricula, String senha, String perfil) {
        this.matricula = matricula;
        this.senha = senha;
        this.perfil = perfil;
    }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public String getPerfil() { return perfil; }
    public void setPerfil(String perfil) { this.perfil = perfil; }
}
