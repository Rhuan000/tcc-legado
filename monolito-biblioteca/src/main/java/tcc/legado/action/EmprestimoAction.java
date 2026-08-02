package tcc.legado.action;

import tcc.legado.ejb.emprestimo.IEmprestimoEJB;
import tcc.legado.model.Emprestimo;
import tcc.legado.dao.LivroDAO;
import tcc.legado.dao.UsuarioDAO;

import org.apache.struts.actions.DispatchAction;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.naming.InitialContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class EmprestimoAction extends DispatchAction {

    private IEmprestimoEJB getEmprestimoEJB() throws Exception {
        InitialContext ctx = new InitialContext();
        return (IEmprestimoEJB) ctx.lookup("java:global/monolito-biblioteca/EmprestimoEJB!tcc.legado.ejb.emprestimo.IEmprestimoEJB");
    }

    public ActionForward novo(ActionMapping mapping, ActionForm form,
                              HttpServletRequest request, HttpServletResponse response) throws Exception {
        LivroDAO livroDAO = new LivroDAO();
        UsuarioDAO usuarioDAO = new UsuarioDAO();
        request.setAttribute("livros", livroDAO.listarTodos());
        request.setAttribute("usuarios", usuarioDAO.listarTodos());
        return mapping.findForward("novo");
    }

    public ActionForward realizar(ActionMapping mapping, ActionForm form,
                                  HttpServletRequest request, HttpServletResponse response) throws Exception {
        String idLivroStr = request.getParameter("idLivro");
        String matricula = request.getParameter("matricula");
        String dataPrevistaFront = request.getParameter("dataPrevistaFront"); // campo hidden

        if (idLivroStr == null || idLivroStr.trim().isEmpty()) {
            request.setAttribute("erro", "Selecione um livro");
            return mapping.findForward("erro");
        }
        Long idLivro = Long.parseLong(idLivroStr);

        // VENENO: validação da data prevista vinda do front (duplicada)
        if (dataPrevistaFront != null && !dataPrevistaFront.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date dataFront = sdf.parse(dataPrevistaFront);
                // Comparar com a data que será calculada no EJB? Não temos como aqui.
                // Vamos apenas armazenar num atributo para depois comparar.
                request.setAttribute("dataFront", dataFront);
            } catch (Exception e) {
                request.setAttribute("erro", "Data prevista inválida");
                return mapping.findForward("erro");
            }
        }

        IEmprestimoEJB ejb = getEmprestimoEJB();
        Emprestimo emp = ejb.criarEmprestimo(idLivro, matricula);

        // VENENO: comparação da data calculada pelo back com a data do front (se enviada)
        Date dataFront = (Date) request.getAttribute("dataFront");
        if (dataFront != null && !dataFront.equals(emp.getDataPrevistaDevolucao())) {
            // Divergência proposital – lança erro
            request.setAttribute("erro", "Divergência na data prevista: Front=" + dataFront + " Back=" + emp.getDataPrevistaDevolucao());
            return mapping.findForward("erro");
        }

        request.setAttribute("mensagem", "Empréstimo realizado com sucesso! ID: " + emp.getId());
        return listar(mapping, form, request, response);
    }

    public ActionForward listar(ActionMapping mapping, ActionForm form,
                                HttpServletRequest request, HttpServletResponse response) throws Exception {
        IEmprestimoEJB ejb = getEmprestimoEJB();
        List<Emprestimo> lista = ejb.listarTodos();
        request.setAttribute("lista", lista);
        return mapping.findForward("listarSucesso");
    }

    public ActionForward devolverForm(ActionMapping mapping, ActionForm form,
                                      HttpServletRequest request, HttpServletResponse response) throws Exception {
        IEmprestimoEJB ejb = getEmprestimoEJB();
        List<Emprestimo> ativos = ejb.listarTodos().stream()
                .filter(e -> e.getDataDevolucaoReal() == null)
                .collect(java.util.stream.Collectors.toList());
        request.setAttribute("emprestimos", ativos);
        return mapping.findForward("devolver");
    }

    public ActionForward devolver(ActionMapping mapping, ActionForm form,
                                  HttpServletRequest request, HttpServletResponse response) throws Exception {
        String idStr = request.getParameter("idEmprestimo");
        if (idStr == null || idStr.trim().isEmpty()) {
            request.setAttribute("erro", "Selecione um empréstimo");
            return mapping.findForward("erro");
        }
        Long id = Long.parseLong(idStr);

        IEmprestimoEJB ejb = getEmprestimoEJB();
        ejb.registrarDevolucao(id);

        request.setAttribute("mensagem", "Devolução registrada com sucesso!");
        return listar(mapping, form, request, response);
    }
}
