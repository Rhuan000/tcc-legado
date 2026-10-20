package tcc.legado;

import org.junit.*;
import tcc.legado.dao.FluxoEmprestimoDAO;
import tcc.legado.exception.ValidacaoException;
import org.json.JSONObject;
import java.sql.*;
import java.util.UUID;
import static org.junit.Assert.*;

/** Teste PostgreSQL: reserva e diario usam ON CONFLICT, como em producao. */
public class FluxoHistoricoTest {
    private final String schema="fluxo_"+UUID.randomUUID().toString().replace("-","");
    private String url;
    private FluxoEmprestimoDAO dao;
    private Connection base() throws SQLException {
        return DriverManager.getConnection(url,System.getProperty("test.jdbc.user","postgres"),System.getProperty("test.jdbc.password","postgres"));
    }
    private Connection connection() throws SQLException { Connection c=base(); c.setSchema(schema); return c; }
    private void sql(String sql) throws SQLException { try(Connection c=connection()) { c.createStatement().execute(sql); } }
    @Before public void setup() throws Exception {
        url=System.getProperty("test.jdbc.url","");
        Assume.assumeTrue(url.startsWith("jdbc:postgresql:"));
        try(Connection c=base()) { c.createStatement().execute("CREATE SCHEMA "+schema); }
        sql("CREATE TABLE usuario(matricula VARCHAR(100) PRIMARY KEY)");
        sql("CREATE TABLE livro(id BIGINT PRIMARY KEY,quantidade INT)");
        sql("CREATE TABLE fluxo_emprestimo(chave VARCHAR(100) PRIMARY KEY,id_livro BIGINT REFERENCES livro(id) ON DELETE RESTRICT,matricula VARCHAR(100) REFERENCES usuario(matricula) ON DELETE RESTRICT,delta INT,dados TEXT,estado VARCHAR(12) DEFAULT 'PENDENTE',id_emprestimo BIGINT,criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
        sql("INSERT INTO usuario VALUES('2099003')"); sql("INSERT INTO livro VALUES(32,1)");
        dao=new FluxoEmprestimoDAO() { @Override protected Connection conectar() throws SQLException { return connection(); } };
    }
    @After public void cleanup() throws Exception {
        if(url!=null && url.startsWith("jdbc:postgresql:")) {
            try(Connection c=base()) { c.createStatement().execute("DROP SCHEMA IF EXISTS "+schema+" CASCADE"); }
        }
    }
    @Test public void reservaPendenteProtegeCadastrosAntesDaChamadaHttp() throws Exception {
        dao.registrar("chave",32,"2099003",-1,new JSONObject());
        assertThrows(SQLException.class,()->sql("DELETE FROM usuario"));
        assertThrows(SQLException.class,()->sql("DELETE FROM livro"));
        dao.registrar("chave",32,"2099003",-1,new JSONObject());
        try(Connection c=connection();ResultSet r=c.createStatement().executeQuery("SELECT quantidade FROM livro")) {
            r.next(); assertEquals(0,r.getInt(1));
        }
        assertEquals(1,dao.pendentes().size());
    }
    @Test public void usuarioExcluidoAntesDaReservaNaoPermiteEmprestimoNemBaixaEstoque() throws Exception {
        sql("DELETE FROM usuario");
        assertThrows(IllegalStateException.class,()->dao.registrar("chave",32,"2099003",-1,new JSONObject()));
        try(Connection c=connection();ResultSet r=c.createStatement().executeQuery("SELECT quantidade FROM livro")) {
            r.next(); assertEquals(1,r.getInt(1));
        }
        assertTrue(dao.pendentes().isEmpty());
    }
    @Test public void semEstoqueRetornaValidacaoENaoDeixaFluxoPendente() throws Exception {
        sql("UPDATE livro SET quantidade=0");
        ValidacaoException e=assertThrows(ValidacaoException.class,()->dao.registrar("chave",32,"2099003",-1,new JSONObject()));
        assertEquals("Livro indisponível",e.getMessage()); assertTrue(dao.pendentes().isEmpty());
    }
}
