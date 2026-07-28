package tcc.legado.service;

import tcc.legado.dao.AuthDAO;
import tcc.legado.model.UsuarioAuth;

import javax.enterprise.context.Dependent;

@Dependent
public class AuthService {

    public UsuarioAuth autenticar(String matricula, String senha) {
        if (matricula == null || matricula.trim().isEmpty()) {
            throw new RuntimeException("Matrícula é obrigatória");
        }
        if (senha == null || senha.trim().isEmpty()) {
            throw new RuntimeException("Senha é obrigatória");
        }
        return AuthDAO.autenticar(matricula, senha);
    }
}
