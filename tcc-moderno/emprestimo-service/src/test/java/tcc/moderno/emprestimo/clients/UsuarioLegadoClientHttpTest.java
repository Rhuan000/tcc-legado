package tcc.moderno.emprestimo.clients;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tcc.moderno.emprestimo.services.ContextoUsuarioService;
import static tcc.moderno.emprestimo.clients.UsuarioLegadoWireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestProfile(UsuarioLegadoClientHttpTest.SemBanco.class)
@QuarkusTestResource(value = UsuarioLegadoWireMock.class, restrictToAnnotatedClass = true)
class UsuarioLegadoClientHttpTest {
    public static class SemBanco implements QuarkusTestProfile {
        @Override public Map<String, String> getConfigOverrides() {
            // O pool existe para satisfazer a injecao dos repositorios, mas este
            // teste HTTP nao abre conexoes nem usa banco (porta local sem servidor).
            return Map.of("quarkus.datasource.devservices.enabled", "false",
                    "quarkus.datasource.jdbc.url", "jdbc:postgresql://127.0.0.1:1/nao_utilizado",
                    "quarkus.datasource.jdbc.initial-size", "0",
                    "quarkus.datasource.jdbc.min-size", "0");
        }
    }

    @Inject ContextoUsuarioService usuarios;
    private static final String ROTA = "/monolito-biblioteca/integracao/usuarios/9/contexto";

    @BeforeEach void preparar() { servidor.resetAll(); }

    @Test void enviaTokenNaRotaCorretaEConsultaNovamenteAposMudancaDeTipo() {
        servidor.stubFor(get(urlEqualTo(ROTA)).withHeader("X-Integration-Token", equalTo(TOKEN))
                .willReturn(okJson("{\"idUsuario\":9,\"tipoUsuario\":\"ALUNO\"}")));
        assertEquals("ALUNO", usuarios.consultarTipoAtual(9));
        servidor.stubFor(get(urlEqualTo(ROTA)).withHeader("X-Integration-Token", equalTo(TOKEN))
                .willReturn(okJson("{\"idUsuario\":9,\"tipoUsuario\":\"PROFESSOR\"}")));
        assertEquals("PROFESSOR", usuarios.consultarTipoAtual(9));
        servidor.verify(2, getRequestedFor(urlEqualTo(ROTA)).withHeader("X-Integration-Token", equalTo(TOKEN)));
    }

    @ParameterizedTest
    @ValueSource(ints = {401, 403, 404, 500, 503})
    void erroHttpNaoViraTipoPadrao(int status) {
        servidor.stubFor(get(urlEqualTo(ROTA)).willReturn(aResponse().withStatus(status)));
        assertThrows(RuntimeException.class, () -> usuarios.consultarTipoAtual(9));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "null", "nao-json", "{\"idUsuario\":8,\"tipoUsuario\":\"ALUNO\"}",
            "{\"idUsuario\":9,\"tipoUsuario\":\" \"}", "{\"idUsuario\":9,\"tipoUsuario\":42}"})
    void rejeitaRespostaInvalida(String json) {
        servidor.stubFor(get(urlEqualTo(ROTA)).willReturn(okJson(json)));
        assertThrows(RuntimeException.class, () -> usuarios.consultarTipoAtual(9));
    }

    @Test void bloqueiaRedirectSemEnviarSegredoAoDestino() {
        servidor.stubFor(get(urlEqualTo(ROTA)).willReturn(aResponse().withStatus(302)
                .withHeader("Location", servidor.baseUrl() + "/outro-destino")));
        assertThrows(RuntimeException.class, () -> usuarios.consultarTipoAtual(9));
        servidor.verify(0, getRequestedFor(urlEqualTo("/outro-destino")));
    }

    @Test void limitaTempoDeEspera() {
        servidor.stubFor(get(urlEqualTo(ROTA)).willReturn(okJson("{\"idUsuario\":9,\"tipoUsuario\":\"ALUNO\"}")
                .withFixedDelay(800)));
        assertThrows(RuntimeException.class, () -> usuarios.consultarTipoAtual(9));
    }

    @Test void rejeitaHtmlMesmoComStatus200() {
        servidor.stubFor(get(urlEqualTo(ROTA)).willReturn(ok("<html>Login</html>").withHeader("Content-Type", "text/html")));
        assertThrows(RuntimeException.class, () -> usuarios.consultarTipoAtual(9));
    }
}
