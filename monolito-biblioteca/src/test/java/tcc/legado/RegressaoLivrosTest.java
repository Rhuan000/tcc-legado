package tcc.legado;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import tcc.legado.action.LivroAction;
import tcc.legado.action.LivroDestaqueAction;
import tcc.legado.dao.LivroDestaqueDAO;
import tcc.legado.model.LivroDestaque;
import tcc.legado.dao.EmprestimoDAO;
import tcc.legado.dao.LivroDAO;
import tcc.legado.ejb.livro.ILivroEJB;
import tcc.legado.ejb.livro.LivroEJB;
import tcc.legado.exception.ValidacaoException;
import tcc.legado.model.Livro;
import tcc.legado.service.LivroService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;

/** H2 por padrão; -Dtest.jdbc.url permite repetir em PostgreSQL com schema isolado. */
public class RegressaoLivrosTest {
    private final String schema = "regressao_" + UUID.randomUUID().toString().replace("-", "");
    private final String url = System.getProperty("test.jdbc.url", "jdbc:h2:mem:livros;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");
    private LivroDAO dao;
    private LivroService service;
    private LivroAction action;
    private ActionMapping mapping;
    private final Map<String, String> params = new HashMap<>();
    private final Map<String, Object> attrs = new HashMap<>();

    private Connection baseConnection() throws SQLException {
        return DriverManager.getConnection(url, System.getProperty("test.jdbc.user", "postgres"),
                System.getProperty("test.jdbc.password", "postgres"));
    }

    private Connection connection() throws SQLException {
        Connection connection = baseConnection();
        connection.setSchema(schema);
        return connection;
    }

    public static void inject(Object target, String field, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }

    @Before
    public void setup() throws Exception {
        try (Connection c = baseConnection()) {
            c.createStatement().execute("CREATE SCHEMA " + schema);
        }
        try (Connection c = connection()) {
            c.createStatement().execute("CREATE TABLE livro (id SERIAL PRIMARY KEY, titulo VARCHAR(200) NOT NULL, "
                    + "autor VARCHAR(200), isbn VARCHAR(20) UNIQUE, ano INT, editora VARCHAR(100), quantidade INT CHECK (quantidade >= 0))");
            c.createStatement().execute("CREATE TABLE emprestimo (id SERIAL PRIMARY KEY, id_livro INT REFERENCES livro(id) ON DELETE CASCADE, data_devolucao_real DATE)");
            c.createStatement().execute("CREATE TABLE livro_destaque (id SERIAL PRIMARY KEY, id_livro INT REFERENCES livro(id) ON DELETE CASCADE, "
                    + "titulo VARCHAR(200), descricao TEXT, desconto DECIMAL(5,2), categoria VARCHAR(50), data_inicio DATE, data_fim DATE, ativo BOOLEAN, visualizacoes INT)");
        }
        dao = new LivroDAO() {
            @Override protected Connection getConnection() throws SQLException { return connection(); }
        };
        service = new LivroService();
        inject(service, "livroDAO", dao);
        inject(service, "emprestimoDAO", new EmprestimoDAO() {
            @Override protected Connection getConnection() throws SQLException { return connection(); }
        });
        LivroEJB ejb = new LivroEJB();
        inject(ejb, "livroService", service);
        action = new LivroAction() {
            @Override protected ILivroEJB getLivroEJB() { return ejb; }
        };
        mapping = new ActionMapping();
        mapping.addForwardConfig(new ActionForward("novo", "/cadastro.jsp", false));
        mapping.addForwardConfig(new ActionForward("erro", "/error.jsp", false));
        mapping.addForwardConfig(new ActionForward("listarSucesso", "/lista.jsp", false));
        params.put("titulo", "Ensaio ação, validação, João e Promoção");
        params.put("autor", "João");
        params.put("isbn", "9780000000147");
        params.put("ano", "2026");
        params.put("quantidade", "0");
    }

    @After
    public void cleanup() throws Exception {
        // Somente o schema aleatório criado por este teste é removido.
        try (Connection c = baseConnection()) {
            c.createStatement().execute("DROP SCHEMA IF EXISTS " + schema + " CASCADE");
        }
    }

