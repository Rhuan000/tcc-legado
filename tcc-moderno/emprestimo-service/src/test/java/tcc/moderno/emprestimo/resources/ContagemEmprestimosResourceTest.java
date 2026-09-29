package tcc.moderno.emprestimo.resources;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javax.sql.DataSource;
import java.sql.SQLException;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class ContagemEmprestimosResourceTest {
    private static final String ROTA = "/emprestimos/mais-emprestados-mes";

    @Inject
    DataSource dataSource;

    @BeforeEach
    void prepararDados() throws SQLException {
        try (var connection = dataSource.getConnection(); var statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM emprestimo");
            statement.executeUpdate("""
                    INSERT INTO emprestimo
                        (id, id_livro, id_usuario, data_emprestimo, data_prevista_devolucao, data_devolucao_real)
                    VALUES
                        (1, 7, 1, date_trunc('month', CURRENT_DATE), CURRENT_DATE, NULL),
                        (2, 7, 1, date_trunc('month', CURRENT_DATE) + INTERVAL '1 month - 1 day', CURRENT_DATE, CURRENT_DATE),
                        (3, 8, 1, CURRENT_DATE, CURRENT_DATE, NULL),
                        (4, 8, 1, CURRENT_DATE, CURRENT_DATE, NULL),
                        (5, 9, 1, CURRENT_DATE, CURRENT_DATE, NULL),
                        (6, 10, 1, CURRENT_DATE, CURRENT_DATE, NULL),
                        (7, 11, 1, CURRENT_DATE, CURRENT_DATE, NULL),
                        (8, 12, 1, CURRENT_DATE, CURRENT_DATE, NULL),
                        (9, 99, 1, date_trunc('month', CURRENT_DATE) - INTERVAL '1 day', CURRENT_DATE, NULL),
                        (10, 99, 1, date_trunc('month', CURRENT_DATE) + INTERVAL '1 month', CURRENT_DATE, NULL),
                        (11, 99, 1, CURRENT_DATE - INTERVAL '1 year', CURRENT_DATE, NULL)
                    """);
        }
    }

    @Test
    void deveContarMesDoBancoIncluindoDevolvidosEDesempatarPorId() {
        given().header("X-Integration-Token", "token-exclusivo-teste").queryParam("limite", 100).when().get(ROTA).then().statusCode(200)
                .body("idLivro", contains(7, 8, 9, 10, 11, 12))
                .body("totalEmprestimos", contains(2, 2, 1, 1, 1, 1))
                .body("[0].keySet()", containsInAnyOrder("idLivro", "totalEmprestimos"));
    }

    @Test
    void deveUsarCincoResultadosPorPadrao() {
        given().header("X-Integration-Token", "token-exclusivo-teste").when().get(ROTA).then().statusCode(200)
                .body("idLivro", contains(7, 8, 9, 10, 11));
    }

    @Test
    void deveRespeitarLimiteInformado() {
        given().header("X-Integration-Token", "token-exclusivo-teste").queryParam("limite", 1).when().get(ROTA).then().statusCode(200)
                .body("idLivro", contains(7)).body("totalEmprestimos", contains(2));
    }

    @Test
    void deveRejeitarLimiteInvalido() {
        for (String limite : new String[]{"0", "-1", "101"}) {
            given().header("X-Integration-Token", "token-exclusivo-teste").queryParam("limite", limite).when().get(ROTA).then().statusCode(400);
        }
    }

    @Test
    void deveRetornarVazioSemEmprestimosNoMes() throws SQLException {
        try (var connection = dataSource.getConnection(); var statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM emprestimo WHERE id_livro <> 99");
        }
        given().header("X-Integration-Token", "token-exclusivo-teste").when().get(ROTA).then().statusCode(200).body("size()", is(0));
    }

    @Test
    void naoDeveTransformarFalhaSqlEmListaVazia() throws SQLException {
        try (var connection = dataSource.getConnection(); var statement = connection.createStatement()) {
            statement.executeUpdate("ALTER TABLE emprestimo RENAME TO emprestimo_indisponivel");
            try {
                given().header("X-Integration-Token", "token-exclusivo-teste").when().get(ROTA).then().statusCode(500);
            } finally {
                statement.executeUpdate("ALTER TABLE emprestimo_indisponivel RENAME TO emprestimo");
            }
        }
    }
}
