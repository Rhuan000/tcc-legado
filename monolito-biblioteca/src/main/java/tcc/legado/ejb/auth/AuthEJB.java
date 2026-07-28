package tcc.legado.ejb.auth;

import tcc.legado.model.UsuarioAuth;
import tcc.legado.service.AuthService;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

@Stateless
public class AuthEJB implements IAuthEJB {

    @Inject
    private AuthService authService;

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public UsuarioAuth autenticar(String matricula, String senha) {
        if (matricula.length() < 7) {
            throw new RuntimeException("Matrícula deve ter pelo menos 7 caracteres");
        }
        return authService.autenticar(matricula, senha);
    }
}
