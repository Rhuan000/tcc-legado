package tcc.legado.action;

import tcc.legado.ejb.usuario.IUsuarioEJB;
import tcc.legado.model.Usuario;

import org.apache.struts.actions.DispatchAction;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.naming.InitialContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public class UsuarioAction extends DispatchAction {
    
    private IUsuarioEJB getUsuarioEJB() throws Exception {
        InitialContext ctx = new InitialContext();
        return (IUsuarioEJB) ctx.lookup("java:global/monolito-biblioteca/UsuarioEJB!tcc.legado.ejb.usuario.IUsuarioEJB");
    }
    
    public ActionForward listar(ActionMapping mapping, ActionForm form,
                                HttpServletRequest request, HttpServletResponse response) throws Exception {
        IUsuarioEJB ejb = getUsuarioEJB();
        List<Usuario> usuarios = ejb.listarTodos();
        request.setAttribute("lista", usuarios);
        return mapping.findForward("listarSucesso");
    }
    
    public ActionForward novo(ActionMapping mapping, ActionForm form,
                              HttpServletRequest request, HttpServletResponse response) throws Exception {
        return mapping.findForward("novo");
    }
    
    public ActionForward salvar(ActionMapping mapping, ActionForm form,
                                HttpServletRequest request, HttpServletResponse response) throws Exception {
        String idStr = request.getParameter("id");
        String nome = request.getParameter("nome");
        String matricula = request.getParameter("matricula");
        String email = request.getParameter("email");
        String tipo = request.getParameter("tipo");
        
        Usuario usuario = new Usuario();
        if (idStr != null && !idStr.isEmpty()) {
            usuario.setId(Long.parseLong(idStr));
        }
        usuario.setNome(nome);
        usuario.setMatricula(matricula);
        usuario.setEmail(email);
        usuario.setTipo(tipo);
        
        IUsuarioEJB ejb = getUsuarioEJB();
        if (usuario.getId() != null) {
            ejb.atualizar(usuario);
            request.setAttribute("mensagem", "Usuário atualizado!");
        } else {
            ejb.salvar(usuario);
            request.setAttribute("mensagem", "Usuário cadastrado!");
        }
        return listar(mapping, form, request, response);
    }
    
    public ActionForward editar(ActionMapping mapping, ActionForm form,
                                HttpServletRequest request, HttpServletResponse response) throws Exception {
        String idStr = request.getParameter("id");
        if (idStr == null) {
            request.setAttribute("erro", "ID não informado");
            return mapping.findForward("erro");
        }
        Long id = Long.parseLong(idStr);
        IUsuarioEJB ejb = getUsuarioEJB();
        Usuario usuario = ejb.buscarPorId(id);
        request.setAttribute("usuario", usuario);
        return mapping.findForward("editar");
    }
    
    public ActionForward excluir(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request, HttpServletResponse response) throws Exception {
        String idStr = request.getParameter("id");
        if (idStr == null) {
            request.setAttribute("erro", "ID não informado");
            return mapping.findForward("erro");
        }
        Long id = Long.parseLong(idStr);
        IUsuarioEJB ejb = getUsuarioEJB();
        ejb.excluir(id);
        request.setAttribute("mensagem", "Usuário excluído!");
        return listar(mapping, form, request, response);
    }
}
