package tcc.legado.service;

import tcc.legado.acl.EmprestimoAcl;
import tcc.legado.dao.EmprestimoDAO;
import tcc.legado.dao.LivroDAO;
import tcc.legado.dao.UsuarioDAO;
import tcc.legado.model.Emprestimo;
import tcc.legado.model.Livro;
import tcc.legado.model.Usuario;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

@Dependent
public class EmprestimoService {

    private static final Logger LOG = Logger.getLogger(EmprestimoService.class.getName());

    @Inject
    private EmprestimoDAO emprestimoDAO;

    @Inject
    private LivroDAO livroDAO;

    @Inject
    private UsuarioDAO usuarioDAO;

    @Inject
    private LivroService livroService; // VENENO: dependência circular

    @Inject
    private EmprestimoAcl emprestimoAcl;

    public Emprestimo criarEmprestimo(Long idLivro, String matricula) {
        Livro livro = livroDAO.buscarPorId(idLivro);
        Usuario usuario = usuarioDAO.buscarPorMatricula(matricula);

        if (livro == null) {
            throw new RuntimeException("Livro não encontrado");
        }
        if (usuario == null) {
            throw new RuntimeException("Usuário não encontrado");
        }
        if (livro.getQuantidade() <= 0) {
            throw new RuntimeException("Livro indisponível");
        }

        Emprestimo emp = new Emprestimo();
        emp.setIdLivro(livro.getId());
        emp.setIdUsuario(usuario.getId());
        Date dataEmprestimo = new Date();
        emp.setDataEmprestimo(dataEmprestimo);

        // A ACL mantém o modelo legado isolado do contrato HTTP do microsserviço.
        Date dataPrevista = emprestimoAcl.calcularDataPrevista(
                dataEmprestimo, usuario.getTipo());
        emp.setDataPrevistaDevolucao(dataPrevista);
        emp.setMulta(0.0);

        emprestimoDAO.salvar(emp);

        // Atualiza quantidade do livro
        livro.setQuantidade(livro.getQuantidade() - 1);
        livroDAO.atualizar(livro);

        // VENENO: chama LivroService para registrar algo (ex: log de empréstimo)
        // Isso cria o ciclo de dependência
        livroService.listarTodos(); // chamada inútil só para forçar o ciclo

        LOG.info("Empréstimo criado com sucesso: ID " + emp.getId());
        return emp;
    }

    public void registrarDevolucao(Long idEmprestimo) {
        Emprestimo emp = emprestimoDAO.buscarPorId(idEmprestimo);
        if (emp == null) {
            throw new RuntimeException("Empréstimo não encontrado");
        }
        if (emp.getDataDevolucaoReal() != null) {
            throw new RuntimeException("Empréstimo já devolvido");
        }

        Usuario usuario = usuarioDAO.buscarPorId(emp.getIdUsuario());
        if (usuario == null) {
            throw new RuntimeException("Usuário do empréstimo não encontrado");
        }

        Date hoje = new Date();
        emp.setDataDevolucaoReal(hoje);

        double multa = emprestimoAcl.calcularMulta(
                emp.getDataPrevistaDevolucao(), usuario.getTipo(), hoje);
        emp.setMulta(multa);

        emprestimoDAO.atualizarDevolucao(emp.getId(), hoje, multa);

        // Atualiza quantidade do livro (devolução)
        Livro livro = livroDAO.buscarPorId(emp.getIdLivro());
        if (livro != null) {
            livro.setQuantidade(livro.getQuantidade() + 1);
            livroDAO.atualizar(livro);
        }

        LOG.info("Devolução registrada: Empréstimo ID " + idEmprestimo + ", Multa: R$ " + multa);
    }

    public void atualizarMultasAtrasadas() {
        Date hoje = new Date();
        for (Emprestimo emp : emprestimoDAO.buscarAtrasados()) {
            Usuario usuario = usuarioDAO.buscarPorId(emp.getIdUsuario());
            if (usuario == null) {
                throw new IllegalStateException("Usuario do emprestimo nao encontrado: " + emp.getId());
            }
            double multa = emprestimoAcl.calcularMulta(
                    emp.getDataPrevistaDevolucao(), usuario.getTipo(), hoje);
            emprestimoDAO.atualizarMulta(emp.getId(), multa);
        }
    }

    // =========================================================
    // MÉTODOS DE CONSULTA (CRUD básico)
    // =========================================================
    public List<Emprestimo> listarTodos() {
        return emprestimoDAO.listarTodos();
    }

    public Emprestimo buscarPorId(Long id) {
        return emprestimoDAO.buscarPorId(id);
    }

    public List<Emprestimo> buscarAtrasados() {
        return emprestimoDAO.buscarAtrasados();
    }
}
