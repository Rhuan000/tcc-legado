package tcc.moderno.emprestimo.resources;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.common.QuarkusTestResource;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tcc.moderno.emprestimo.clients.FeriadoApiWireMock;
import javax.sql.DataSource;
import java.util.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@QuarkusTestResource(value=FeriadoApiWireMock.class,restrictToAnnotatedClass=true)
class ComandosEmprestimoResourceTest {
    @Inject DataSource dataSource;
    private io.restassured.specification.RequestSpecification chamada(){
        return given().header("X-Integration-Token","token-exclusivo-teste").contentType("application/json");
    }
    private Map<String,Object> criarDados(String chave){
        return Map.of("chave",chave,"idLivro",7,"idUsuario",9,"tipoUsuario","ALUNO","dataEmprestimo","2026-09-21");
    }
    private Map<String,String> contexto(){
        return Map.of("tipoUsuario","ALUNO","dataReferencia","2026-10-01");
    }
    @BeforeEach void limpar() throws Exception {
        try(var c=dataSource.getConnection();var s=c.createStatement()){
            s.executeUpdate("DELETE FROM emprestimo");s.executeUpdate("DELETE FROM emprestimo_operacao");
        }
    }
    private long criar(String chave){
        return chamada().body(criarDados(chave)).post("/emprestimos").then().statusCode(200)
                .body("idUsuario",is(9),"idLivro",is(7),"multa",is(0.0f)).extract().jsonPath().getLong("id");
    }
    @Test void criaComInformacoesRecebidasSemConsultarLegado(){
        long id=criar(UUID.randomUUID().toString());
        chamada().get("/emprestimos/"+id).then().body("dataEmprestimo",is("2026-09-21"));
    }
    @Test void repeteCriacaoSemDuplicar(){
        String chave=UUID.randomUUID().toString();assertEquals(criar(chave),criar(chave));
        chamada().get("/emprestimos").then().body("size()",is(1));
    }
    @Test void rejeitaMudancaDeConteudo(){
        String chave=UUID.randomUUID().toString();criar(chave);
        Map<String,Object> dados=new HashMap<>(criarDados(chave));dados.put("tipoUsuario","PROFESSOR");
        chamada().body(dados).post("/emprestimos").then().statusCode(409);
    }
    @Test void validaContextoObrigatorio(){
        chamada().body("{}").post("/emprestimos").then().statusCode(400);
        Map<String,Object> dados=new HashMap<>(criarDados(UUID.randomUUID().toString()));dados.put("tipoUsuario","");
        chamada().body(dados).post("/emprestimos").then().statusCode(400);
        dados.remove("tipoUsuario");
        chamada().body(dados).post("/emprestimos").then().statusCode(400);
    }
    @Test void devolucaoRepetidaPreservaPrimeiroResultado(){
        long id=criar(UUID.randomUUID().toString());
        chamada().body(contexto()).post("/emprestimos/"+id+"/devolucao").then().statusCode(200).body("dataDevolucaoReal",is("2026-10-01"));
        chamada().body(Map.of("tipoUsuario","PROFESSOR","dataReferencia","2026-10-10"))
                .post("/emprestimos/"+id+"/devolucao").then().statusCode(200).body("dataDevolucaoReal",is("2026-10-01"));
    }
    @Test void devolucaoInexistente(){
        chamada().body(contexto()).post("/emprestimos/999999/devolucao").then().statusCode(404);
    }
    @Test void devolucaoExigeContexto(){
        long id=criar(UUID.randomUUID().toString());
        chamada().body("{}").post("/emprestimos/"+id+"/devolucao").then().statusCode(400);
    }
    @Test void serializaCriacoesConcorrentes(){
        String chave=UUID.randomUUID().toString();
        var chamadas=java.util.stream.IntStream.range(0,4).mapToObj(i->
                java.util.concurrent.CompletableFuture.supplyAsync(()->criar(chave))).toList();
        long id=chamadas.get(0).join();chamadas.forEach(c->assertEquals(id,c.join()));
        chamada().get("/emprestimos").then().body("size()",is(1));
    }
    @Test void retomaFalhaSqlComMesmoComando() throws Exception {
        String chave=UUID.randomUUID().toString();
        try(var c=dataSource.getConnection();var s=c.createStatement()){
            s.executeUpdate("ALTER TABLE emprestimo ADD CONSTRAINT falha_simulada CHECK(id<0) NOT VALID");
            try{chamada().body(criarDados(chave)).post("/emprestimos").then().statusCode(503);}
            finally{s.executeUpdate("ALTER TABLE emprestimo DROP CONSTRAINT falha_simulada");}
        }
        criar(chave);chamada().get("/emprestimos").then().body("size()",is(1));
    }
    @Test void atualizaMultaComTipoAtualRecebido(){
        long id=criar(UUID.randomUUID().toString());
        chamada().body(contexto()).post("/emprestimos/"+id+"/multa").then().statusCode(200).body("multa",greaterThan(0.0f));
        chamada().body(contexto()).post("/emprestimos/"+id+"/devolucao").then().statusCode(200);
        float multa=chamada().get("/emprestimos/"+id).jsonPath().getFloat("multa");
        chamada().body(Map.of("tipoUsuario","PROFESSOR","dataReferencia","2026-10-10"))
                .post("/emprestimos/"+id+"/multa").then().statusCode(200).body("multa",is(multa));
    }
    @Test void naoAtualizaMultaAntesDoVencimento(){
        long id=criar(UUID.randomUUID().toString());
        chamada().body(Map.of("tipoUsuario","ALUNO","dataReferencia","2026-09-21"))
                .post("/emprestimos/"+id+"/multa").then().statusCode(200).body("multa",is(0.0f));
    }
    @Test void preservaRegraPadraoParaOutrosTiposDoLegado(){
        String chave=UUID.randomUUID().toString();
        Map<String,Object> dados=new HashMap<>(criarDados(chave));dados.put("tipoUsuario","ADMIN");
        chamada().body(dados).post("/emprestimos").then().statusCode(200)
                .body("dataPrevistaDevolucao",is("2026-09-30"));
    }
}
