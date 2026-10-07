package tcc.moderno.emprestimo.services;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tcc.moderno.emprestimo.clients.UsuarioLegadoClient;

@ApplicationScoped
public class ContextoUsuarioService {
    private final UsuarioLegadoClient cliente;

    @Inject
    public ContextoUsuarioService(@RestClient UsuarioLegadoClient cliente) {
        this.cliente = cliente;
    }

    /** Sem cache: cada processamento consulta o tipo atual, como no legado. */
    public String consultarTipoAtual(long idUsuario) {
        if (idUsuario <= 0) throw new IllegalArgumentException("Identificador de usuario invalido");
        try (var resposta = cliente.consultar(idUsuario)) {
            if (resposta.getStatus() != 200 || resposta.getMediaType() == null
                    || !resposta.getMediaType().isCompatible(MediaType.APPLICATION_JSON_TYPE)) {
                throw new IllegalStateException("Consulta de contexto de usuario nao confirmada");
            }
            JsonNode dados = resposta.readEntity(JsonNode.class);
            if (dados == null || !dados.path("idUsuario").isIntegralNumber()
                    || !dados.path("idUsuario").canConvertToLong()
                    || dados.path("idUsuario").longValue() != idUsuario
                    || !dados.path("tipoUsuario").isTextual()
                    || dados.path("tipoUsuario").textValue().isBlank()
                    || dados.path("tipoUsuario").textValue().length() > 20) {
                throw new IllegalStateException("Resposta de contexto de usuario invalida");
            }
            // Tipos nao reconhecidos seguem a regra padrao existente, sem normalizacao.
            return dados.path("tipoUsuario").textValue();
        }
    }
}
