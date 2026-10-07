package tcc.moderno.emprestimo.clients;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.util.Map;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class UsuarioLegadoWireMock implements QuarkusTestResourceLifecycleManager {
    public static final String TOKEN = "credencial-exclusiva-usuarios-testes-32";
    public static WireMockServer servidor;

    @Override public Map<String, String> start() {
        servidor = new WireMockServer(wireMockConfig().dynamicPort());
        servidor.start();
        return Map.of("quarkus.rest-client.legado-usuarios.url", servidor.baseUrl() + "/monolito-biblioteca",
                "quarkus.rest-client.legado-usuarios.read-timeout", "200",
                "legado.usuarios.integracao.token", TOKEN,
                "legado.usuarios.allow-local-http", "true",
                "quarkus.rest-client.feriado-api.url", servidor.baseUrl());
    }

    @Override public void stop() {
        if (servidor != null) servidor.stop();
    }
}
