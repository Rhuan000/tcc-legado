package tcc.moderno.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/calcular")
public class MultaResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String verificar() {
        return "Multa resource disponível";
    }
}
