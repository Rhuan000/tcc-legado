package tcc.legado.integracao;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Enumeration;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import tcc.legado.dao.UsuarioDAO;
import tcc.legado.model.Usuario;

/** API interna somente de leitura. Não aceita sessão de operador como credencial. */
@WebServlet(urlPatterns = "/integracao/usuarios/*")
public class UsuarioContextoServlet extends HttpServlet {
    private static final String HEADER = "X-Integration-Token";

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("X-Content-Type-Options", "nosniff");

        String esperado = configuracao("legado.usuarios.integracao.token", "LEGADO_USUARIOS_INTEGRATION_TOKEN");
        if (esperado == null || esperado.trim().length() < 32) {
            erro(response, 503, "Integracao indisponivel");
            return;
        }
        boolean localPermitido = Boolean.parseBoolean(configuracao(
                "legado.usuarios.allow-local-http", "LEGADO_USUARIOS_ALLOW_LOCAL_HTTP"));
        if (!request.isSecure() && !(localPermitido && loopback(request.getRemoteAddr()))) {
            erro(response, 403, "HTTPS obrigatorio");
            return;
        }
        Enumeration<String> valores = request.getHeaders(HEADER);
        String recebido = valores != null && valores.hasMoreElements() ? valores.nextElement() : null;
        if (recebido == null || valores.hasMoreElements()
                || !MessageDigest.isEqual(esperado.getBytes(StandardCharsets.UTF_8),
                        recebido.getBytes(StandardCharsets.UTF_8))) {
            erro(response, 401, "Credencial de integracao invalida");
            return;
        }
        if (!"GET".equals(request.getMethod())) {
            response.setHeader("Allow", "GET");
            erro(response, 405, "Metodo nao permitido");
            return;
        }
        String path = request.getPathInfo();
        if (path == null || !path.matches("/[1-9][0-9]{0,18}/contexto")) {
            erro(response, 400, "Identificador invalido");
            return;
        }
        long id;
        try {
            id = Long.parseLong(path.substring(1, path.indexOf('/', 1)));
        } catch (NumberFormatException e) {
            erro(response, 400, "Identificador invalido");
            return;
        }
        try {
            Usuario usuario = buscarUsuario(id);
            if (usuario == null) {
                erro(response, 404, "Usuario inexistente");
                return;
            }
            if (usuario.getTipo() == null || usuario.getTipo().trim().isEmpty()) {
                erro(response, 503, "Contexto de usuario indisponivel");
                return;
            }
            response.getWriter().write(new JSONObject().put("idUsuario", usuario.getId())
                    .put("tipoUsuario", usuario.getTipo()).toString());
        } catch (RuntimeException e) {
            // Não publicar SQL, dados pessoais ou credenciais na resposta/log.
            getServletContext().log("Falha ao consultar contexto de usuario na integracao");
            erro(response, 503, "Contexto de usuario indisponivel");
        }
    }

    protected Usuario buscarUsuario(long id) {
        return new UsuarioDAO().buscarContextoPorId(id);
    }

    private static String configuracao(String propriedade, String variavel) {
        String valor = System.getProperty(propriedade);
        return valor != null ? valor : System.getenv(variavel);
    }

    private static boolean loopback(String endereco) {
        return "127.0.0.1".equals(endereco) || "::1".equals(endereco)
                || "0:0:0:0:0:0:0:1".equals(endereco);
    }

    private static void erro(HttpServletResponse response, int status, String mensagem) throws IOException {
        response.setStatus(status);
        response.getWriter().write(new JSONObject().put("erro", mensagem).toString());
    }
}
