package tcc.legado.action;

import tcc.legado.ejb.auth.IAuthEJB;
import tcc.legado.model.UsuarioAuth;

import org.apache.struts.actions.DispatchAction;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.naming.InitialContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class AuthAction extends DispatchAction {

    // Método para fazer login
    public ActionForward login(ActionMapping mapping, ActionForm form,
                               HttpServletRequest request, HttpServletResponse response) throws Exception {

        String matricula = request.getParameter("matricula");
        String senha = request.getParameter("senha");

        if (matricula == null || matricula.trim().isEmpty()) {
            request.setAttribute("erro", "Matrícula é obrigatória");
            return mapping.findForward("erro");
        }

        // Lookup JNDI do EJB
        InitialContext ctx = new InitialContext();
        IAuthEJB authEJB = (IAuthEJB) ctx.lookup("java:global/monolito-biblioteca/AuthEJB!tcc.legado.ejb.auth.IAuthEJB");

        UsuarioAuth user = authEJB.autenticar(matricula, senha);

        if (user != null) {
            HttpSession session = request.getSession();
            session.setAttribute("usuarioLogado", user);
            session.setAttribute("perfil", user.getPerfil());
            return mapping.findForward("sucesso");
        } else {
            request.setAttribute("erro", "Usuário ou senha inválidos");
            return mapping.findForward("erro");
        }
    }

    // Método para fazer logout
    public ActionForward logout(ActionMapping mapping, ActionForm form,
                                HttpServletRequest request, HttpServletResponse response) throws Exception {

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return mapping.findForward("logout");
    }
}
