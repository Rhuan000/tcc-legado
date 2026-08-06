package tcc.legado.action;

import tcc.legado.model.LivroDestaque;
import tcc.legado.dao.LivroDestaqueDAO;

import org.apache.struts.actions.DispatchAction;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Action para gerenciar Livros em Destaque/Promoções
 * Domínio 2 do sistema
 */
public class LivroDestaqueAction extends DispatchAction {
    
    private LivroDestaqueDAO dao = new LivroDestaqueDAO();

	public ActionForward listar(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		String tipo = request.getParameter("tipo");
		List<LivroDestaque> destaques;

		if ("ativos".equals(tipo)) {
			destaques = dao.listarAtivos();
		} else {
			destaques = dao.listarTodos();
		}

		List<LivroDestaque> maisEmprestados = dao.listarPorCategoria("MAIS_EMPRESTADO");

		request.setAttribute("lista", destaques);
		request.setAttribute("listaMaisEmprestados", maisEmprestados); 
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
        
        String idStr = request.getParameter("id");
        String idLivroStr = request.getParameter("idLivro");
        String titulo = request.getParameter("titulo");
        String descricao = request.getParameter("descricao");
        String descontoStr = request.getParameter("desconto");
        String categoria = request.getParameter("categoria");
        String dataInicioStr = request.getParameter("dataInicio");
        String dataFimStr = request.getParameter("dataFim");
        String ativoStr = request.getParameter("ativo");
        
        if (titulo == null || titulo.trim().isEmpty()) {
            request.setAttribute("erro", "Título é obrigatório");
            return mapping.findForward("erro");
        }
        
        if (idLivroStr == null || idLivroStr.trim().isEmpty()) {
            request.setAttribute("erro", "ID do Livro é obrigatório");
            return mapping.findForward("erro");
        }
        
        LivroDestaque livroDestaque = new LivroDestaque();
        if (idStr != null && !idStr.isEmpty()) {
            livroDestaque.setId(Long.parseLong(idStr));
        }
        
        livroDestaque.setIdLivro(Long.parseLong(idLivroStr));
        livroDestaque.setTitulo(titulo);
        livroDestaque.setDescricao(descricao);
        
        if (descontoStr != null && !descontoStr.isEmpty()) {
            livroDestaque.setDesconto(Double.parseDouble(descontoStr));
        }
        
        livroDestaque.setCategoria(categoria);
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if (dataInicioStr != null && !dataInicioStr.isEmpty()) {
            livroDestaque.setDataInicio(sdf.parse(dataInicioStr));
        }
        if (dataFimStr != null && !dataFimStr.isEmpty()) {
            livroDestaque.setDataFim(sdf.parse(dataFimStr));
        }
        
        livroDestaque.setAtivo("on".equals(ativoStr) || "true".equals(ativoStr));
        
        if (livroDestaque.getId() != null) {
            dao.atualizar(livroDestaque);
            request.setAttribute("mensagem", "Destaque atualizado com sucesso!");
        } else {
            dao.salvar(livroDestaque);
            request.setAttribute("mensagem", "Destaque cadastrado com sucesso!");
        }
        
        return listar(mapping, form, request, response);
    }

    public ActionForward editar(ActionMapping mapping, ActionForm form,
                                HttpServletRequest request, HttpServletResponse response) throws Exception {
        
        String idStr = request.getParameter("id");
        if (idStr == null) {
            request.setAttribute("erro", "ID do destaque não informado");
            return mapping.findForward("erro");
        }
        
        Long id = Long.parseLong(idStr);
        LivroDestaque livroDestaque = dao.buscarPorId(id);
        request.setAttribute("destaque", livroDestaque);
        
        return mapping.findForward("editar");
    }

    public ActionForward excluir(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request, HttpServletResponse response) throws Exception {
        
        String idStr = request.getParameter("id");
        if (idStr == null) {
            request.setAttribute("erro", "ID do destaque não informado");
            return mapping.findForward("erro");
        }
        
        Long id = Long.parseLong(idStr);
        dao.excluir(id);
        request.setAttribute("mensagem", "Destaque excluído com sucesso!");
        
        return listar(mapping, form, request, response);
    }

    public ActionForward visualizar(ActionMapping mapping, ActionForm form,
                                    HttpServletRequest request, HttpServletResponse response) throws Exception {
        
        String idStr = request.getParameter("id");
        if (idStr == null) {
            request.setAttribute("erro", "ID do destaque não informado");
            return mapping.findForward("erro");
        }
        
        Long id = Long.parseLong(idStr);
        dao.incrementarVisualizacoes(id);
        LivroDestaque livroDestaque = dao.buscarPorId(id);
        request.setAttribute("destaque", livroDestaque);
        
        return mapping.findForward("visualizar");
    }
}
