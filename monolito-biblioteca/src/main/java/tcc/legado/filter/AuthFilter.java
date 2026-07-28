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
        HttpSession session = request.getSession();

        String uri = request.getRequestURI();

        // Permite acesso público à página de login
        if (uri.contains("/auth.do") || uri.contains("/login.jsp") || uri.contains("/erro.jsp")) {
            chain.doFilter(request, response);
            return;
        }

        // Verifica se o usuário está logado
        Object usuario = session.getAttribute("usuarioLogado");
        if (usuario == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        // Verifica permissão baseada no perfil
        String perfil = (String) session.getAttribute("perfil");
        if (!temPermissao(uri, perfil)) {
            request.setAttribute("erro", "Acesso negado para este perfil");
            request.getRequestDispatcher("/erro.jsp").forward(request, response);
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
