package tcc.legado.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        HttpSession session = request.getSession(false); // não cria sessão se não existir

        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();

        // 1. Ignorar recursos estáticos (CSS, JS, imagens, webjars, etc.)
            uri.endsWith(".png") || uri.endsWith(".jpg") || uri.endsWith(".gif") ||
            uri.endsWith(".ico") || uri.endsWith(".map") ||
            uri.contains("/webjars/") || uri.contains("/font-awesome/")) {
            chain.doFilter(request, response);
            return;
        }


        if (uri.contains("/auth.do") || 
            uri.endsWith("/login.jsp") || 
            uri.endsWith("/error.jsp") ||
            uri.endsWith("/erro.jsp") ||
            uri.equals(contextPath + "/")) {    
            chain.doFilter(request, response);
            return;
        }

        if (session == null || session.getAttribute("usuarioLogado") == null) {
            // Redireciona para login
            response.sendRedirect(contextPath + "/login.jsp");
            return;
        }

        String perfil = (String) session.getAttribute("perfil");
        if (!temPermissao(uri, perfil)) {
            request.setAttribute("erro", "Acesso negado para este perfil");
            request.getRequestDispatcher("/error.jsp").forward(request, response);
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean temPermissao(String uri, String perfil) {
        if ("ADMIN".equals(perfil)) return true;
        if ("BIBLIOTECARIO".equals(perfil)) {
            return !uri.contains("excluir");
        }
        if ("CONSULTA".equals(perfil)) {
            return uri.contains("listar") || uri.contains("login");
        }
        return false;
    }

}