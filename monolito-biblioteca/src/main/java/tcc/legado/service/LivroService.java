package tcc.legado.service;

import tcc.legado.dao.LivroDAO;
import tcc.legado.model.Livro;

import javax.inject.Inject;
import java.util.List;

public class LivroService {

    @Inject
    private LivroDAO livroDAO;

    public List<Livro> listarTodos() {
        List<Livro> livros = livroDAO.listarTodos();
        // Ordena por título (regra de negócio)
        livros.sort((l1, l2) -> l1.getTitulo().compareToIgnoreCase(l2.getTitulo()));
        return livros;
    }

    public Livro buscarPorId(Long id) {
        return livroDAO.buscarPorId(id);
    }

    public void salvar(Livro livro) {
        if (livro.getQuantidade() == null || livro.getQuantidade() < 0) {
            throw new RuntimeException("Quantidade inválida");
        }
        // Valida ISBN (se existir)
        if (livro.getIsbn() != null && !livro.getIsbn().isEmpty()) {
            if (!livro.getIsbn().matches("^[0-9-]+$")) {
                throw new RuntimeException("ISBN inválido");
            }
        }
        livroDAO.salvar(livro);
    }

    public void atualizar(Livro livro) {
        Livro existente = livroDAO.buscarPorId(livro.getId());
        if (existente == null) {
            throw new RuntimeException("Livro não encontrado");
        }
        // Poderia verificar se o ISBN mudou e se já existe outro com o mesmo ISBN
        livroDAO.atualizar(livro);
    }

    public void excluir(Long id) {
        Livro livro = livroDAO.buscarPorId(id);
        if (livro == null) {
            throw new RuntimeException("Livro não encontrado");
        }
        // Exemplo: não permite excluir se quantidade > 0 (regra de negócio)
        if (livro.getQuantidade() > 0) {
            throw new RuntimeException("Não é possível excluir um livro com estoque disponível");
        }
        // livroDAO.excluir(id);
    }
}
