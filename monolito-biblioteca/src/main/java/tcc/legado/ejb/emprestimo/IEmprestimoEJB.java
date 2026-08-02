package tcc.legado.ejb.emprestimo;

import tcc.legado.model.Emprestimo;
import java.util.List;

public interface IEmprestimoEJB {
    Emprestimo criarEmprestimo(Long idLivro, String matricula);
    void registrarDevolucao(Long idEmprestimo);
    List<Emprestimo> listarTodos();
    Emprestimo buscarPorId(Long id);
    List<Emprestimo> buscarAtrasados();
}
