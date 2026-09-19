package tcc.legado.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import tcc.legado.ejb.emprestimo.IEmprestimoEJB;

import javax.naming.InitialContext;
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
            emprestimoEJB.atualizarMultasAtrasadas();

            LOG.info("MultaDiariaJob concluído.");
        } catch (Exception e) {
            LOG.severe("Erro no job: " + e.getMessage());
            throw new JobExecutionException(e);
        }
    }
}
