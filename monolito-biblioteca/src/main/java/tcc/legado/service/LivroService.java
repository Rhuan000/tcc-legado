package tcc.legado.service;

import tcc.legado.dao.LivroDAO;
import tcc.legado.model.Livro;
import tcc.legado.dao.EmprestimoDAO;
import tcc.legado.exception.ValidacaoException;
import tcc.legado.util.CacheGlobal;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.List;
import java.util.logging.Logger;

@Dependent
public class LivroService {

    private static final Logger LOG = Logger.getLogger(LivroService.class.getName());

    @Inject
    private LivroDAO livroDAO;

    @Inject
    private EmprestimoDAO emprestimoDAO;

    public List<Livro> listarTodos() {
        List<Livro> livros = livroDAO.listarTodos();
        livros.sort((l1, l2) -> l1.getTitulo().compareToIgnoreCase(l2.getTitulo()));
        return livros;
    }

    public Livro buscarPorId(Long id) {
        return livroDAO.buscarPorId(id);
    }

    private void validar(Livro livro) {
        if (livro.getTitulo() == null || livro.getTitulo().trim().isEmpty()) {
            throw new ValidacaoException("Título é obrigatório");
        }
        if (livro.getQuantidade() == null || livro.getQuantidade() < 0) {
            throw new ValidacaoException("Quantidade inválida: informe zero ou um valor positivo");
        }
        String isbn = livro.getIsbn();
        if (isbn == null || isbn.trim().isEmpty()) {
            livro.setIsbn(null);
        } else if (!isbn.matches("^[0-9-]+$")) {
            throw new ValidacaoException("ISBN inválido");
        }
    }

    public void salvar(Livro livro) {
        validar(livro);
        livroDAO.salvar(livro);
        LOG.info("Livro salvo: " + livro.getTitulo());
    }

    public void atualizar(Livro livro) {
        validar(livro);
        if (livro.getId() == null || livroDAO.buscarPorId(livro.getId()) == null) {
            throw new ValidacaoException("Livro não encontrado");
        }
        livroDAO.atualizar(livro);
        LOG.info("Livro atualizado: " + livro.getTitulo());
    }

    public void excluir(Long id) {
        Livro livro = livroDAO.buscarPorId(id);
        if (livro == null) {
            throw new ValidacaoException("Livro não encontrado");
        }
        if (livro.getQuantidade() > 0) {
            throw new ValidacaoException("Não é possível excluir um livro com estoque disponível");
        }

        if (emprestimoDAO.existeAtivoPorLivro(id)) {
            throw new ValidacaoException("Livro está em empréstimo ativo, não pode ser excluído");
        }

        livroDAO.excluir(id);
        Integer totalExcluidos = (Integer) CacheGlobal.get("totalLivrosExcluidos");
        if (totalExcluidos == null) totalExcluidos = 0;
        CacheGlobal.put("totalLivrosExcluidos", totalExcluidos + 1);

        LOG.info("Livro excluído: " + livro.getTitulo() + " (ID " + id + ")");
    }
}
