package tcc.legado.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import tcc.legado.ejb.emprestimo.IEmprestimoEJB;
import tcc.legado.model.Emprestimo;

import javax.naming.InitialContext;
import java.util.List;
import java.util.logging.Logger;

public class MultaDiariaJob implements Job {

    private static final Logger LOG = Logger.getLogger(MultaDiariaJob.class.getName());

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LOG.info("Iniciando MultaDiariaJob...");

        try {
            // Lookup do EJB (acoplamento forte com JNDI)
            InitialContext ctx = new InitialContext();
            IEmprestimoEJB emprestimoEJB = (IEmprestimoEJB) ctx.lookup(
                    "java:global/monolito-biblioteca/EmprestimoEJB!tcc.legado.ejb.emprestimo.IEmprestimoEJB");

            // Busca empréstimos atrasados
            List<Emprestimo> atrasados = emprestimoEJB.buscarAtrasados();
            LOG.info("Encontrados " + atrasados.size() + " empréstimos atrasados.");

            for (Emprestimo emp : atrasados) {
                LOG.info("Processando empréstimo ID " + emp.getId() + " - multa atual: " + emp.getMulta());
            }

            LOG.info("MultaDiariaJob concluído.");
        } catch (Exception e) {
            LOG.severe("Erro no job: " + e.getMessage());
            throw new JobExecutionException(e);
        }
    }
}
