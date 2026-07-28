package tcc.legado.ejb.auth;

import tcc.legado.model.UsuarioAuth;

public interface IAuthEJB {
    UsuarioAuth autenticar(String matricula, String senha);
}
