package tcc.legado;

import org.junit.*;
import org.apache.struts.action.*;
import tcc.legado.action.UsuarioAction;
import tcc.legado.dao.*;
import tcc.legado.acl.EmprestimoAcl;
import tcc.legado.model.Emprestimo;
import tcc.legado.ejb.usuario.*;
import tcc.legado.exception.ValidacaoException;
import tcc.legado.service.UsuarioService;
import javax.servlet.http.*;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.*;
import static org.junit.Assert.*;

public class RegressaoUsuariosTest {
    private final String schema = "usuarios_" + UUID.randomUUID().toString().replace("-", "");
    private final String url = System.getProperty("test.jdbc.url", "jdbc:h2:mem:usuarios;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");
    private UsuarioDAO usuarios;
    private UsuarioService service;
    private UsuarioAction action;
    private ActionMapping mapping;
    private final Map<String, Object> attrs = new HashMap<>();

    private Connection base() throws SQLException {
        return DriverManager.getConnection(url, System.getProperty("test.jdbc.user", "postgres"), System.getProperty("test.jdbc.password", "postgres"));
    }
    private Connection connection() throws SQLException {
        Connection c = base(); c.setSchema(schema); return c;
    }
    private void sql(String sql) throws SQLException {
        try (Connection c = connection(); Statement s = c.createStatement()) { s.execute(sql); }
    }
    private int count(String table) throws SQLException {
        try (Connection c = connection(); ResultSet r = c.createStatement().executeQuery("SELECT COUNT(*) FROM " + table)) {
            r.next(); return r.getInt(1);
        }
    }
    @Before public void setup() throws Exception {
        try (Connection c = base()) { c.createStatement().execute("CREATE SCHEMA " + schema); }
        sql("CREATE TABLE usuario(id INT PRIMARY KEY, nome VARCHAR(100), matricula VARCHAR(20) UNIQUE, email VARCHAR(100), tipo VARCHAR(20))");
        sql("CREATE TABLE usuario_auth(matricula VARCHAR(20) REFERENCES usuario(matricula) ON DELETE CASCADE)");
        sql("CREATE TABLE livro(id INT PRIMARY KEY, quantidade INT)");
        sql("CREATE TABLE emprestimo(id INT PRIMARY KEY, id_usuario INT, id_livro INT, data_devolucao_real DATE)");
        sql("CREATE TABLE fluxo_emprestimo(matricula VARCHAR(20) REFERENCES usuario(matricula) ON DELETE RESTRICT, id_livro INT REFERENCES livro(id) ON DELETE RESTRICT)");
        sql("INSERT INTO usuario VALUES(37,'Leitor','2099003','teste@example.test','ALUNO')");
        sql("INSERT INTO usuario_auth VALUES('2099003')");
        sql("INSERT INTO livro VALUES(32,0)");
        usuarios = new UsuarioDAO() { @Override protected Connection getConnection() throws SQLException { return connection(); } };
        service = new UsuarioService();
        RegressaoLivrosTest.inject(service, "usuarioDAO", usuarios);
        RegressaoLivrosTest.inject(service, "emprestimoAcl", new EmprestimoAcl() {
            @Override public List<Emprestimo> listarTodos() {
                // Simula somente a fronteira HTTP; producao nao consulta tabela remota.
                try (Connection c=connection(); ResultSet r=c.createStatement().executeQuery("SELECT * FROM emprestimo")) {
                    List<Emprestimo> result=new ArrayList<>();
                    while(r.next()) { Emprestimo e=new Emprestimo(); e.setIdUsuario(r.getLong("id_usuario")); result.add(e); }
                    return result;
                } catch(SQLException e) { throw new IllegalStateException("API indisponivel",e); }
            }
        });
        UsuarioEJB ejb = new UsuarioEJB();
        RegressaoLivrosTest.inject(ejb, "usuarioService", service);
        action = new UsuarioAction() { @Override protected IUsuarioEJB getUsuarioEJB() { return ejb; } };
        mapping = new ActionMapping();
        mapping.addForwardConfig(new ActionForward("erro", "/erro.jsp", false));
        mapping.addForwardConfig(new ActionForward("listarSucesso", "/lista.jsp", false));
    }
    @After public void cleanup() throws Exception {
        try (Connection c = base()) { c.createStatement().execute("DROP SCHEMA IF EXISTS " + schema + " CASCADE"); }
    }
    private HttpServletRequest request() {
        HttpSession session = (HttpSession) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{HttpSession.class},
                (p,m,a) -> "getAttribute".equals(m.getName()) ? "ADMIN" : null);
        return (HttpServletRequest) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{HttpServletRequest.class}, (p,m,a) -> {
            switch (m.getName()) {
                case "getParameter": return "37";
                case "getSession": return session;
                case "setAttribute": attrs.put((String) a[0], a[1]); return null;
                default: return null;
            }
        });
    }
    @Test public void lim01BloqueiaUsuarioEPreservaEmprestimoEstoqueECredencial() throws Exception {
        sql("INSERT INTO emprestimo VALUES(82,37,32,NULL)");
        assertEquals("/erro.jsp", action.excluir(mapping, null, request(), null).getPath());
        assertTrue(attrs.get("erro").toString().contains("histórico"));
        assertNull(attrs.get("mensagem"));
        assertEquals(1, count("usuario")); assertEquals(1, count("emprestimo")); assertEquals(1, count("usuario_auth"));
        try (Connection c = connection(); ResultSet r = c.createStatement().executeQuery("SELECT quantidade FROM livro WHERE id=32")) {
            r.next(); assertEquals(0, r.getInt(1));
        }
    }
    @Test public void preservaHistoricoDevolvido() throws Exception {
        sql("INSERT INTO emprestimo VALUES(82,37,32,CURRENT_DATE)");
        assertThrows(ValidacaoException.class, () -> service.excluir(37L));
        assertEquals(1, count("emprestimo")); assertEquals(1, count("usuario"));
    }
    @Test public void fluxoPendenteImpedeExclusaoDiretaDeUsuarioELivro() throws Exception {
        sql("INSERT INTO emprestimo VALUES(82,37,32,NULL)");
        sql("INSERT INTO fluxo_emprestimo VALUES('2099003',32)");
        assertThrows(ValidacaoException.class, () -> usuarios.excluir(37L));
        assertThrows(SQLException.class, () -> sql("DELETE FROM livro WHERE id=32"));
        assertEquals(1, count("emprestimo"));
    }
    @Test public void usuarioSemHistoricoPodeSerExcluido() throws Exception {
        assertEquals("/lista.jsp", action.excluir(mapping, null, request(), null).getPath());
        assertEquals(0, count("usuario")); assertEquals(0, count("usuario_auth"));
        assertNotNull(attrs.get("mensagem"));
    }
    @Test public void consultaIndisponivelBloqueiaExclusaoSemDetalheTecnico() throws Exception {
        sql("DROP TABLE emprestimo");
        assertEquals("/erro.jsp", action.excluir(mapping, null, request(), null).getPath());
        assertEquals("Não foi possível excluir o usuário. Tente novamente.", attrs.get("erro"));
        assertNull(attrs.get("mensagem")); assertEquals(1, count("usuario"));
    }
    @Test public void falhaDePersistenciaNaoEhIgnorada() throws Exception {
        UsuarioDAO indisponivel = new UsuarioDAO() {
            @Override protected Connection getConnection() throws SQLException { throw new SQLException("detalhe privado"); }
        };
        assertThrows(IllegalStateException.class, () -> indisponivel.excluir(37L));
    }
}
