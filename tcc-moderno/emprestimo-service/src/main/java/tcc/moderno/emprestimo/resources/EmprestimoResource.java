package tcc.moderno.emprestimo.resources;

import jakarta.inject.Inject;
import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import tcc.moderno.emprestimo.models.Emprestimo;
import tcc.moderno.emprestimo.repositories.EmprestimoRepository;

@Path("/emprestimos")
@Produces(MediaType.APPLICATION_JSON)
public class EmprestimoResource {

    private final EmprestimoRepository repository;

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
}
