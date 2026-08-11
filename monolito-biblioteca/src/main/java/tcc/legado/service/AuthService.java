package tcc.legado.service;

import tcc.legado.dao.AuthDAO;
import tcc.legado.model.Usuario;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@Dependent
public class AuthService {

    @Inject
    private AuthDAO authDAO;

    public Usuario autenticar(String matricula, String senha) {
        if (matricula == null || matricula.trim().isEmpty()) {
            throw new RuntimeException("Matrícula é obrigatória");
        }
        if (senha == null || senha.trim().isEmpty()) {
            throw new RuntimeException("Senha é obrigatória");
        }
        return authDAO.autenticar(matricula, senha);
    }
}
