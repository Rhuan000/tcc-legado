package tcc.legado.dao;

import java.sql.*;
import java.util.*;
import java.util.function.Function;
import org.json.JSONObject;
import tcc.legado.model.Emprestimo;

/** Diario do coordenador e estoque locais; nunca acessa a tabela emprestimo. */
public class FluxoEmprestimoDAO {
    private Connection conectar() throws SQLException {
        return DriverManager.getConnection("jdbc:postgresql://localhost:5432/biblioteca","postgres","postgres");
    }
    public JSONObject buscar(String chave) {
        try(Connection c=conectar(); PreparedStatement s=c.prepareStatement("SELECT * FROM fluxo_emprestimo WHERE chave=?")) {
            s.setString(1,chave);
            try(ResultSet r=s.executeQuery()){return r.next()?mapear(r):null;}
        } catch(SQLException e){throw new IllegalStateException("Falha ao consultar fluxo",e);}
    }
    private JSONObject mapear(ResultSet r) throws SQLException {
        return new JSONObject().put("chave",r.getString("chave")).put("idLivro",r.getLong("id_livro"))
                .put("matricula",r.getString("matricula")).put("delta",r.getInt("delta"))
                .put("dados",new JSONObject(r.getString("dados"))).put("estado",r.getString("estado"))
                .put("idEmprestimo",r.getLong("id_emprestimo"));
    }
    public void registrar(String chave,long livro,String matricula,int delta,JSONObject dados) {
        try(Connection c=conectar()) {
            c.setAutoCommit(false);
            try {
                int novo;
                try(PreparedStatement s=c.prepareStatement("INSERT INTO fluxo_emprestimo(chave,id_livro,matricula,delta,dados) VALUES (?,?,?,?,?) ON CONFLICT(chave) DO NOTHING")) {
                    s.setString(1,chave);s.setLong(2,livro);s.setString(3,matricula);s.setInt(4,delta);s.setString(5,dados.toString());
                    novo=s.executeUpdate();
                }
                // Reserva e diario sao atomicos. Uma falha HTTP posterior nao perde a reserva.
                if(novo==1 && delta==-1) {
                    try(PreparedStatement s=c.prepareStatement("UPDATE livro SET quantidade=quantidade-1 WHERE id=? AND quantidade>0")) {
                        s.setLong(1,livro);
                        if(s.executeUpdate()!=1) throw new IllegalStateException("Livro inexistente ou indisponivel");
                    }
                }
                c.commit();
            } catch(SQLException|RuntimeException e){c.rollback();throw e;}
        } catch(SQLException e){throw new IllegalStateException("Falha ao registrar fluxo",e);}
    }
    public long processar(String chave,Function<JSONObject,Emprestimo> executar) {
        try(Connection c=conectar()) {
            c.setAutoCommit(false);
            try {
                JSONObject fluxo;
                try(PreparedStatement s=c.prepareStatement("SELECT * FROM fluxo_emprestimo WHERE chave=? FOR UPDATE")) {
                    s.setString(1,chave);
                    try(ResultSet r=s.executeQuery()){if(!r.next())throw new IllegalStateException("Fluxo inexistente");fluxo=mapear(r);}
                }
                long id=fluxo.getLong("idEmprestimo");
                if("PENDENTE".equals(fluxo.getString("estado"))) {
                    id=executar.apply(fluxo).getId();
                    if(fluxo.getInt("delta")==1) {
                        try(PreparedStatement s=c.prepareStatement("UPDATE livro SET quantidade=quantidade+1 WHERE id=?")) {
                            s.setLong(1,fluxo.getLong("idLivro"));
                            if(s.executeUpdate()!=1) throw new IllegalStateException("Livro da devolucao inexistente");
                        }
                    }
                    try(PreparedStatement s=c.prepareStatement("UPDATE fluxo_emprestimo SET estado='CONCLUIDA',id_emprestimo=? WHERE chave=?")) {
                        s.setLong(1,id);s.setString(2,chave);s.executeUpdate();
                    }
                }
                c.commit();return id;
            } catch(SQLException|RuntimeException e){c.rollback();throw e;}
        } catch(SQLException e){throw new IllegalStateException("Falha ao concluir fluxo",e);}
    }
    public List<String> pendentes() {
        try(Connection c=conectar();Statement s=c.createStatement();
                ResultSet r=s.executeQuery("SELECT chave FROM fluxo_emprestimo WHERE estado='PENDENTE' ORDER BY criado_em,chave")) {
            List<String> chaves=new ArrayList<>();while(r.next())chaves.add(r.getString(1));return chaves;
        } catch(SQLException e){throw new IllegalStateException("Falha ao consultar pendencias",e);}
    }
}
