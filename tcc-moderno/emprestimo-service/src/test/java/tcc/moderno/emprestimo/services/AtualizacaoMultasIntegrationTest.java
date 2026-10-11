package tcc.moderno.emprestimo.services;

import io.quarkus.scheduler.Scheduler;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tcc.moderno.emprestimo.clients.UsuarioLegadoWireMock;
import static tcc.moderno.emprestimo.clients.UsuarioLegadoWireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestProfile(AtualizacaoMultasIntegrationTest.Perfil.class)
@QuarkusTestResource(value = UsuarioLegadoWireMock.class, restrictToAnnotatedClass = true)
class AtualizacaoMultasIntegrationTest {
    public static class Perfil implements QuarkusTestProfile {
        @Override public Map<String, String> getConfigOverrides() {
            return Map.of("emprestimo.multas.cron", "0/1 * * * * ?", "quarkus.scheduler.start-mode", "halted");
        }
    }

    @Inject DataSource banco;
    @Inject AtualizacaoMultasService multas;
    @Inject ContextoUsuarioService usuarios;
    @Inject Scheduler scheduler;
    private static final LocalDate REFERENCIA = LocalDate.of(2026, 10, 6);
    private static final String ROTA = "/monolito-biblioteca/integracao/usuarios/9/contexto";

    @BeforeEach void preparar() throws Exception {
        scheduler.pause();
        servidor.resetAll();
        servidor.stubFor(get(urlPathMatching("/api/feriados/v1/[0-9]+"))
                .willReturn(okJson("[]")));
        try (var c = banco.getConnection(); var s = c.createStatement()) {
            s.executeUpdate("DELETE FROM emprestimo_operacao");
            s.executeUpdate("DELETE FROM emprestimo");
            s.executeUpdate("INSERT INTO emprestimo(id,id_livro,id_usuario,data_emprestimo,data_prevista_devolucao,multa) "
                    + "VALUES(100,7,9,'2026-09-21','2026-10-02',7.00)");
        }
    }

    private void usuario(String tipo) {
        servidor.stubFor(get(urlEqualTo(ROTA)).withHeader("X-Integration-Token", equalTo(TOKEN))
                .willReturn(okJson("{\"idUsuario\":9,\"tipoUsuario\":\"" + tipo + "\"}")));
    }

    private BigDecimal multa(long id) throws Exception {
        try (var c = banco.getConnection(); var s = c.prepareStatement("SELECT multa FROM emprestimo WHERE id=?")) {
            s.setLong(1, id);
            try (var r = s.executeQuery()) { assertTrue(r.next()); return r.getBigDecimal(1); }
        }
    }

    @Test void consultaTipoAtualSemCacheERecalculaSemAcumular() throws Exception {
        usuario("ALUNO");
        multas.atualizar(REFERENCIA);
        assertEquals(new BigDecimal("4.00"), multa(100));
        usuario("PROFESSOR");
        multas.atualizar(REFERENCIA);
        multas.atualizar(REFERENCIA);
        assertEquals(new BigDecimal("1.00"), multa(100));
        servidor.verify(3, getRequestedFor(urlEqualTo(ROTA)).withHeader("X-Integration-Token", equalTo(TOKEN)));
        servidor.verify(0, getRequestedFor(urlPathMatching("/feriados/.*"))
                .withHeader("X-Integration-Token", matching(".*")));
    }

