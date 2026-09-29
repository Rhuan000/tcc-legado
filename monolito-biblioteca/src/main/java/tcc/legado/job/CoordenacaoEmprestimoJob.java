package tcc.legado.job;
import org.quartz.*;
import tcc.legado.service.CoordenadorEmprestimo;

@DisallowConcurrentExecution
public class CoordenacaoEmprestimoJob implements Job {
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            CoordenadorEmprestimo coordenador=new CoordenadorEmprestimo();
            if(context.getMergedJobDataMap().getBoolean("multas")) coordenador.atualizarMultas();
            else coordenador.recuperarPendentes();
        } catch(RuntimeException e){throw new JobExecutionException(e);}
    }
}
