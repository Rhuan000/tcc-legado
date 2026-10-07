package tcc.moderno.emprestimo.jobs;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDate;
import java.time.ZoneId;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import tcc.moderno.emprestimo.services.AtualizacaoMultasService;

@ApplicationScoped
public class AtualizacaoMultasJob {
    @Inject AtualizacaoMultasService multas;
    @ConfigProperty(name = "emprestimo.multas.time-zone") String fuso;

    @Scheduled(identity = "atualizacao-multas", cron = "${emprestimo.multas.cron}",
            timeZone = "${emprestimo.multas.time-zone}", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    void executar() {
        multas.atualizar(LocalDate.now(ZoneId.of(fuso)));
    }
}
