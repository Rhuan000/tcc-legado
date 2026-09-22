package tcc.moderno.emprestimo.resources;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class MultaResourceTest {

    @Test
    void deveConfirmarQueResourceEstaDisponivel() {
        given()
                .when()
                .get("/calcular")
                .then()
                .statusCode(200)
                .body(is("Multa resource disponível"));
    }
}
