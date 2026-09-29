package tcc.moderno.emprestimo.resources;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class EmprestimoResourceTest {

    @Inject
    DataSource dataSource;

    private LocalDate hoje;

    @BeforeEach
    void prepararDados() throws SQLException {
        try (var connection = dataSource.getConnection(); var statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM emprestimo");
            try (var result = statement.executeQuery("SELECT CURRENT_DATE")) {
                result.next();
                hoje = result.getObject(1, LocalDate.class);
            }
            statement.executeUpdate("""
                    INSERT INTO emprestimo
                        (id, id_livro, id_usuario, data_emprestimo,
                         data_prevista_devolucao, data_devolucao_real, multa)
                    VALUES
                        (30, 7, 8, DATE '2026-01-01', CURRENT_DATE + 1, NULL, 0),
                        (10, 7, 9, DATE '2026-01-01', CURRENT_DATE - 1, NULL, 4.25),
                        (40, 8, 9, DATE '2026-01-01', CURRENT_DATE - 2, CURRENT_DATE, 2),
                        (20, 8, 8, DATE '2026-01-01', CURRENT_DATE, NULL, NULL)
                    """);
        }
    }

    @Test
    void deveListarOrdenadoPorId() {
        given().header("X-Integration-Token", "token-exclusivo-teste").when().get("/emprestimos").then().statusCode(200)
                .body("id", contains(10, 20, 30, 40));
    }

    @Test
    void devePreservarCamposDatasEValorPersistido() {
        given().header("X-Integration-Token", "token-exclusivo-teste").when().get("/emprestimos/10").then().statusCode(200)
                .body("id", is(10), "idLivro", is(7), "idUsuario", is(9),
                        "dataEmprestimo", is("2026-01-01"),
                        "dataPrevistaDevolucao", is(hoje.minusDays(1).toString()),
                        "dataDevolucaoReal", nullValue(), "multa", is(4.25f));
    }

    @Test
    void devePreservarDataDeDevolucao() {
        given().header("X-Integration-Token", "token-exclusivo-teste").when().get("/emprestimos/40").then().statusCode(200)
                .body("dataDevolucaoReal", is(hoje.toString()), "multa", is(2.0f));
    }

    @Test
    void deveInterpretarMultaNulaComoZeroAssimComoLegado() {
        given().header("X-Integration-Token", "token-exclusivo-teste").when().get("/emprestimos/20").then().statusCode(200)
                .body("multa", is(0));
    }

    @Test
    void deveExcluirDevolvidosVencimentoHojeEFuturosDosAtrasados() {
        given().header("X-Integration-Token", "token-exclusivo-teste").when().get("/emprestimos/atrasados").then().statusCode(200)
                .body("id", contains(10));
    }

    @Test
    void deveRetornar404ParaIdInexistente() {
        given().header("X-Integration-Token", "token-exclusivo-teste").when().get("/emprestimos/999").then().statusCode(404);
    }

    @Test
    void deveRejeitarIdNaoPositivo() {
        given().header("X-Integration-Token", "token-exclusivo-teste").when().get("/emprestimos/0").then().statusCode(400);
        given().header("X-Integration-Token", "token-exclusivo-teste").when().get("/emprestimos/-1").then().statusCode(400);
    }

    @Test
    void deveRetornarErroQuandoConsultaFalha() throws SQLException {
        try (var connection = dataSource.getConnection(); var statement = connection.createStatement()) {
            statement.executeUpdate("ALTER TABLE emprestimo RENAME TO emprestimo_indisponivel");
            try {
                given().header("X-Integration-Token", "token-exclusivo-teste").when().get("/emprestimos").then().statusCode(500);
                given().header("X-Integration-Token", "token-exclusivo-teste").when().get("/emprestimos/10").then().statusCode(500);
                given().header("X-Integration-Token", "token-exclusivo-teste").when().get("/emprestimos/atrasados").then().statusCode(500);
            } finally {
                statement.executeUpdate("ALTER TABLE emprestimo_indisponivel RENAME TO emprestimo");
            }
        }
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHaRegistros() throws SQLException {
        try (var connection = dataSource.getConnection(); var statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM emprestimo");
        }
        given().header("X-Integration-Token", "token-exclusivo-teste").when().get("/emprestimos").then().statusCode(200).body("size()", is(0));
        given().header("X-Integration-Token", "token-exclusivo-teste").when().get("/emprestimos/atrasados").then().statusCode(200).body("size()", is(0));
    }
}
