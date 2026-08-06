package tcc.legado.service;

import tcc.legado.dao.EmprestimoDAO;
import tcc.legado.dao.LivroDAO;
import tcc.legado.dao.UsuarioDAO;
import tcc.legado.model.Emprestimo;
import tcc.legado.model.Livro;
import tcc.legado.model.Usuario;
import tcc.legado.util.CacheGlobal;
import tcc.legado.util.FeriadoClient;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.naming.InitialContext;
import java.time.LocalDate;
import java.time.ZoneId;
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
        emp.setDataEmprestimo(new Date());

        // Prazo em DIAS ÚTEIS: 7 para ALUNO, 14 para PROFESSOR, 10 para BOLSISTA
        int diasUteisPrazo;
        switch (usuario.getTipo()) {
            case "PROFESSOR":
                diasUteisPrazo = 14;
                break;
            case "BOLSISTA":
                diasUteisPrazo = 10;
                break;
            default:
                diasUteisPrazo = 7;
                break;
        }

        // VENENO: calcula data prevista consultando API de feriados (via cache)
        Date dataPrevista = calcularDataPrevistaComFeriados(diasUteisPrazo);
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


    private Date calcularDataPrevistaComFeriados(int diasUteis) {
        LocalDate dataAtual = LocalDate.now();
        int ano = dataAtual.getYear();

        // VENENO: verifica se o cache está atualizado para o ano corrente
        // Se não estiver, busca na API externa e atualiza o cache global
        if (CacheGlobal.getAnoCorrente() != ano || CacheGlobal.getFeriados().isEmpty()) {
            LOG.info("Cache de feriados desatualizado ou vazio. Buscando na API para o ano " + ano);
            List<LocalDate> feriados = FeriadoClient.buscarFeriados(ano);
            CacheGlobal.setAnoCorrente(ano);
            CacheGlobal.setFeriados(feriados);
        }

        List<LocalDate> feriados = CacheGlobal.getFeriados();
        LocalDate dataPrevista = dataAtual;

        while (diasUteis > 0) {
            dataPrevista = dataPrevista.plusDays(1);
            // Verifica se é dia útil (segunda a sexta) E não é feriado
            if (isDiaUtil(dataPrevista, feriados)) {
                diasUteis--;
            }
        }

        LOG.info("Data prevista calculada (com feriados): " + dataPrevista);
        return Date.from(dataPrevista.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }


    private boolean isDiaUtil(LocalDate data, List<LocalDate> feriados) {
        // Fim de semana (sábado = 6, domingo = 7)
        if (data.getDayOfWeek().getValue() > 5) {
            return false;
        }
        // Feriado nacional
        return !feriados.contains(data);
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

        double multa = calcularMulta(emp, usuario.getTipo());
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

    // =========================================================
    // CÁLCULO DE MULTA (considerando apenas dias úteis de atraso)
    // =========================================================
    private double calcularMulta(Emprestimo emp, String tipoUsuario) {
        if (emp.getDataDevolucaoReal() == null) {
            return 0.0;
        }

        // Converte java.sql.Date para java.util.Date e depois para LocalDate
        Date dataPrevistaUtil = emp.getDataPrevistaDevolucao(); // já é java.util.Date?
        Date dataDevolucaoUtil = emp.getDataDevolucaoReal();

        // Se forem java.sql.Date, converta:
        LocalDate dataPrevista = new java.sql.Date(dataPrevistaUtil.getTime()).toLocalDate();
        LocalDate dataDevolucao = new java.sql.Date(dataDevolucaoUtil.getTime()).toLocalDate();
        

        // Se devolveu antes ou no dia, sem multa
        if (dataDevolucao.isBefore(dataPrevista) || dataDevolucao.equals(dataPrevista)) {
            return 0.0;
        }

        // VENENO: busca feriados do cache para o ano da data prevista
        int ano = dataPrevista.getYear();
        if (CacheGlobal.getAnoCorrente() != ano || CacheGlobal.getFeriados().isEmpty()) {
            LOG.info("Cache de feriados desatualizado. Buscando para o ano " + ano);
            List<LocalDate> feriados = FeriadoClient.buscarFeriados(ano);
            CacheGlobal.setAnoCorrente(ano);
            CacheGlobal.setFeriados(feriados);
        }
        List<LocalDate> feriados = CacheGlobal.getFeriados();

        // Conta quantos dias úteis (excluindo feriados) de atraso
        long diasUteisAtraso = 0;
        LocalDate cursor = dataPrevista.plusDays(1);
        while (!cursor.isAfter(dataDevolucao)) {
            if (isDiaUtil(cursor, feriados)) {
                diasUteisAtraso++;
            }
            cursor = cursor.plusDays(1);
        }

        if (diasUteisAtraso <= 0) {
            return 0.0;
        }

        // Valor da multa por dia útil de atraso
        double valorDia;
        switch (tipoUsuario) {
            case "PROFESSOR":
                valorDia = 0.50;
                break;
            case "BOLSISTA":
                valorDia = 1.00;
                break;
            default:
                valorDia = 2.00;
                break;
        }

        double multa = diasUteisAtraso * valorDia;
        LOG.info("Multa calculada: " + multa + " (" + diasUteisAtraso + " dias úteis de atraso)");
        return multa;
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
