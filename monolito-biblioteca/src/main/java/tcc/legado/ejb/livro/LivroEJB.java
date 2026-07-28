
package tcc.legado.ejb.livro;

import tcc.legado.service.LivroService;
import tcc.legado.model.Livro;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import java.util.List;

@Stateless
public class LivroEJB implements ILivroEJB {

    @Inject
    private LivroService livroService;

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void salvar(Livro livro) {
        // ==========================================
        // VENENO 24: Regra de negócio no EJB (além de chamar o Service)
        // ==========================================
        if (livro.getTitulo() == null || livro.getTitulo().trim().isEmpty()) {
            throw new RuntimeException("Título é obrigatório");
        }
        // Chama o Service que chama o DAO
        livroService.salvar(livro);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void atualizar(Livro livro) {
        // ==========================================
        // VENENO 25: Validação duplicada (no EJB e no Service)
        // ==========================================
        if (livro.getId() == null) {
            throw new RuntimeException("ID do livro é obrigatório para atualização");
        }
        livroService.atualizar(livro);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void excluir(Long id) {
        // ==========================================
        // VENENO 26: Lógica de exclusão com verificação de dependências
        // ==========================================
        if (id == null) {
            throw new RuntimeException("ID não informado");
        }
        // Poderia verificar se o livro está em um empréstimo ativo
        livroService.excluir(id);
    }

    @Override
    public List<Livro> listarTodos() {
        // ==========================================
        // VENENO 27: Chama o Service que chama o DAO
        // ==========================================
        return livroService.listarTodos();
    }

    @Override
    public Livro buscarPorId(Long id) {
        return livroService.buscarPorId(id);
    }
}
