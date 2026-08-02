package tcc.legado.ejb.emprestimo;

import tcc.legado.model.Emprestimo;
import tcc.legado.service.EmprestimoService;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import java.util.List;

@Stateless
public class EmprestimoEJB implements IEmprestimoEJB {

    @Inject
    private EmprestimoService emprestimoService;

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Emprestimo criarEmprestimo(Long idLivro, String matricula) {
        // VENENO: validação extra no EJB (duplicada)
        if (idLivro == null || idLivro <= 0) {
            throw new RuntimeException("ID do livro inválido");
        }
        if (matricula == null || matricula.trim().isEmpty()) {
            throw new RuntimeException("Matrícula é obrigatória");
        }
        return emprestimoService.criarEmprestimo(idLivro, matricula);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void registrarDevolucao(Long idEmprestimo) {
        if (idEmprestimo == null || idEmprestimo <= 0) {
            throw new RuntimeException("ID do empréstimo inválido");
        }
        emprestimoService.registrarDevolucao(idEmprestimo);
    }

    @Override
    public List<Emprestimo> listarTodos() {
        return emprestimoService.listarTodos();
    }

    @Override
    public Emprestimo buscarPorId(Long id) {
        return emprestimoService.buscarPorId(id);
    }

    @Override
    public List<Emprestimo> buscarAtrasados() {
        return emprestimoService.buscarAtrasados();
    }
}
