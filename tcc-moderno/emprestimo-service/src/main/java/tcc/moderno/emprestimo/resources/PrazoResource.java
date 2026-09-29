package tcc.moderno.emprestimo.resources;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import tcc.moderno.emprestimo.dtos.PrazoRequestDTO;
import tcc.moderno.emprestimo.dtos.PrazoResponseDTO;
import tcc.moderno.emprestimo.services.CalendarioService;
import tcc.moderno.emprestimo.services.PrazoService;

@Path("/prazos")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PrazoResource {

    private final PrazoService prazoService;
    private final CalendarioService calendarioService;

    @Inject
    public PrazoResource(PrazoService prazoService, CalendarioService calendarioService) {
        this.prazoService = prazoService;
        this.calendarioService = calendarioService;
    }

    @POST
    @Path("/calcular")
    public PrazoResponseDTO calcular(@Valid PrazoRequestDTO requisicao) {
        var feriados = calendarioService.buscarFeriados(requisicao.getDataEmprestimo().getYear());
        var dataPrevista = prazoService.calcularDataPrevista(
                requisicao.getDataEmprestimo(),
                requisicao.getTipoUsuario(),
                feriados);
        return new PrazoResponseDTO(dataPrevista);
    }
}
