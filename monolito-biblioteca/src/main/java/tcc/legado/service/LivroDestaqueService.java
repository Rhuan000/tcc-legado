package tcc.legado.service;

import tcc.legado.dao.LivroDestaqueDAO;
import tcc.legado.model.LivroDestaque;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.List;

@Dependent
public class LivroDestaqueService {

    @Inject
    private LivroDestaqueDAO dao;

    public List<LivroDestaque> listarTodos() {
        return dao.listarTodos();
    }

    public List<LivroDestaque> listarAtivos() {
        return dao.listarAtivos();
    }

    public LivroDestaque buscarPorId(Long id) {
        if (id == null || id <= 0) {
            throw new RuntimeException("ID do destaque inválido");
        }
        return dao.buscarPorId(id);
    }

    public void salvar(LivroDestaque livroDestaque) {
        validar(livroDestaque);
        dao.salvar(livroDestaque);
    }

    public void atualizar(LivroDestaque livroDestaque) {
        if (livroDestaque.getId() == null) {
            throw new RuntimeException("ID do destaque é obrigatório");
        }
        validar(livroDestaque);
        dao.atualizar(livroDestaque);
    }

    public void excluir(Long id) {
        if (id == null || id <= 0) {
            throw new RuntimeException("ID do destaque inválido");
        }
        dao.excluir(id);
    }

    public void visualizar(Long id) {
        if (id == null || id <= 0) {
            throw new RuntimeException("ID do destaque inválido");
        }
        dao.incrementarVisualizacoes(id);
    }

    private void validar(LivroDestaque livroDestaque) {
        if (livroDestaque == null) {
            throw new RuntimeException("Destaque inválido");
        }
        if (livroDestaque.getIdLivro() == null || livroDestaque.getIdLivro() <= 0) {
            throw new RuntimeException("ID do livro é obrigatório");
        }
        if (livroDestaque.getTitulo() == null || livroDestaque.getTitulo().trim().isEmpty()) {
            throw new RuntimeException("Título do destaque é obrigatório");
        }
        if (livroDestaque.getDesconto() != null && (livroDestaque.getDesconto() < 0 || livroDestaque.getDesconto() > 100)) {
            throw new RuntimeException("Desconto deve estar entre 0 e 100");
        }
    }
}
