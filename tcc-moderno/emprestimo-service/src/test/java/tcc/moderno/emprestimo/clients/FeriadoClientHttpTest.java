package tcc.moderno.emprestimo.clients;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tcc.moderno.emprestimo.dtos.FeriadoResponseDTO;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
@QuarkusTestResource(value = FeriadoApiWireMock.class, restrictToAnnotatedClass = true)
@DisplayName("Cliente HTTP da BrasilAPI")
class FeriadoClientHttpTest {

    @Inject
    @RestClient
    FeriadoClient cliente;

    @Test
    @DisplayName("Usa a rota documentada e desserializa o contrato de feriado")
    void deveConsultarEDesserializarFeriados() {
        List<FeriadoResponseDTO> feriados = cliente.buscarFeriados(2026);

        assertEquals(List.of(new FeriadoResponseDTO(
                LocalDate.of(2026, 1, 1),
                "Confraternização mundial",
                "national")), feriados);
        FeriadoApiWireMock.verificarConsulta(2026);
    }

    @Test
    @DisplayName("Desserializa uma resposta sem feriados")
    void deveDesserializarListaVazia() {
        assertEquals(List.of(), cliente.buscarFeriados(2027));
        FeriadoApiWireMock.verificarConsulta(2027);
    }

    @Test
    @DisplayName("Não transforma erro HTTP da API em resposta válida")
    void devePropagarErroHttp() {
        WebApplicationException erro = assertThrows(
                WebApplicationException.class,
                () -> cliente.buscarFeriados(2099));

        assertEquals(500, erro.getResponse().getStatus());
        FeriadoApiWireMock.verificarConsulta(2099);
    }
}
