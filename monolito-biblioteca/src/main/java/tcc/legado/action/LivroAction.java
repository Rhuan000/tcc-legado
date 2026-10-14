package tcc.legado.action;

import tcc.legado.ejb.livro.ILivroEJB;
import tcc.legado.model.Livro;
import tcc.legado.exception.ValidacaoException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.struts.actions.DispatchAction;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.naming.InitialContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.util.List;

public class LivroAction extends DispatchAction {

    private static final Logger LOG = Logger.getLogger(LivroAction.class.getName());

    protected ILivroEJB getLivroEJB() throws Exception {
        InitialContext ctx = new InitialContext();
        String jndiName = "java:global/monolito-biblioteca/LivroEJB!tcc.legado.ejb.livro.ILivroEJB";
        return (ILivroEJB) ctx.lookup(jndiName);
    }
    private boolean checarAutenticacao(HttpServletRequest request) throws Exception {
        HttpSession session = request.getSession();
        String perfil = (String) session.getAttribute("perfil");
        return !"ADMIN".equals(perfil); 
    }
    public ActionForward listar(ActionMapping mapping, ActionForm form,
                                HttpServletRequest request, HttpServletResponse response) throws Exception {
        
        ILivroEJB livroEJB = getLivroEJB();
        List<Livro> livros = livroEJB.listarTodos();
        request.setAttribute("lista", livros);
        
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
        Livro livro = new Livro();
        livro.setTitulo(request.getParameter("titulo"));
        livro.setAutor(request.getParameter("autor"));
        livro.setIsbn(request.getParameter("isbn"));
        livro.setEditora(request.getParameter("editora"));
        request.setAttribute("livro", livro);
        // A JSP usa os parâmetros originais ao reapresentar erros de conversão.
        request.setAttribute("reapresentar", true);
        try {
            if (idStr != null && !idStr.trim().isEmpty()) {
                livro.setId(Long.valueOf(idStr.trim()));
            }
            String ano = request.getParameter("ano");
            livro.setAno(ano == null || ano.trim().isEmpty() ? null : Integer.valueOf(ano.trim()));
            String quantidade = request.getParameter("quantidade");
            livro.setQuantidade(quantidade == null || quantidade.trim().isEmpty()
                    ? 0 : Integer.valueOf(quantidade.trim()));

            ILivroEJB livroEJB = getLivroEJB();
            if (livro.getId() != null) {
                livroEJB.atualizar(livro);
                request.setAttribute("mensagem", "Livro atualizado com sucesso!");
            } else {
                livroEJB.salvar(livro);
                request.setAttribute("mensagem", "Livro cadastrado com sucesso!");
            }
        } catch (NumberFormatException e) {
            request.setAttribute("erro", "Ano, quantidade e ID devem ser números inteiros válidos");
            return mapping.findForward("novo");
        } catch (ValidacaoException e) {
            request.setAttribute("erro", e.getMessage());
            return mapping.findForward("novo");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Falha ao gravar livro", e);
            request.setAttribute("erro", "Não foi possível gravar o livro. Tente novamente.");
            return mapping.findForward("novo");
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
            request.setAttribute("erro", "ID do livro não informado");
            return mapping.findForward("erro");
        }
        
        Long id = Long.parseLong(idStr);
        ILivroEJB livroEJB = getLivroEJB();
        Livro livro = livroEJB.buscarPorId(id);
        request.setAttribute("livro", livro);
        
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
            request.setAttribute("erro", "ID do livro não informado");
            return mapping.findForward("erro");
        }
        
        try {
            Long id = Long.valueOf(idStr);
            getLivroEJB().excluir(id);
            request.setAttribute("mensagem", "Livro excluído com sucesso!");
        } catch (NumberFormatException e) {
            request.setAttribute("erro", "ID do livro inválido");
            return mapping.findForward("erro");
        } catch (ValidacaoException e) {
            request.setAttribute("erro", e.getMessage());
            return mapping.findForward("erro");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Falha ao excluir livro", e);
            request.setAttribute("erro", "Não foi possível excluir o livro. Tente novamente.");
            return mapping.findForward("erro");
        }

        return listar(mapping, form, request, response);
    }

	public ActionForward detalhar(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String idStr = request.getParameter("id");
		if (idStr == null || idStr.trim().isEmpty()) {
			request.setAttribute("erro", "ID do livro não informado");
			return mapping.findForward("erro");
		}
		Long id = Long.parseLong(idStr);
		ILivroEJB livroEJB = getLivroEJB();
		Livro livro = livroEJB.buscarPorId(id);
		if (livro == null) {
			request.setAttribute("erro", "Livro não encontrado");
			return mapping.findForward("erro");
		}
		request.setAttribute("livro", livro);
		return mapping.findForward("detalhar");
	}
}
