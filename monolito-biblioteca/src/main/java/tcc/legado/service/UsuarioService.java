
package tcc.legado.service;

import tcc.legado.dao.UsuarioDAO;
import tcc.legado.model.Usuario;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.List;

@Dependent
public class UsuarioService {
    
    @Inject
    private UsuarioDAO usuarioDAO;
    
    public List<Usuario> listarTodos() {
        List<Usuario> usuarios = usuarioDAO.listarTodos();
        usuarios.sort((u1, u2) -> u1.getNome().compareToIgnoreCase(u2.getNome()));
        return usuarios;
    }
    
    public Usuario buscarPorId(Long id) {
        return usuarioDAO.buscarPorId(id);
    }
    
    public Usuario buscarPorMatricula(String matricula) {
        return usuarioDAO.buscarPorMatricula(matricula);
    }
    
    public void salvar(Usuario usuario) {
        if (usuarioDAO.buscarPorMatricula(usuario.getMatricula()) != null) {
            throw new RuntimeException("Matrícula já cadastrada");
        }
        if (usuario.getTipo() == null || 
            !usuario.getTipo().matches("ALUNO|PROFESSOR|BOLSISTA")) {
            throw new RuntimeException("Tipo inválido");
        }
        usuarioDAO.salvar(usuario);
    }
    
    public void atualizar(Usuario usuario) {
        Usuario existente = usuarioDAO.buscarPorId(usuario.getId());
        if (existente == null) {
            throw new RuntimeException("Usuário não encontrado");
        }
        if (!existente.getMatricula().equals(usuario.getMatricula())) {
            throw new RuntimeException("Matrícula não pode ser alterada");
        }
        usuarioDAO.atualizar(usuario);
    }
    
    public void excluir(Long id) {
        Usuario usuario = usuarioDAO.buscarPorId(id);
        if (usuario == null) {
            throw new RuntimeException("Usuário não encontrado");
        }
        usuarioDAO.excluir(id);
    }
}
