
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
        if (livro.getTitulo() == null || livro.getTitulo().trim().isEmpty()) {
            throw new RuntimeException("Título é obrigatório");
        }
        livroService.salvar(livro);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void atualizar(Livro livro) {
        if (livro.getId() == null) {
            throw new RuntimeException("ID do livro é obrigatório para atualização");
        }
        livroService.atualizar(livro);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void excluir(Long id) {
        if (id == null) {
            throw new RuntimeException("ID não informado");
        }
        // Poderia verificar se o livro está em um empréstimo ativo
        livroService.excluir(id);
    }

    @Override
    public List<Livro> listarTodos() {
        return livroService.listarTodos();
    }

    @Override
    public Livro buscarPorId(Long id) {
        return livroService.buscarPorId(id);
    }
}
