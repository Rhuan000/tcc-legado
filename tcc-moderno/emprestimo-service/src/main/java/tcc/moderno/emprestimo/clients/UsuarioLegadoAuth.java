package tcc.moderno.emprestimo.clients;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import java.net.URI;
import java.util.Optional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/** Registrado somente no cliente de usuarios: nunca envia o segredo a outras APIs. */
@ApplicationScoped
public class UsuarioLegadoAuth implements ClientRequestFilter {
    @ConfigProperty(name = "legado.usuarios.integracao.token") Optional<String> token;
    @ConfigProperty(name = "legado.usuarios.allow-local-http", defaultValue = "false") boolean permitirHttpLocal;

    @Override
    public void filter(ClientRequestContext request) {
        String segredo = token.orElse("");
        validarDestino(request.getUri(), permitirHttpLocal);
        if (segredo.strip().length() < 32 || segredo.contains("\r") || segredo.contains("\n")) {
            throw new IllegalStateException("Credencial da integracao de usuarios ausente ou invalida");
        }
        request.getHeaders().putSingle("X-Integration-Token", segredo);
    }

    static void validarDestino(URI uri, boolean permitirHttpLocal) {
        String host = uri.getHost();
        boolean local = "localhost".equalsIgnoreCase(host) || "127.0.0.1".equals(host)
                || "[::1]".equals(host) || "::1".equals(host);
        boolean transporteSeguro = "https".equalsIgnoreCase(uri.getScheme())
                || (permitirHttpLocal && local && "http".equalsIgnoreCase(uri.getScheme()));
        if (!transporteSeguro || host == null || uri.getUserInfo() != null
                || uri.getRawQuery() != null || uri.getFragment() != null) {
            throw new IllegalStateException("Destino da integracao de usuarios exige HTTPS sem credenciais na URL");
        }
    }
}
