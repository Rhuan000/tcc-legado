package tcc.legado.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import tcc.legado.model.Perfil;
import tcc.legado.util.PerfisWrapper;

import java.io.IOException;
import java.util.Map;

public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        HttpSession session = request.getSession(false);

        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();

        // 1. Ignorar recursos estáticos
        if (uri.endsWith(".css") || uri.endsWith(".js") ||
            uri.endsWith(".png") || uri.endsWith(".jpg") || uri.endsWith(".gif") ||
            uri.endsWith(".ico") || uri.endsWith(".map") ||
            uri.contains("/webjars/") || uri.contains("/font-awesome/")) {
            chain.doFilter(request, response);
            return;
        }

        // 2. Liberar URLs públicas
        if (uri.contains("/auth.do") ||
            uri.endsWith("/login.jsp") ||
            uri.endsWith("/error.jsp") ||
            uri.endsWith("/erro.jsp") ||
            uri.equals(contextPath + "/")) {
            chain.doFilter(request, response);
            return;
        }

        // 3. Verificar autenticação
        if (session == null || session.getAttribute("usuarioLogado") == null) {
            response.sendRedirect(contextPath + "/login.jsp");
            return;
        }

        // 4. Verificar permissão (se for uma ação .do)
        if (uri.contains(".do")) {
            String perfil = (String) session.getAttribute("perfil");
            String metodo = request.getParameter("metodo"); // parâmetro do Struts
            
            if (!temPermissao(uri, perfil, metodo)) {
                request.setAttribute("erro", "Acesso negado para este perfil");
                request.getRequestDispatcher("/error.jsp").forward(request, response);
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private boolean temPermissao(String uri, String perfilNome, String metodo) {
        Map<String, Perfil> perfis = PerfisWrapper.carregarPerfis();
        Perfil perfil = perfis.get(perfilNome);
        if (perfil == null) return false;

        // Mapeia a URI e o método para uma permissão
        String permissao = mapearParaPermissao(uri, metodo);
        if (permissao == null) return true; // sem restrição (ex: index.jsp)

        return perfil.getPermissoes().contains(permissao);
    }

    private String mapearParaPermissao(String uri, String metodo) {
        String dominio = extrairDominio(uri);
        if (dominio == null) return null;

        String acao = mapearMetodo(metodo);
        if (acao == null) return null;

        return dominio + "." + acao;
    }

    private String extrairDominio(String uri) {
        if (uri.contains("/livro")) return "livro";
        if (uri.contains("/usuario")) return "usuario";
        if (uri.contains("/emprestimo")) return "emprestimo";
        if (uri.contains("/destaque")) return "destaque";
        return null;
    }

    private String mapearMetodo(String metodo) {
        if (metodo == null) return null;
        switch (metodo) {
            case "listar": return "listar";
            case "novo": return "cadastrar";
            case "salvar": return "cadastrar";
            case "editar": return "editar";
            case "excluir": return "excluir";
            case "detalhar": return "listar";      // detalhar usa a mesma permissão de listar
            case "visualizar": return "visualizar";
            case "devolver": return "devolver";
            case "devolverForm": return "devolver";
            case "realizar": return "realizar";
            default: return null;
        }
    }
}