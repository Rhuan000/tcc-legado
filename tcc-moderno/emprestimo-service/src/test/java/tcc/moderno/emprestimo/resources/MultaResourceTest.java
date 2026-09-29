package tcc.moderno.emprestimo.resources;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.common.QuarkusTestResource;
import org.junit.jupiter.api.Test;
import tcc.moderno.emprestimo.clients.FeriadoApiWireMock;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
@QuarkusTestResource(value = FeriadoApiWireMock.class, restrictToAnnotatedClass = true)
class MultaResourceTest {

    @Test
    void deveCalcularMultaComFeriadosDoAnoDoVencimento() {
        given().header("X-Integration-Token", "token-exclusivo-teste")
                .contentType("application/json")
                .body("""
                        {
                          "dataPrevista": "2026-01-01",
                          "tipoUsuario": "ALUNO",
                          "dataReferencia": "2026-01-05"
                        }
                        """)
                .when()
                .post("/multas/calcular")
                .then()
                .statusCode(200)
                .body("valor", is(4.0f));
    }

    @Test
    void deveRejeitarRequisicaoSemCamposObrigatorios() {
        given().header("X-Integration-Token", "token-exclusivo-teste")
                .contentType("application/json")
                .body("{}")
                .when()
                .post("/multas/calcular")
                .then()
                .statusCode(400);
    }

    @Test
    void naoDeveConsultarCalendarioQuandoNaoHaAtraso() {
        given().header("X-Integration-Token", "token-exclusivo-teste")
                .contentType("application/json")
                .body("""
                        {
                          "dataPrevista": "2099-01-05",
                          "tipoUsuario": "ALUNO",
                          "dataReferencia": "2099-01-05"
                        }
                        """)
                .when()
                .post("/multas/calcular")
                .then()
                .statusCode(200)
                .body("valor", is(0.0f));
    }
}
