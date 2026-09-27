package tcc.moderno.emprestimo.clients;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class FeriadoApiWireMock implements QuarkusTestResourceLifecycleManager {

    private static WireMockServer servidor;

    @Override
    public Map<String, String> start() {
        servidor = new WireMockServer(wireMockConfig().dynamicPort());
        servidor.start();

        servidor.stubFor(get(urlEqualTo("/feriados/v1/2026"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [
                                  {
                                    "date": "2026-01-01",
                                    "name": "Confraternização mundial",
                                    "type": "national"
                                  }
                                ]
                                """)));

        servidor.stubFor(get(urlEqualTo("/feriados/v1/2027"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("[]")));

        servidor.stubFor(get(urlEqualTo("/feriados/v1/2099"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"message\":\"falha simulada\"}")));

        return Map.of("quarkus.rest-client.feriado-api.url", servidor.baseUrl());
    }

    static void verificarConsulta(int ano) {
        servidor.verify(1, getRequestedFor(urlEqualTo("/feriados/v1/" + ano)));
    }

    @Override
    public void stop() {
        if (servidor != null) {
            servidor.stop();
            servidor = null;
        }
    }
}
