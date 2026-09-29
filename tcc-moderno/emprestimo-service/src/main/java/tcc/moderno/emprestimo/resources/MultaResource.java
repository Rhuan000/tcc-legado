package tcc.moderno.emprestimo.resources;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import tcc.moderno.emprestimo.dtos.MultaRequestDTO;
import tcc.moderno.emprestimo.dtos.MultaResponseDTO;
import tcc.moderno.emprestimo.services.CalendarioService;
import tcc.moderno.emprestimo.services.MultaService;

import java.time.LocalDate;
import java.util.List;

@Path("/multas")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MultaResource {

    private final MultaService multaService;
    private final CalendarioService calendarioService;

    @Inject
    public MultaResource(MultaService multaService, CalendarioService calendarioService) {
        this.multaService = multaService;
        this.calendarioService = calendarioService;
    }

    @POST
    @Path("/calcular")
    public MultaResponseDTO calcular(@Valid MultaRequestDTO requisicao) {
        List<LocalDate> feriados = requisicao.getDataReferencia().isAfter(requisicao.getDataPrevista())
                ? calendarioService.buscarFeriados(requisicao.getDataPrevista().getYear())
                : List.of();
        double valor = multaService.calcularMulta(
                requisicao.getDataPrevista(),
                requisicao.getTipoUsuario(),
                requisicao.getDataReferencia(),
                feriados);
        return new MultaResponseDTO(valor);
    }
}
