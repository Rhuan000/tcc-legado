package tcc.moderno.emprestimo.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tcc.moderno.emprestimo.clients.FeriadoClient;
import tcc.moderno.emprestimo.dtos.FeriadoResponseDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Calendário: consulta e cache de feriados")
class CalendarioServiceTest {

    @Test
    @DisplayName("Consulta o cliente uma vez para chamadas repetidas do mesmo ano")
    void deveUsarCachePorAno() {
        AtomicInteger consultas = new AtomicInteger();
        FeriadoClient cliente = ano -> {
            consultas.incrementAndGet();
            return List.of(feriado("2026-01-01"), feriado("2026-04-21"));
        };
        CalendarioService calendario = new CalendarioService(cliente);

        List<LocalDate> primeira = calendario.buscarFeriados(2026);
        List<LocalDate> segunda = calendario.buscarFeriados(2026);

        assertEquals(List.of(LocalDate.parse("2026-01-01"), LocalDate.parse("2026-04-21")),
                primeira);
        assertEquals(primeira, segunda);
        assertEquals(1, consultas.get());
    }

    @Test
    @DisplayName("Mantém entradas independentes para anos diferentes")
    void deveSepararCachePorAno() {
        AtomicInteger consultas = new AtomicInteger();
        FeriadoClient cliente = ano -> {
            consultas.incrementAndGet();
            return List.of(feriado(ano + "-01-01"));
        };
        CalendarioService calendario = new CalendarioService(cliente);

        assertEquals(List.of(LocalDate.parse("2026-01-01")),
                calendario.buscarFeriados(2026));
        assertEquals(List.of(LocalDate.parse("2027-01-01")),
                calendario.buscarFeriados(2027));
        calendario.buscarFeriados(2026);

        assertEquals(2, consultas.get());
    }

    @Test
    @DisplayName("Não armazena falhas e tenta consultar novamente")
    void naoDeveColocarFalhaNoCache() {
        AtomicInteger consultas = new AtomicInteger();
        FeriadoClient cliente = ano -> {
            if (consultas.incrementAndGet() == 1) {
                throw new IllegalStateException("API indisponível");
            }
            return List.of(feriado("2026-01-01"));
        };
        CalendarioService calendario = new CalendarioService(cliente);

        assertThrows(IllegalStateException.class, () -> calendario.buscarFeriados(2026));
        assertEquals(List.of(LocalDate.parse("2026-01-01")),
                calendario.buscarFeriados(2026));
        assertEquals(2, consultas.get());
    }

    @Test
    @DisplayName("Remove feriados duplicados recebidos da API")
    void deveRemoverFeriadosDuplicados() {
        FeriadoClient cliente = ano -> List.of(
                feriado("2026-01-01"), feriado("2026-01-01"));
        CalendarioService calendario = new CalendarioService(cliente);

        assertEquals(List.of(LocalDate.parse("2026-01-01")),
                calendario.buscarFeriados(2026));
    }

    private FeriadoResponseDTO feriado(String data) {
        return new FeriadoResponseDTO(LocalDate.parse(data), "Feriado", "national");
    }
}
