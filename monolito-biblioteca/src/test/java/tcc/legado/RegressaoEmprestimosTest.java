package tcc.legado;

import org.junit.Test;
import org.apache.struts.action.*;
import tcc.legado.action.EmprestimoAction;
import tcc.legado.exception.ValidacaoException;
import tcc.legado.model.Emprestimo;
import tcc.legado.service.CoordenadorEmprestimo;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Proxy;
import java.util.*;
import static org.junit.Assert.*;

public class RegressaoEmprestimosTest {
    private final Map<String,Object> attrs = new HashMap<>();
    private HttpServletRequest request() {
        return (HttpServletRequest) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{HttpServletRequest.class}, (p,m,a) -> {
            if ("getParameter".equals(m.getName())) return "chave".equals(a[0]) ? "00000000-0000-0000-0000-000000000001" : "1";
            if ("setAttribute".equals(m.getName())) attrs.put((String)a[0],a[1]);
            return null;
        });
    }
    private ActionMapping mapping() {
        ActionMapping m=new ActionMapping(); m.addForwardConfig(new ActionForward("erro","/erro.jsp",false)); return m;
    }
    @Test public void semEstoqueExibeSomenteMensagemDeNegocio() throws Exception {
        EmprestimoAction action=new EmprestimoAction();
        RegressaoLivrosTest.inject(action,"coordenador",new CoordenadorEmprestimo() {
            @Override public Emprestimo criar(Long id,String matricula,String chave) { throw new ValidacaoException("Livro indisponível"); }
        });
        assertEquals("/erro.jsp",action.realizar(mapping(),null,request(),null).getPath());
        assertEquals("Livro indisponível",attrs.get("erro")); assertNull(attrs.get("mensagem"));
    }
    @Test public void falhaTecnicaFicaNoLogEPreservaOrientacaoDeRetentativa() throws Exception {
        EmprestimoAction action=new EmprestimoAction();
        RegressaoLivrosTest.inject(action,"coordenador",new CoordenadorEmprestimo() {
            @Override public Emprestimo criar(Long id,String matricula,String chave) { throw new IllegalStateException("jdbc:senha privada"); }
            @Override public Emprestimo devolver(Long id) { throw new IllegalStateException("SQL privado"); }
        });
        action.realizar(mapping(),null,request(),null);
        assertTrue(attrs.get("erro").toString().contains("reenvie o mesmo formulário"));
        assertFalse(attrs.get("erro").toString().contains("privada"));
        action.devolver(mapping(),null,request(),null);
        assertEquals("Não foi possível confirmar a devolução. Consulte a lista e tente novamente.",attrs.get("erro"));
        assertNull(attrs.get("mensagem"));
    }
}
