package tcc.legado.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import tcc.legado.ejb.emprestimo.IEmprestimoEJB;

import javax.naming.InitialContext;
import java.util.logging.Logger;

public class MultaDiariaJob implements Job {

    private static final Logger LOG = Logger.getLogger(MultaDiariaJob.class.getName());

    protected IEmprestimoEJB getEmprestimoEJB() throws Exception {
        InitialContext ctx = new InitialContext();
        return (IEmprestimoEJB) ctx.lookup(
                "java:global/monolito-biblioteca/EmprestimoEJB!tcc.legado.ejb.emprestimo.IEmprestimoEJB");
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LOG.info("Iniciando MultaDiariaJob...");

        try {
            // Lookup do EJB (acoplamento forte com JNDI)
            IEmprestimoEJB emprestimoEJB = getEmprestimoEJB();

            // Busca empréstimos atrasados
            emprestimoEJB.atualizarMultasAtrasadas();

            LOG.info("MultaDiariaJob concluído.");
        } catch (Exception e) {
            LOG.log(java.util.logging.Level.SEVERE, "Erro no job de multas", e);
            throw new JobExecutionException(e);
        }
    }
}
