package tcc.moderno.emprestimo.resources;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Consumes;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Max;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import tcc.moderno.emprestimo.models.Emprestimo;
import tcc.moderno.emprestimo.dtos.ContagemEmprestimosDTO;
import tcc.moderno.emprestimo.repositories.EmprestimoRepository;
import tcc.moderno.emprestimo.dtos.CriarEmprestimoDTO;
import tcc.moderno.emprestimo.services.ComandosEmprestimoService;

@Path("/emprestimos")
@Produces(MediaType.APPLICATION_JSON)
public class EmprestimoResource {

    private final EmprestimoRepository repository;
    @Inject ComandosEmprestimoService comandos;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Emprestimo criar(@NotNull @Valid CriarEmprestimoDTO requisicao) {
        return comandos.criar(requisicao);
    }

    @POST
    @Path("/{id}/devolucao")
    public Emprestimo devolver(@PathParam("id") @Positive long id, @NotNull @Valid tcc.moderno.emprestimo.dtos.ContextoUsuarioDTO contexto) {
        return comandos.devolver(id, contexto);
    }

    @POST
    @Path("/{id}/multa")
    @Consumes(MediaType.APPLICATION_JSON)
    public Emprestimo atualizarMulta(@PathParam("id") @Positive long id,
            @NotNull @Valid tcc.moderno.emprestimo.dtos.ContextoUsuarioDTO contexto) {
        return comandos.atualizarMulta(id, contexto);
    }

    @Inject
    public EmprestimoResource(EmprestimoRepository repository) {
        this.repository = repository;
    }

    @GET
    public List<Emprestimo> listarTodos() {
        return repository.listarTodos();
    }

    @GET
    @Path("/{id}")
    public Emprestimo buscarPorId(@PathParam("id") @Positive long id) {
        return repository.buscarPorId(id).orElseThrow(NotFoundException::new);
    }

    @GET
    @Path("/atrasados")
    public List<Emprestimo> buscarAtrasados() {
        return repository.buscarAtrasados();
    }

    @GET
    @Path("/mais-emprestados-mes")
    public List<ContagemEmprestimosDTO> buscarMaisEmprestadosNoMes(
            @QueryParam("limite") @DefaultValue("5") @Positive @Max(100) int limite) {
        return repository.buscarMaisEmprestadosNoMes(limite);
    }
}
