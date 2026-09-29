package tcc.moderno.emprestimo.resources;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.Priorities;
import jakarta.annotation.Priority;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.util.Optional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class IntegracaoAuthFilter implements ContainerRequestFilter {
    @ConfigProperty(name="emprestimo.integracao.token") Optional<String> token;
    public void filter(ContainerRequestContext request) {
        if(token.isEmpty() || token.get().isBlank()) {
            request.abortWith(Response.status(503).build()); return;
        }
        String recebido=request.getHeaderString("X-Integration-Token");
        if(recebido==null || !MessageDigest.isEqual(token.get().getBytes(StandardCharsets.UTF_8),
                recebido.getBytes(StandardCharsets.UTF_8)))
            request.abortWith(Response.status(401).build());
    }
}
