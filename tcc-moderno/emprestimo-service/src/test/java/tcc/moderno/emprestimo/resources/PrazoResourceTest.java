package tcc.moderno.emprestimo.resources;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import tcc.moderno.emprestimo.clients.FeriadoApiWireMock;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
@QuarkusTestResource(value = FeriadoApiWireMock.class, restrictToAnnotatedClass = true)
class PrazoResourceTest {

    @Test
    void deveCalcularPrazoComFeriadosDoAnoDoEmprestimo() {
        given()
                .contentType("application/json")
                .body("""
                        {
                          "dataEmprestimo": "2026-01-01",
                          "tipoUsuario": "ALUNO"
                        }
                        """)
                .when()
                .post("/prazos/calcular")
                .then()
                .statusCode(200)
                .body("dataPrevista", is("2026-01-12"));
    }

    @Test
    void deveRejeitarRequisicaoSemCamposObrigatorios() {
        given()
                .contentType("application/json")
                .body("{}")
                .when()
                .post("/prazos/calcular")
                .then()
                .statusCode(400);
    }
}
