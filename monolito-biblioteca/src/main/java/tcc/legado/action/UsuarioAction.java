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
import javax.servlet.http.HttpSession;

import java.io.PrintWriter;
import java.util.List;

public class UsuarioAction extends DispatchAction {
    
    private IUsuarioEJB getUsuarioEJB() throws Exception {
        InitialContext ctx = new InitialContext();
        return (IUsuarioEJB) ctx.lookup("java:global/monolito-biblioteca/UsuarioEJB!tcc.legado.ejb.usuario.IUsuarioEJB");
    }
       private boolean checarAutenticacao(HttpServletRequest request) throws Exception {
        HttpSession session = request.getSession();
        String perfil = (String) session.getAttribute("perfil");
        return !"ADMIN".equals(perfil); 
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
    
    public ActionForward voltar(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
    		return mapping.findForward("voltar");
    }
    
    public ActionForward salvar(ActionMapping mapping, ActionForm form,
                                HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (checarAutenticacao(request)) {
            request.setAttribute("erro", "Acesso negado. Apenas administradores podem realizar esta ação.");
            return mapping.findForward("erro");
        }
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
        if (checarAutenticacao(request)) {
            request.setAttribute("erro", "Acesso negado. Apenas administradores podem realizar esta ação.");
            return mapping.findForward("erro");
        }
 
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
        if (checarAutenticacao(request)) {
            request.setAttribute("erro", "Acesso negado. Apenas administradores podem realizar esta ação.");
            return mapping.findForward("erro");
        }
 
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

    public ActionForward buscarPorMatricula(ActionMapping mapping, ActionForm form,
                                            HttpServletRequest request, HttpServletResponse response) throws Exception {
        String matricula = request.getParameter("matricula");
        response.setContentType("application/json;charset=UTF-8");
        if (matricula == null || matricula.trim().isEmpty()) {
            response.getWriter().write("{\"encontrado\":false}");
            return null;
        }
        Usuario usuario = getUsuarioEJB().buscarPorMatricula(matricula);
        PrintWriter out = response.getWriter();
        if (usuario == null) {
            out.write("{\"encontrado\":false}");
        } else {
            out.write("{\"encontrado\":true,\"id\":" + usuario.getId() + ",\"nome\":\"" + usuario.getNome().replace("\"", "\\\"") + "\",\"matricula\":\"" + usuario.getMatricula() + "\",\"tipo\":\"" + usuario.getTipo() + "\"}");
        }
        return null;
    }
}
