package tcc.legado.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import tcc.legado.dao.LivroDestaqueDAO;
import tcc.legado.dao.LivroDAO;
import tcc.legado.acl.EmprestimoAcl;
import tcc.legado.model.Livro;
import tcc.legado.model.LivroDestaque;

import java.util.Calendar;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

public class AtualizarMaisEmprestadosJob implements Job {

    private static final Logger LOG = Logger.getLogger(AtualizarMaisEmprestadosJob.class.getName());

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LOG.info("Iniciando AtualizarMaisEmprestadosJob...");

        try {
            LivroDestaqueDAO dao = new LivroDestaqueDAO();

            // Quartz instancia este job fora do CDI, como os DAOs já usados aqui.
            EmprestimoAcl emprestimoAcl = new EmprestimoAcl();
            LivroDAO livroDAO = new LivroDAO();
            List<Livro> topLivros = new ArrayList<>();
            for (Long idLivro : emprestimoAcl.buscarIdsMaisEmprestadosNoMes(5)) {
                Livro livro = livroDAO.buscarPorId(idLivro);
                if (livro == null) {
                    throw new IllegalStateException("Não foi possível carregar o livro " + idLivro);
                }
                topLivros.add(livro);
            }

            // Só substitui destaques após obter a contagem e os dados locais dos livros.
            dao.excluirPorCategoria("MAIS_EMPRESTADO");
            LOG.info("Destaques antigos da categoria 'MAIS_EMPRESTADO' removidos.");

            LOG.info("Encontrados " + topLivros.size() + " livros para destacar.");

            if (topLivros.isEmpty()) {
                LOG.warning("Nenhum empréstimo registrado no mês atual. Nenhum destaque gerado.");
                return;
            }

            // 3. Insere cada um como um registro em livro_destaque
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            Date primeiroDia = cal.getTime();

            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            Date ultimoDia = cal.getTime();

            int posicao = 1;
            for (Livro livro : topLivros) {
                LivroDestaque destaque = new LivroDestaque();
                destaque.setIdLivro(livro.getId());
                destaque.setTitulo("📈 Mais emprestado #" + posicao);
                destaque.setDescricao(livro.getTitulo() + " - " + livro.getAutor());
                destaque.setDesconto(0.0); // sem desconto automático
                destaque.setCategoria("MAIS_EMPRESTADO");
                destaque.setDataInicio(primeiroDia);
                destaque.setDataFim(ultimoDia);
                destaque.setAtivo(true);
                destaque.setVisualizacoes(0);

                dao.salvar(destaque);
                LOG.info("Inserido destaque: " + livro.getTitulo());
                posicao++;
            }

            LOG.info("AtualizarMaisEmprestadosJob concluído com sucesso.");

        } catch (Exception e) {
            LOG.severe("Erro crítico no job: " + e.getMessage());
            throw new JobExecutionException(e);
        }
    }
}
