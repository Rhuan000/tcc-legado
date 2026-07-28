package tcc.legado.ejb.usuario;

import tcc.legado.model.Usuario;
import tcc.legado.service.UsuarioService;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import java.util.List;

@Stateless
public class UsuarioEJB implements IUsuarioEJB {
    
    @Inject
    private UsuarioService usuarioService;
    
    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void salvar(Usuario usuario) {
        if (usuario.getNome() == null || usuario.getNome().trim().isEmpty()) {
            throw new RuntimeException("Nome obrigatório");
        }
        usuarioService.salvar(usuario);
    }
    
    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void atualizar(Usuario usuario) {
        if (usuario.getId() == null) {
            throw new RuntimeException("ID obrigatório");
        }
        usuarioService.atualizar(usuario);
    }
    
    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void excluir(Long id) {
        if (id == null) {
            throw new RuntimeException("ID obrigatório");
        }
        usuarioService.excluir(id);
    }
    
    @Override
    public List<Usuario> listarTodos() {
        return usuarioService.listarTodos();
    }
    
    @Override
    public Usuario buscarPorId(Long id) {
        return usuarioService.buscarPorId(id);
    }
    
    @Override
    public Usuario buscarPorMatricula(String matricula) {
        return usuarioService.buscarPorMatricula(matricula);
    }
}
