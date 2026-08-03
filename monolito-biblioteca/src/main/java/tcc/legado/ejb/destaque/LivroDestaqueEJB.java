package tcc.legado.ejb.destaque;

import tcc.legado.model.LivroDestaque;
import tcc.legado.service.LivroDestaqueService;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import java.util.List;

@Stateless
public class LivroDestaqueEJB implements ILivroDestaqueEJB {

    @Inject
    private LivroDestaqueService service;

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<LivroDestaque> listarTodos() {
        return service.listarTodos();
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<LivroDestaque> listarAtivos() {
        return service.listarAtivos();
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public LivroDestaque buscarPorId(Long id) {
        return service.buscarPorId(id);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void salvar(LivroDestaque livroDestaque) {
        service.salvar(livroDestaque);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void atualizar(LivroDestaque livroDestaque) {
        service.atualizar(livroDestaque);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void excluir(Long id) {
        service.excluir(id);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void visualizar(Long id) {
        service.visualizar(id);
    }
}
