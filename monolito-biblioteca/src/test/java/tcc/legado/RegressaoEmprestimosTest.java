package tcc.legado;

import org.junit.Test;
import org.apache.struts.action.*;
import tcc.legado.action.EmprestimoAction;
import tcc.legado.dao.*;
import tcc.legado.ejb.emprestimo.*;
import tcc.legado.model.*;
import tcc.legado.service.EmprestimoService;
import javax.ejb.EJBException;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Proxy;
import java.util.*;
import static org.junit.Assert.*;

public class RegressaoEmprestimosTest {
    private final Map<String,Object> attrs = new HashMap<>();
    private HttpServletRequest request() {
        return (HttpServletRequest) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{HttpServletRequest.class}, (p,m,a) -> {
            if ("getParameter".equals(m.getName())) return "1";
            if ("setAttribute".equals(m.getName())) attrs.put((String)a[0],a[1]);
            return null;
        });
    }
    private ActionMapping mapping() {
        ActionMapping m = new ActionMapping(); m.addForwardConfig(new ActionForward("erro","/erro.jsp",false)); return m;
    }
    private EmprestimoAction action(IEmprestimoEJB ejb) {
        return new EmprestimoAction() { @Override protected IEmprestimoEJB getEmprestimoEJB() { return ejb; } };
    }
    @Test public void semEstoqueEDevolucaoRepetidaExibemSomenteMensagemDeNegocio() throws Exception {
        EmprestimoService service = new EmprestimoService();
        RegressaoLivrosTest.inject(service, "livroDAO", new LivroDAO() {
            @Override public Livro buscarPorId(Long id) { Livro l = new Livro(); l.setQuantidade(0); return l; }
        });
        RegressaoLivrosTest.inject(service, "usuarioDAO", new UsuarioDAO() {
            @Override public Usuario buscarPorMatricula(String matricula) { return new Usuario(); }
        });
        RegressaoLivrosTest.inject(service, "emprestimoDAO", new EmprestimoDAO() {
            @Override public Emprestimo buscarPorId(Long id) { Emprestimo e = new Emprestimo(); e.setDataDevolucaoReal(new Date()); return e; }
        });
        EmprestimoEJB ejb = new EmprestimoEJB(); RegressaoLivrosTest.inject(ejb, "emprestimoService", service);
        assertEquals("/erro.jsp", action(ejb).realizar(mapping(),null,request(),null).getPath());
        assertEquals("Livro indisponível",attrs.get("erro")); assertNull(attrs.get("mensagem"));
        attrs.clear();
        assertEquals("/erro.jsp", action(ejb).devolver(mapping(),null,request(),null).getPath());
        assertEquals("Empréstimo já devolvido",attrs.get("erro")); assertNull(attrs.get("mensagem"));
    }
    @Test public void falhaTecnicaFicaForaDaTela() throws Exception {
        EmprestimoEJB ejb = new EmprestimoEJB() {
            @Override public Emprestimo criarEmprestimo(Long id, String matricula) { throw new EJBException(new RuntimeException("jdbc:senha privada")); }
            @Override public void registrarDevolucao(Long id) { throw new EJBException(new RuntimeException("SQL privado")); }
        };
        action(ejb).realizar(mapping(),null,request(),null);
        assertEquals("Não foi possível realizar o empréstimo. Tente novamente.",attrs.get("erro"));
        action(ejb).devolver(mapping(),null,request(),null);
        assertEquals("Não foi possível registrar a devolução. Tente novamente.",attrs.get("erro"));
    }
}
