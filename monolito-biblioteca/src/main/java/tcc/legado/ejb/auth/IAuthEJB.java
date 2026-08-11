package tcc.legado.ejb.auth;

import tcc.legado.model.Usuario;

public interface IAuthEJB {
    Usuario autenticar(String matricula, String senha);
}
