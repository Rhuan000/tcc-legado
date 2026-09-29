package tcc.moderno.emprestimo.resources;
import io.quarkus.test.junit.*;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static io.restassured.RestAssured.given;

@QuarkusTest
@TestProfile(TokenAusenteTest.SemToken.class)
class TokenAusenteTest {
    public static class SemToken implements QuarkusTestProfile {
        public Map<String,String> getConfigOverrides(){return Map.of("emprestimo.integracao.token","");}
    }
    @Test void falhaFechadoSemSegredoConfigurado(){
        given().get("/emprestimos").then().statusCode(503);
        given().get("/q/health/live").then().statusCode(200);
    }
}
