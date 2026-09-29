package tcc.legado.job;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.util.logging.Logger;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

@Singleton
@Startup
public class QuartzInitializer {

    private static final Logger LOG = Logger.getLogger(QuartzInitializer.class.getName());

    @PostConstruct
    public void init() {
        try {
            LOG.info("Inicializando Quartz Scheduler...");

            // Obtém o scheduler padrão
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();

            LOG.info("Quartz Scheduler iniciado com sucesso.");

            // Agenda os jobs
            agendarJobs(scheduler);

        } catch (Exception e) {
            LOG.severe("Falha ao inicializar o Quartz: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void agendarJobs(Scheduler scheduler) throws Exception {
        scheduler.scheduleJob(newJob(CoordenacaoEmprestimoJob.class).withIdentity("Multas", "Sistema")
                .usingJobData("multas",true).build(), newTrigger().withIdentity("Multas", "Sistema")
                .withSchedule(cronSchedule(System.getProperty("emprestimo.multas.cron","0 0 0 * * ?"))).build());
        scheduler.scheduleJob(newJob(CoordenacaoEmprestimoJob.class).withIdentity("Recuperacao", "Sistema")
                .usingJobData("multas",false).build(), newTrigger().withIdentity("Recuperacao", "Sistema")
                .withSchedule(cronSchedule("0/30 * * * * ?")).build());
        // --- Job 2: AtualizarMaisEmprestadosJob (a cada 5 minutos) ---
        JobDetail destaqueJob = newJob(AtualizarMaisEmprestadosJob.class)
                .withIdentity("AtualizarMaisEmprestadosJob", "Sistema")
                .build();

        Trigger destaqueTrigger = newTrigger()
                .withIdentity("triggerMaisEmprestados", "Sistema")
                .withSchedule(cronSchedule("0 */5 * * * ?")) // a cada 5 minutos
                .build();

        scheduler.scheduleJob(destaqueJob, destaqueTrigger);

        LOG.info("Jobs agendados com sucesso!");
    }
}