    private HttpServletRequest request() {
        HttpSession session = (HttpSession) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{HttpSession.class}, (p, method, args) -> "getAttribute".equals(method.getName()) ? "ADMIN" : null);
        return (HttpServletRequest) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{HttpServletRequest.class},
                (p, method, args) -> {
                    switch (method.getName()) {
                        case "getParameter": return params.get(args[0]);
                        case "getAttribute": return attrs.get(args[0]);
                        case "setAttribute": attrs.put((String) args[0], args[1]); return null;
                        case "getSession": return session;
                        default: return null;
                    }
                });
    }

    private String salvar() throws Exception {
        attrs.clear();
        return action.salvar(mapping, null, request(), null).getPath();
    }

    private Livro livro(String isbn) {
        Livro livro = new Livro();
        livro.setTitulo("Ensaio");
        livro.setIsbn(isbn);
        livro.setQuantidade(0);
        return livro;
    }

    @Test public void cadastroComAnoVazioPersisteNullETextoOriginal() throws Exception {
        params.put("ano", "");
        assertEquals("/lista.jsp", salvar());
        Livro saved = dao.listarTodos().get(0);
        assertNull(saved.getAno());
        assertNull(dao.buscarPorId(saved.getId()).getAno());
        assertEquals(params.get("titulo"), saved.getTitulo());
        assertEquals("João", saved.getAutor());
        assertNotNull(attrs.get("mensagem"));
    }

    @Test public void cadastroComAnoPreenchido() throws Exception {
        salvar();
        assertEquals(Integer.valueOf(2026), dao.listarTodos().get(0).getAno());
    }

    @Test public void isbnDuplicadoComAnoVazioRetornaFormularioSemSucesso() throws Exception {
        salvar();
        params.put("titulo", "Ensaio ISBN Duplicado");
        params.put("ano", "");
        assertEquals("/cadastro.jsp", salvar());
        assertEquals("Já existe um livro com este ISBN", attrs.get("erro"));
        assertNull(attrs.get("mensagem"));
        assertEquals(params.get("titulo"), ((Livro) attrs.get("livro")).getTitulo());
        assertEquals(1, dao.listarTodos().size());
        assertNotEquals(params.get("titulo"), dao.listarTodos().get(0).getTitulo());
    }

    @Test public void isbnDuplicadoComAnoPreenchido() throws Exception {
        salvar();
        assertEquals("/cadastro.jsp", salvar());
        assertEquals(1, dao.listarTodos().size());
        assertNull(attrs.get("mensagem"));
    }

    @Test public void editarParaIsbnDuplicadoMantemBancoEFormulario() throws Exception {
        salvar();
        Livro outro = livro("123");
        service.salvar(outro);
        params.put("id", outro.getId().toString());
        assertEquals("/cadastro.jsp", salvar());
        assertEquals("123", dao.buscarPorId(outro.getId()).getIsbn());
        assertEquals(outro.getId(), ((Livro) attrs.get("livro")).getId());
        assertEquals(2, dao.listarTodos().size());
        assertNull(attrs.get("mensagem"));
    }

    @Test public void editarMesmoIsbnELimparAno() throws Exception {
        salvar();
        Livro saved = dao.listarTodos().get(0);
        params.put("id", saved.getId().toString());
        params.put("ano", "");
        assertEquals("/lista.jsp", salvar());
        assertNull(dao.buscarPorId(saved.getId()).getAno());
        assertEquals(1, dao.listarTodos().size());
    }

    @Test public void editarLivroSemIsbnNaoCausaNullPointer() {
        Livro saved = livro(null);
        service.salvar(saved);
        saved.setIsbn("123");
        service.atualizar(saved);
        assertEquals("123", dao.buscarPorId(saved.getId()).getIsbn());
    }

    @Test public void isbnOpcionalNaoConflita() throws Exception {
        params.put("isbn", "");
        assertEquals("/lista.jsp", salvar());
        assertEquals("/lista.jsp", salvar());
        assertEquals(2, dao.listarTodos().size());
    }

    @Test public void quantidadeNegativaRetornaDadosSemGravar() throws Exception {
        params.put("quantidade", "-1");
        assertEquals("/cadastro.jsp", salvar());
        assertTrue(attrs.get("erro").toString().contains("Quantidade inválida"));
        assertEquals(Integer.valueOf(-1), ((Livro) attrs.get("livro")).getQuantidade());
        assertTrue(dao.listarTodos().isEmpty());
        assertNull(attrs.get("mensagem"));
    }

    @Test public void edicaoQuantidadeNegativaNaoAlteraBanco() throws Exception {
        salvar();
        Livro saved = dao.listarTodos().get(0);
        params.put("id", saved.getId().toString());
        params.put("quantidade", "-1");
        assertEquals("/cadastro.jsp", salvar());
        assertEquals(Integer.valueOf(0), dao.buscarPorId(saved.getId()).getQuantidade());
    }

    @Test public void tituloVazioValidadoNoServidor() throws Exception {
        params.put("titulo", " ");
        assertEquals("/cadastro.jsp", salvar());
        assertEquals("Título é obrigatório", attrs.get("erro"));
        assertTrue(dao.listarTodos().isEmpty());
    }

    @Test public void numeroInvalidoPreservaParametros() throws Exception {
        params.put("ano", "abc");
        assertEquals("/cadastro.jsp", salvar());
        assertEquals(Boolean.TRUE, attrs.get("reapresentar"));
        assertEquals("abc", request().getParameter("ano"));
        assertTrue(dao.listarTodos().isEmpty());
    }

    @Test public void excluiSemEstoqueESemEmprestimos() {
        Livro saved = livro(null);
        service.salvar(saved);
        service.excluir(saved.getId());
        assertNull(dao.buscarPorId(saved.getId()));
    }

    @Test public void bloqueiaExclusaoComEstoque() {
        Livro saved = livro(null);
        saved.setQuantidade(1);
        service.salvar(saved);
        assertThrows(ValidacaoException.class, () -> service.excluir(saved.getId()));
        assertNotNull(dao.buscarPorId(saved.getId()));
    }

    @Test public void bloqueiaExclusaoComEmprestimoAtivo() throws Exception {
        Livro saved = livro(null);
        service.salvar(saved);
        try (Connection c = connection()) {
            c.createStatement().execute("INSERT INTO emprestimo(id_livro) VALUES (" + saved.getId() + ")");
        }
        params.put("id", saved.getId().toString());
        assertEquals("/error.jsp", action.excluir(mapping, null, request(), null).getPath());
        assertTrue(attrs.get("erro").toString().contains("empréstimo ativo"));
        assertNotNull(dao.buscarPorId(saved.getId()));
        assertNull(attrs.get("mensagem"));
    }

    @Test public void emprestimoDevolvidoNaoBloqueiaExclusao() throws Exception {
        Livro saved = livro(null);
        service.salvar(saved);
        try (Connection c = connection()) {
            c.createStatement().execute("INSERT INTO emprestimo(id_livro, data_devolucao_real) VALUES (" + saved.getId() + ", DATE '2026-10-14')");
        }
        service.excluir(saved.getId());
        assertNull(dao.buscarPorId(saved.getId()));
    }

    @Test public void falhaAoConsultarEmprestimosImpedeExclusao() throws Exception {
        Livro saved = livro(null);
        service.salvar(saved);
        try (Connection c = connection()) { c.createStatement().execute("DROP TABLE emprestimo"); }
        assertThrows(IllegalStateException.class, () -> service.excluir(saved.getId()));
        assertNotNull(dao.buscarPorId(saved.getId()));
    }

    @Test public void falhaDeBancoNaoInformaSucessoNemExibeDetalhesTecnicos() throws Exception {
        try (Connection c = connection()) { c.createStatement().execute("DROP TABLE livro CASCADE"); }
        assertEquals("/cadastro.jsp", salvar());
        assertEquals("Não foi possível gravar o livro. Tente novamente.", attrs.get("erro"));
        assertNull(attrs.get("mensagem"));
    }

    @Test public void destaquePreservaAcentosNoCadastroEdicaoListaEDetalhe() throws Exception {
        Livro saved = livro(null);
        service.salvar(saved);
        LivroDestaqueDAO destaques = new LivroDestaqueDAO() {
            @Override protected Connection getConnection() throws SQLException { return connection(); }
        };
        LivroDestaqueAction destaqueAction = new LivroDestaqueAction();
        inject(destaqueAction, "dao", destaques);
        params.put("idLivro", saved.getId().toString());
        params.put("titulo", "Promoção João");
        params.put("descricao", "Destaque criado para ação e validação funcional comparativa.");
        params.put("categoria", "Promoção");
        params.put("desconto", "10");
        params.put("dataInicio", "2026-10-14");
        params.put("dataFim", "2026-10-31");
        params.put("ativo", "on");
        assertEquals("/lista.jsp", destaqueAction.salvar(mapping, null, request(), null).getPath());
        LivroDestaque destaque = destaques.listarTodos().get(0);
        assertEquals(params.get("titulo"), destaque.getTitulo());
        assertEquals(params.get("descricao"), destaque.getDescricao());
        assertEquals("Promoção", destaque.getCategoria());
        assertEquals(params.get("descricao"), destaques.buscarPorId(destaque.getId()).getDescricao());
        params.put("id", destaque.getId().toString());
        params.put("descricao", "ação, validação, João e Promoção");
        destaqueAction.salvar(mapping, null, request(), null);
        assertEquals(params.get("descricao"), destaques.buscarPorId(destaque.getId()).getDescricao());
        assertEquals(1, destaques.listarTodos().size());
    }
}
