package tcc.moderno.emprestimo.clients;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import tcc.moderno.emprestimo.dtos.FeriadoResponseDTO;

import java.util.List;

@Path("/api/feriados/v1")
@Produces(MediaType.APPLICATION_JSON)
@RegisterRestClient(configKey = "feriado-api")
public interface FeriadoClient {

    @GET
    @Path("/{ano}")
    List<FeriadoResponseDTO> buscarFeriados(@PathParam("ano") int ano);
}
