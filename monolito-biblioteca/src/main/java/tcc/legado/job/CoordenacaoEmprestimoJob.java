package tcc.legado.job;
import org.quartz.*;
import tcc.legado.service.CoordenadorEmprestimo;

@DisallowConcurrentExecution
public class CoordenacaoEmprestimoJob implements Job {
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            new CoordenadorEmprestimo().recuperarPendentes();
        } catch(RuntimeException e){throw new JobExecutionException(e);}
    }
}
