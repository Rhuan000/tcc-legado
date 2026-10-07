package tcc.moderno.emprestimo.clients;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.core.MultivaluedHashMap;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class UsuarioLegadoAuthTest {
    @ParameterizedTest
    @ValueSource(strings = {"http://legado.interno/usuarios", "http://localhost/usuarios",
            "https://usuario:senha@legado.interno/usuarios", "https://legado.interno/usuarios?token=x",
            "https://legado.interno/usuarios#token", "file:///usuarios"})
    void rejeitaDestinoInseguro(String url) {
        assertThrows(IllegalStateException.class, () -> UsuarioLegadoAuth.validarDestino(URI.create(url), false));
    }

    @Test void aceitaHttpsSemCredencialNaUrl() {
        assertDoesNotThrow(() -> UsuarioLegadoAuth.validarDestino(URI.create("https://legado.interno/usuarios"), false));
    }

    @Test void excecaoLocalNaoLiberaHttpRemoto() {
        assertDoesNotThrow(() -> UsuarioLegadoAuth.validarDestino(URI.create("http://127.0.0.1:8080/usuarios"), true));
        assertThrows(IllegalStateException.class,
                () -> UsuarioLegadoAuth.validarDestino(URI.create("http://192.168.0.2/usuarios"), true));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "curto", "0123456789012345678901234567890123\r\nHeader: valor"})
    void naoEnviaCredencialAusenteFracaOuComQuebraDeLinha(String segredo) {
        var filtro = new UsuarioLegadoAuth();
        filtro.token = Optional.of(segredo);
        var headers = new MultivaluedHashMap<String, Object>();
        var request = (ClientRequestContext) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{ClientRequestContext.class}, (proxy, method, args) -> switch (method.getName()) {
                    case "getUri" -> URI.create("https://legado.interno/usuarios");
                    case "getHeaders" -> headers;
                    default -> throw new UnsupportedOperationException(method.getName());
                });
        assertThrows(IllegalStateException.class, () -> filtro.filter(request));
        assertFalse(headers.containsKey("X-Integration-Token"));
    }
}
