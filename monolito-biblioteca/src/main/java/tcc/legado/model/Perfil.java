package tcc.legado.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashSet;
import java.util.Set;

@XmlRootElement(name = "perfil")
public class Perfil {
    private String nome;
    private Set<String> permissoes = new HashSet<>();

    @XmlAttribute(name = "nome")
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    @XmlElement(name = "permissao")
    public Set<String> getPermissoes() { return permissoes; }
    public void setPermissoes(Set<String> permissoes) { this.permissoes = permissoes; }
}