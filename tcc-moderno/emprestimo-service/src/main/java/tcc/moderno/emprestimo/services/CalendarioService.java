package tcc.moderno.emprestimo.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tcc.moderno.emprestimo.clients.FeriadoClient;
import tcc.moderno.emprestimo.dtos.FeriadoResponseDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@ApplicationScoped
public class CalendarioService {

    private final FeriadoClient feriadoClient;
    private final ConcurrentMap<Integer, List<LocalDate>> feriadosPorAno =
            new ConcurrentHashMap<>();

    @Inject
    public CalendarioService(@RestClient FeriadoClient feriadoClient) {
        this.feriadoClient = feriadoClient;
    }

    /**
     * Obtém os feriados do ano e mantém uma cópia imutável em memória.
     * Se a consulta falhar, o mapa não recebe uma entrada e a próxima chamada
     * poderá tentar novamente.
     */
    public List<LocalDate> buscarFeriados(int ano) {
        return feriadosPorAno.computeIfAbsent(ano, this::consultarFeriados);
    }

    private List<LocalDate> consultarFeriados(int ano) {
        return feriadoClient.buscarFeriados(ano).stream()
                .map(FeriadoResponseDTO::date)
                .distinct()
                .toList();
    }
}
