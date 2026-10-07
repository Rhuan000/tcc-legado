package tcc.moderno.emprestimo.clients;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/integracao/usuarios")
@Produces(MediaType.APPLICATION_JSON)
@RegisterRestClient(configKey = "legado-usuarios")
@RegisterProvider(UsuarioLegadoAuth.class)
public interface UsuarioLegadoClient {
    @GET
    @Path("/{id}/contexto")
    Response consultar(@PathParam("id") long id);
}
