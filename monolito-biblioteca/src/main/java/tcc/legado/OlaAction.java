package tcc.legado;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class OlaAction extends Action {
    
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request, 
                                 HttpServletResponse response) throws Exception {
        
        // Coloca uma mensagem na requisição para o JSP pegar
        request.setAttribute("mensagem", "📚 Sistema Legado de Biblioteca - TCC");
        request.setAttribute("subtitulo", "Agora com Struts 1.x, o caos está completo!");
        
        // Encaminha para o JSP (mapeado no struts-config.xml como "sucesso")
        return mapping.findForward("sucesso");
    }
}