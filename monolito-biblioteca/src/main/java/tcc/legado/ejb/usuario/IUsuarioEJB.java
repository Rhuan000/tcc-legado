package tcc.legado.ejb.usuario;

import tcc.legado.model.Usuario;
import java.util.List;

public interface IUsuarioEJB {
    List<Usuario> listarTodos();
    Usuario buscarPorId(Long id);
    Usuario buscarPorMatricula(String matricula);
    void salvar(Usuario usuario);
    void atualizar(Usuario usuario);
    void excluir(Long id);
}
