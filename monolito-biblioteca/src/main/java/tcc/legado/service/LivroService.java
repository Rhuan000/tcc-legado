package tcc.legado.service;

import tcc.legado.dao.LivroDAO;
import tcc.legado.model.Livro;
import tcc.legado.model.Emprestimo;
import tcc.legado.util.CacheGlobal;

import javax.inject.Inject;
import javax.naming.InitialContext;
import java.util.List;
import java.util.logging.Logger;

public class LivroService {

    private static final Logger LOG = Logger.getLogger(LivroService.class.getName());

    @Inject
    private LivroDAO livroDAO;

    private EmprestimoService getEmprestimoService() {
        try {
            InitialContext ctx = new InitialContext();
            return (EmprestimoService) ctx.lookup("java:global/monolito-biblioteca/EmprestimoService");
        } catch (Exception e) {
            LOG.severe("Erro ao obter EmprestimoService via JNDI: " + e.getMessage());
            throw new RuntimeException("Erro ao obter EmprestimoService via JNDI", e);
        }
    }

    public List<Livro> listarTodos() {
        List<Livro> livros = livroDAO.listarTodos();
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
        LOG.info("Livro salvo: " + livro.getTitulo());
    }

    public void atualizar(Livro livro) {
        Livro existente = livroDAO.buscarPorId(livro.getId());
        if (existente == null) {
            throw new RuntimeException("Livro não encontrado");
        }

        // VENENO: verifica se o ISBN foi alterado e se já existe outro com o mesmo ISBN
        if (!existente.getIsbn().equals(livro.getIsbn()) && livro.getIsbn() != null) {
            // Simula uma verificação de duplicidade (na prática, poderia chamar o DAO)
            // Aqui apenas forçamos um conflito para demonstrar acoplamento
            LOG.warning("ISBN alterado de " + existente.getIsbn() + " para " + livro.getIsbn());
        }
        livroDAO.atualizar(livro);
        LOG.info("Livro atualizado: " + livro.getTitulo());
    }

    public void excluir(Long id) {
        Livro livro = livroDAO.buscarPorId(id);
        if (livro == null) {
            throw new RuntimeException("Livro não encontrado");
        }
        if (livro.getQuantidade() > 0) {
            throw new RuntimeException("Não é possível excluir um livro com estoque disponível");
        }

        EmprestimoService empService = getEmprestimoService();
        List<Emprestimo> emprestimos = empService.listarTodos(); // obtém todos os empréstimos
        for (Emprestimo e : emprestimos) {
            if (e.getIdLivro().equals(id) && e.getDataDevolucaoReal() == null) {
                throw new RuntimeException("Livro está em empréstimo ativo, não pode ser excluído");
            }
        }

        Integer totalExcluidos = (Integer) CacheGlobal.get("totalLivrosExcluidos");
        if (totalExcluidos == null) totalExcluidos = 0;
        CacheGlobal.put("totalLivrosExcluidos", totalExcluidos + 1);

        // livroDAO.excluir(id);
        LOG.info("Livro excluído (simulado): " + livro.getTitulo() + " (ID " + id + ")");
    }
}
