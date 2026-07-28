package tcc.legado.action;

import tcc.legado.ejb.livro.LivroEJB;
import tcc.legado.model.Livro;

import org.apache.struts.actions.DispatchAction;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.naming.InitialContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public class LivroAction extends DispatchAction {

    private LivroEJB getLivroEJB() throws Exception {
        InitialContext ctx = new InitialContext();
        // Nome JNDI do EJB (padrão JBoss EAP 7.4)
        String jndiName = "java:global/monolito-biblioteca/LivroEJBImpl!tcc.legado.ejb.LivroEJB";
        return (LivroEJB) ctx.lookup(jndiName);
    }

    public ActionForward listar(ActionMapping mapping, ActionForm form,
                                HttpServletRequest request, HttpServletResponse response) throws Exception {
        
        LivroEJB livroEJB = getLivroEJB();
        List<Livro> livros = livroEJB.listarTodos();
        request.setAttribute("lista", livros);
        
        return mapping.findForward("listarSucesso");
    }

    public ActionForward novo(ActionMapping mapping, ActionForm form,
                              HttpServletRequest request, HttpServletResponse response) throws Exception {
        return mapping.findForward("novo");
    }

    public ActionForward salvar(ActionMapping mapping, ActionForm form,
                                HttpServletRequest request, HttpServletResponse response) throws Exception {
        
        String idStr = request.getParameter("id");
        String titulo = request.getParameter("titulo");
        String autor = request.getParameter("autor");
        String isbn = request.getParameter("isbn");
        String anoStr = request.getParameter("ano");
        String editora = request.getParameter("editora");
        String quantidadeStr = request.getParameter("quantidade");
        
        if (titulo == null || titulo.trim().isEmpty()) {
            request.setAttribute("erro", "Título é obrigatório");
            return mapping.findForward("erro");
        }
        
        Livro livro = new Livro();
        if (idStr != null && !idStr.isEmpty()) {
            livro.setId(Long.parseLong(idStr));
        }
        livro.setTitulo(titulo);
        livro.setAutor(autor);
        livro.setIsbn(isbn);
        if (anoStr != null && !anoStr.isEmpty()) {
            livro.setAno(Integer.parseInt(anoStr));
        }
        livro.setEditora(editora);
        if (quantidadeStr != null && !quantidadeStr.isEmpty()) {
            livro.setQuantidade(Integer.parseInt(quantidadeStr));
        }
        
        LivroEJB livroEJB = getLivroEJB();
        if (livro.getId() != null) {
            livroEJB.atualizar(livro);
            request.setAttribute("mensagem", "Livro atualizado com sucesso!");
        } else {
            livroEJB.salvar(livro);
            request.setAttribute("mensagem", "Livro cadastrado com sucesso!");
        }
        
        return listar(mapping, form, request, response);
    }

    public ActionForward editar(ActionMapping mapping, ActionForm form,
                                HttpServletRequest request, HttpServletResponse response) throws Exception {
        
        String idStr = request.getParameter("id");
        if (idStr == null) {
            request.setAttribute("erro", "ID do livro não informado");
            return mapping.findForward("erro");
        }
        
        Long id = Long.parseLong(idStr);
        LivroEJB livroEJB = getLivroEJB();
        Livro livro = livroEJB.buscarPorId(id);
        request.setAttribute("livro", livro);
        
        return mapping.findForward("editar");
    }

    public ActionForward excluir(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request, HttpServletResponse response) throws Exception {
        
        String idStr = request.getParameter("id");
        if (idStr == null) {
            request.setAttribute("erro", "ID do livro não informado");
            return mapping.findForward("erro");
        }
        
        Long id = Long.parseLong(idStr);
        LivroEJB livroEJB = getLivroEJB();
        livroEJB.excluir(id);
        request.setAttribute("mensagem", "Livro excluído com sucesso!");
        
        return listar(mapping, form, request, response);
    }
}