    @ParameterizedTest
    @ValueSource(ints = {401, 403, 404, 500, 503})
    void preservaMultaSobErroHttpERetomaNoProximoCiclo(int status) throws Exception {
        servidor.stubFor(get(urlEqualTo(ROTA)).willReturn(aResponse().withStatus(status)));
        assertThrows(IllegalStateException.class, () -> multas.atualizar(REFERENCIA));
        assertEquals(new BigDecimal("7.00"), multa(100));
        usuario("BOLSISTA");
        multas.atualizar(REFERENCIA);
        assertEquals(new BigDecimal("2.00"), multa(100));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "null", "nao-json", "{\"idUsuario\":8,\"tipoUsuario\":\"ALUNO\"}",
            "{\"idUsuario\":9,\"tipoUsuario\":\" \"}", "{\"idUsuario\":9,\"tipoUsuario\":42}"})
    void rejeitaContextoInvalidoSemAlterarMulta(String json) throws Exception {
        servidor.stubFor(get(urlEqualTo(ROTA)).willReturn(okJson(json)));
        assertThrows(IllegalStateException.class, () -> multas.atualizar(REFERENCIA));
        assertEquals(new BigDecimal("7.00"), multa(100));
    }

    @Test void naoSegueRedirectNemEnviaTokenParaOutroDestino() throws Exception {
        servidor.stubFor(get(urlEqualTo(ROTA)).willReturn(aResponse().withStatus(302)
                .withHeader("Location", servidor.baseUrl() + "/destino-indevido")));
        assertThrows(RuntimeException.class, () -> usuarios.consultarTipoAtual(9));
        servidor.verify(0, getRequestedFor(urlEqualTo("/destino-indevido")));
        assertEquals(new BigDecimal("7.00"), multa(100));
    }

    @Test void limitaEsperaPorRespostaDoLegado() throws Exception {
        servidor.stubFor(get(urlEqualTo(ROTA)).willReturn(okJson("{\"idUsuario\":9,\"tipoUsuario\":\"ALUNO\"}")
                .withFixedDelay(800)));
        assertThrows(IllegalStateException.class, () -> multas.atualizar(REFERENCIA));
        assertEquals(new BigDecimal("7.00"), multa(100));
    }

    @Test void falhaDeUmUsuarioNaoImpedeProcessamentoDosDemais() throws Exception {
        try (var c = banco.getConnection(); var s = c.createStatement()) {
            s.executeUpdate("INSERT INTO emprestimo(id,id_livro,id_usuario,data_emprestimo,data_prevista_devolucao,multa) "
                    + "VALUES(101,7,10,'2026-09-21','2026-10-02',7.00)");
        }
        servidor.stubFor(get(urlEqualTo(ROTA)).willReturn(aResponse().withStatus(404)));
        servidor.stubFor(get(urlEqualTo(ROTA.replace("/9/", "/10/")))
                .willReturn(okJson("{\"idUsuario\":10,\"tipoUsuario\":\"PROFESSOR\"}")));
        assertThrows(IllegalStateException.class, () -> multas.atualizar(REFERENCIA));
        assertEquals(new BigDecimal("7.00"), multa(100));
        assertEquals(new BigDecimal("1.00"), multa(101));
    }

    @Test void naoProcessaDevolvidos() throws Exception {
        try (var c = banco.getConnection(); var s = c.createStatement()) {
            s.executeUpdate("UPDATE emprestimo SET data_devolucao_real='2026-10-05' WHERE id=100");
        }
        multas.atualizar(REFERENCIA);
        assertEquals(new BigDecimal("7.00"), multa(100));
        servidor.verify(0, getRequestedFor(urlEqualTo(ROTA)));
    }

    @Test void preservaMultaDeDevolucaoPendente() throws Exception {
        usuario("ALUNO");
        try (var c = banco.getConnection(); var s = c.createStatement()) {
            s.executeUpdate("INSERT INTO emprestimo_operacao(chave,tipo,id_emprestimo,dados,estado) "
                    + "VALUES('devolucao-100','DEVOLVER',100,'{}','PENDENTE')");
        }
        multas.atualizar(REFERENCIA);
        assertEquals(new BigDecimal("7.00"), multa(100));
    }

    @Test void agendadorRealDisparaConsultaEAtualizaBanco() throws Exception {
        usuario("ALUNO");
        LocalDate hoje = LocalDate.now(ZoneId.of("America/Sao_Paulo"));
        // Vencimento antigo: evita depender do dia da semana e do dia de execução do teste.
        try (var c = banco.getConnection(); var s = c.prepareStatement(
                "UPDATE emprestimo SET data_prevista_devolucao=?, multa=0 WHERE id=100")) {
            s.setObject(1, hoje.minusDays(14));
            s.executeUpdate();
        }
        try {
            scheduler.resume();
            long limite = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(10);
            while (multa(100).signum() == 0 && System.nanoTime() < limite) Thread.sleep(100);
            assertTrue(multa(100).signum() > 0, "O agendador deve atualizar a multa sem invocacao manual");
            servidor.verify(getRequestedFor(urlEqualTo(ROTA)).withHeader("X-Integration-Token", equalTo(TOKEN)));
        } finally {
            scheduler.pause();
        }
    }
}
