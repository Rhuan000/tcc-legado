package tcc.legado;

import org.junit.*;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.listeners.JobListenerSupport;
import tcc.legado.dao.*;
import tcc.legado.ejb.emprestimo.*;
import tcc.legado.job.MultaDiariaJob;
import tcc.legado.model.Usuario;
import tcc.legado.service.EmprestimoService;
import tcc.legado.util.CacheGlobal;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.Assert.*;

/** Quartz real, DAO/service/EJB reais, schema isolado e calendario controlado. */
public class MultaDiariaJobTest {
    private static IEmprestimoEJB ejb;
    public static class JobControlado extends MultaDiariaJob {
        @Override protected IEmprestimoEJB getEmprestimoEJB() { return ejb; }
    }
    private final String schema = "multas_" + UUID.randomUUID().toString().replace("-", "");
    private final String url = System.getProperty("test.jdbc.url", "jdbc:h2:mem:multas;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");
    private Object feriadosAnteriores;
    private int anoAnterior;
    private Connection base() throws SQLException {
        return DriverManager.getConnection(url, System.getProperty("test.jdbc.user", "postgres"), System.getProperty("test.jdbc.password", "postgres"));
    }
    private Connection connection() throws SQLException { Connection c=base(); c.setSchema(schema); return c; }
    @Before public void setup() throws Exception {
        feriadosAnteriores = CacheGlobal.get("feriados"); anoAnterior = CacheGlobal.getAnoCorrente();
        LocalDate hoje=LocalDate.now();
        CacheGlobal.setAnoCorrente(hoje.minusDays(14).getYear());
        CacheGlobal.setFeriados(Collections.singletonList(hoje.minusDays(60)));
        try(Connection c=base()) { c.createStatement().execute("CREATE SCHEMA " + schema); }
        try(Connection c=connection(); Statement s=c.createStatement()) {
            s.execute("CREATE TABLE emprestimo(id INT PRIMARY KEY,id_livro INT,id_usuario INT,data_emprestimo DATE,data_prevista_devolucao DATE,data_devolucao_real DATE,multa DECIMAL(10,2))");
            for(int id=1;id<=6;id++) {
                LocalDate prevista=id==5 ? hoje.plusDays(3) : hoje.minusDays(14);
                s.execute("INSERT INTO emprestimo VALUES("+id+",32,"+id+",DATE '"+hoje.minusDays(30)+"',DATE '"+prevista+"',"+(id==6 ? "CURRENT_DATE" : "NULL")+",7.00)");
            }
        }
        EmprestimoService service=new EmprestimoService();
        RegressaoLivrosTest.inject(service,"emprestimoDAO",new EmprestimoDAO() {
            @Override protected Connection getConnection() throws SQLException { return connection(); }
        });
        RegressaoLivrosTest.inject(service,"usuarioDAO",new UsuarioDAO() {
            @Override public Usuario buscarPorId(Long id) {
                Usuario u=new Usuario(); u.setTipo(id==2 ? "PROFESSOR" : id==3 ? "BOLSISTA" : id==4 ? "BIBLIOTECARIO" : "ALUNO"); return u;
            }
        });
        EmprestimoEJB real=new EmprestimoEJB(); RegressaoLivrosTest.inject(real,"emprestimoService",service); ejb=real;
    }
    @After public void cleanup() throws Exception {
        CacheGlobal.put("feriados",feriadosAnteriores); CacheGlobal.setAnoCorrente(anoAnterior);
        try(Connection c=base()) { c.createStatement().execute("DROP SCHEMA IF EXISTS "+schema+" CASCADE"); }
        ejb=null;
    }
    private List<Double> valores() throws SQLException {
        List<Double> values=new ArrayList<>();
        try(Connection c=connection(); ResultSet r=c.createStatement().executeQuery("SELECT multa FROM emprestimo ORDER BY id")) {
            while(r.next()) values.add(r.getDouble(1));
        }
        return values;
    }
    @Test public void disparoQuartzAtualizaValoresEPreservaNaoVencidosEDevolvidos() throws Exception {
        assertEquals(Arrays.asList(7d,7d,7d,7d,7d,7d),valores());
        Properties props=new Properties();
        props.setProperty("org.quartz.scheduler.instanceName",schema);
        props.setProperty("org.quartz.threadPool.threadCount","1");
        props.setProperty("org.quartz.jobStore.class","org.quartz.simpl.RAMJobStore");
        Scheduler scheduler=new StdSchedulerFactory(props).getScheduler();
        CountDownLatch terminou=new CountDownLatch(1);
        AtomicReference<JobExecutionException> erro=new AtomicReference<>();
        scheduler.getListenerManager().addJobListener(new JobListenerSupport() {
            @Override public String getName() { return "evidencia"; }
            @Override public void jobWasExecuted(JobExecutionContext ctx,JobExecutionException e) { erro.set(e); terminou.countDown(); }
        });
        try {
            scheduler.scheduleJob(JobBuilder.newJob(JobControlado.class).withIdentity("MultaDiariaJob").build(),
                    TriggerBuilder.newTrigger().startNow().build());
            scheduler.start();
            assertTrue("Quartz deve concluir em 15 segundos",terminou.await(15,TimeUnit.SECONDS));
            assertNull(erro.get());
            assertEquals(Arrays.asList(20d,5d,10d,20d,7d,7d),valores());
            ejb.atualizarMultasAtrasadas();
            assertEquals(Arrays.asList(20d,5d,10d,20d,7d,7d),valores());
            System.out.println("PEN-01: Quartz executado; antes=[7,7,7,7,7,7]; depois="+valores()+"; repeticao idempotente");
        } finally { scheduler.shutdown(true); }
    }
    @Test public void falhaNaConsultaNaoConcluiJobComFalsoSucesso() throws Exception {
        try(Connection c=connection()) { c.createStatement().execute("DROP TABLE emprestimo"); }
        assertThrows(JobExecutionException.class,()->new JobControlado().execute(null));
    }
}
