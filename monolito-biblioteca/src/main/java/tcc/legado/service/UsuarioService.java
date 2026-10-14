
package tcc.legado.service;

import tcc.legado.dao.UsuarioDAO;
import tcc.legado.dao.EmprestimoDAO;
import tcc.legado.exception.ValidacaoException;
import tcc.legado.model.Usuario;
import tcc.legado.util.HashUtil;

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
    
    public void salvar(Usuario usuario, String senha) {
        if (usuario.getMatricula() == null || !usuario.getMatricula().matches("[0-9]{7}")) {
            throw new IllegalArgumentException("A matrícula deve conter exatamente 7 dígitos.");
        }
        if (senha == null || senha.trim().isEmpty() || senha.length() < 8 || senha.length() > 128) {
            throw new IllegalArgumentException("A senha deve conter entre 8 e 128 caracteres.");
        }
        if (usuarioDAO.buscarPorMatricula(usuario.getMatricula()) != null) {
            throw new IllegalArgumentException("Matrícula já cadastrada");
        }
        if (usuario.getTipo() == null || 
            !usuario.getTipo().matches("ALUNO|PROFESSOR|BOLSISTA|BIBLIOTECARIO")) {
            throw new IllegalArgumentException("Tipo inválido");
        }
        usuarioDAO.salvar(usuario, HashUtil.sha256(senha));
    }
    
    public void atualizar(Usuario usuario) {
        Usuario existente = usuarioDAO.buscarPorId(usuario.getId());
        if (existente == null) {
            throw new ValidacaoException("Usuário não encontrado");
        }
        if (!existente.getMatricula().equals(usuario.getMatricula())) {
            throw new RuntimeException("Matrícula não pode ser alterada");
        }
        if (usuario.getTipo() == null || !usuario.getTipo().matches("ALUNO|PROFESSOR|BOLSISTA|BIBLIOTECARIO")) {
            throw new IllegalArgumentException("Tipo inválido");
        }
        usuarioDAO.atualizar(usuario);
    }
    
    @Inject
    private EmprestimoDAO emprestimoDAO;

    public void excluir(Long id) {
        Usuario usuario = usuarioDAO.buscarPorId(id);
        if (usuario == null) {
            throw new ValidacaoException("Usuário não encontrado");
        }
        if (emprestimoDAO.existePorUsuario(id)) {
            throw new ValidacaoException("Usuário possui empréstimos ativos ou histórico de empréstimos e não pode ser excluído.");
        }
        usuarioDAO.excluir(id);
    }
}
