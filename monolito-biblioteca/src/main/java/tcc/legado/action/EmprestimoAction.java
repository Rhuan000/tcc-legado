package tcc.legado.action;

import tcc.legado.ejb.emprestimo.IEmprestimoEJB;
import tcc.legado.model.Emprestimo;
import tcc.legado.model.Livro;
import tcc.legado.model.Usuario;
import tcc.legado.dao.LivroDAO;
import tcc.legado.dao.UsuarioDAO;


import org.apache.struts.actions.DispatchAction;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.naming.InitialContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import tcc.legado.exception.ValidacaoException;

public class EmprestimoAction extends DispatchAction {
    private static final Logger LOG = Logger.getLogger(EmprestimoAction.class.getName());

    protected IEmprestimoEJB getEmprestimoEJB() throws Exception {
        InitialContext ctx = new InitialContext();
        return (IEmprestimoEJB) ctx.lookup("java:global/monolito-biblioteca/EmprestimoEJB!tcc.legado.ejb.emprestimo.IEmprestimoEJB");
    }

    // Exibe formulário para novo empréstimo
    public ActionForward novo(ActionMapping mapping, ActionForm form,
                              HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            LivroDAO livroDAO = new LivroDAO();
            UsuarioDAO usuarioDAO = new UsuarioDAO();
            request.setAttribute("livros", livroDAO.listarTodos());
            request.setAttribute("usuarios", usuarioDAO.listarTodos());
            return mapping.findForward("novo");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Não foi possível carregar os dados. Tente novamente.", e);
            request.setAttribute("erro", "Não foi possível carregar os dados. Tente novamente.");
            return mapping.findForward("erro");
        }
    }

    // Realiza o empréstimo
    public ActionForward realizar(ActionMapping mapping, ActionForm form,
                                  HttpServletRequest request, HttpServletResponse response) throws Exception {
        String idLivroStr = request.getParameter("idLivro");
        String matricula = request.getParameter("matricula");

        if (idLivroStr == null || idLivroStr.trim().isEmpty()) {
            request.setAttribute("erro", "Selecione um livro");
            return mapping.findForward("erro");
        }
        if (matricula == null || matricula.trim().isEmpty()) {
            request.setAttribute("erro", "Informe a matrícula do usuário");
            return mapping.findForward("erro");
        }

        try {
            Long idLivro = Long.parseLong(idLivroStr);
            IEmprestimoEJB ejb = getEmprestimoEJB();
            Emprestimo emp = ejb.criarEmprestimo(idLivro, matricula);
            request.setAttribute("mensagem", "Empréstimo realizado com sucesso! ID: " + emp.getId());
            return listar(mapping, form, request, response);
        } catch (NumberFormatException e) {
            request.setAttribute("erro", "ID do livro inválido");
            return mapping.findForward("erro");
        } catch (ValidacaoException e) {
            request.setAttribute("erro", e.getMessage());
            return mapping.findForward("erro");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Falha ao realizar empréstimo", e);
            request.setAttribute("erro", "Não foi possível realizar o empréstimo. Tente novamente.");
            return mapping.findForward("erro");
        }
    }

    // Lista empréstimos
    public ActionForward listar(ActionMapping mapping, ActionForm form,
                                HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            IEmprestimoEJB ejb = getEmprestimoEJB();
            List<Emprestimo> lista = ejb.listarTodos();
            request.setAttribute("lista", lista);
            return mapping.findForward("listarSucesso");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Não foi possível listar os empréstimos. Tente novamente.", e);
            request.setAttribute("erro", "Não foi possível listar os empréstimos. Tente novamente.");
            return mapping.findForward("erro");
        }
    }

    // Exibe formulário de devolução
    public ActionForward devolverForm(ActionMapping mapping, ActionForm form,
                                      HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            IEmprestimoEJB ejb = getEmprestimoEJB();
            List<Emprestimo> ativos = ejb.listarTodos().stream()
                    .filter(e -> e.getDataDevolucaoReal() == null)
                    .collect(java.util.stream.Collectors.toList());
            request.setAttribute("emprestimos", ativos);
            return mapping.findForward("devolver");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Não foi possível carregar os empréstimos ativos. Tente novamente.", e);
            request.setAttribute("erro", "Não foi possível carregar os empréstimos ativos. Tente novamente.");
            return mapping.findForward("erro");
        }
    }

    // Registra devolução
    public ActionForward devolver(ActionMapping mapping, ActionForm form,
                                  HttpServletRequest request, HttpServletResponse response) throws Exception {
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.trim().isEmpty()) {
            request.setAttribute("erro", "Selecione um empréstimo");
            return mapping.findForward("erro");
        }

        try {
            Long id = Long.parseLong(idStr);
            IEmprestimoEJB ejb = getEmprestimoEJB();
            ejb.registrarDevolucao(id);
            request.setAttribute("mensagem", "Devolução registrada com sucesso!");
            return listar(mapping, form, request, response);
        } catch (NumberFormatException e) {
            request.setAttribute("erro", "ID do empréstimo inválido");
            return mapping.findForward("erro");
        } catch (ValidacaoException e) {
            request.setAttribute("erro", e.getMessage());
            return mapping.findForward("erro");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Falha ao registrar devolução", e);
            request.setAttribute("erro", "Não foi possível registrar a devolução. Tente novamente.");
            return mapping.findForward("erro");
        }
    }

    public ActionForward voltar(ActionMapping mapping, ActionForm form,
                                HttpServletRequest request, HttpServletResponse response) throws Exception {
        return mapping.findForward("voltar");
    }
    
	public ActionForward detalhar(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String idStr = request.getParameter("id");
		if (idStr == null || idStr.trim().isEmpty()) {
			request.setAttribute("erro", "ID do empréstimo não informado");
			return mapping.findForward("erro");
		}
		Long id = Long.parseLong(idStr);
		IEmprestimoEJB ejb = getEmprestimoEJB();
		Emprestimo emp = ejb.buscarPorId(id);
		if (emp == null) {
			request.setAttribute("erro", "Empréstimo não encontrado");
			return mapping.findForward("erro");
		}

		
		LivroDAO livroDAO = new LivroDAO();
		UsuarioDAO usuarioDAO = new UsuarioDAO();
		Livro livro = livroDAO.buscarPorId(emp.getIdLivro());
		Usuario usuario = usuarioDAO.buscarPorId(emp.getIdUsuario());
		
		request.setAttribute("emprestimo", emp);
		request.setAttribute("livro", livro);
		request.setAttribute("usuario", usuario);
		return mapping.findForward("detalhar");
	}
}