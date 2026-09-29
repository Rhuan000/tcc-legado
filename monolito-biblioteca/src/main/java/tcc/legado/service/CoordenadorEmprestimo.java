package tcc.legado.service;

import org.json.JSONObject;
import java.time.LocalDate;
import java.util.UUID;
import tcc.legado.acl.EmprestimoAcl;
import tcc.legado.dao.FluxoEmprestimoDAO;
import tcc.legado.dao.UsuarioDAO;
import tcc.legado.model.Emprestimo;
import tcc.legado.model.Usuario;

/** Coordena dominios locais e API; nao calcula prazo/multa nem persiste emprestimos. */
public class CoordenadorEmprestimo {
    private final EmprestimoAcl acl=new EmprestimoAcl();
    private final FluxoEmprestimoDAO fluxos=new FluxoEmprestimoDAO();
    private final UsuarioDAO usuarios=new UsuarioDAO();

    public Emprestimo criar(Long livro,String matricula,String chave) {
        chave=UUID.fromString(chave).toString();
        JSONObject fluxo=fluxos.buscar(chave);
        if(fluxo==null) {
            Usuario usuario=usuarios.buscarPorMatricula(matricula);
            validarUsuario(usuario);
            JSONObject dados=new JSONObject().put("chave",chave).put("idLivro",livro)
                    .put("idUsuario",usuario.getId()).put("tipoUsuario",usuario.getTipo())
                    .put("dataEmprestimo",LocalDate.now().toString());
            fluxos.registrar(chave,livro,matricula,-1,dados);
            fluxo=fluxos.buscar(chave);
        }
        if(fluxo.getInt("delta")!=-1 || fluxo.getLong("idLivro")!=livro || !matricula.equals(fluxo.getString("matricula")))
            throw new IllegalArgumentException("Chave ja usada com outro conteudo");
        return concluir(chave);
    }
    public Emprestimo devolver(Long id) {
        String chave="devolucao-"+id;
        if(fluxos.buscar(chave)==null) {
            Emprestimo emp=acl.buscarPorId(id);
            if(emp==null)throw new IllegalArgumentException("Emprestimo inexistente");
            if(emp.getDataDevolucaoReal()!=null)return emp;
            Usuario usuario=usuarios.buscarPorId(emp.getIdUsuario());
            validarUsuario(usuario);
            JSONObject dados=contexto(usuario,LocalDate.now()).put("idEmprestimo",id);
            fluxos.registrar(chave,emp.getIdLivro(),usuario.getMatricula(),1,dados);
        }
        return concluir(chave);
    }
    private Emprestimo concluir(String chave) {
        long id=fluxos.processar(chave,fluxo -> {
            JSONObject dados=fluxo.getJSONObject("dados");
            return fluxo.getInt("delta")==-1 ? acl.criarEmprestimo(dados)
                    : acl.registrarDevolucao(dados.getLong("idEmprestimo"),dados);
        });
        return acl.buscarPorId(id);
    }
    public void recuperarPendentes() {
        for(String chave:fluxos.pendentes()) {
            try{concluir(chave);}
            catch(RuntimeException e){java.util.logging.Logger.getLogger(getClass().getName()).warning("Fluxo pendente: "+chave);}
        }
    }
    public void atualizarMultas() {
        LocalDate referencia=LocalDate.now();
        for(Emprestimo emp:acl.buscarAtrasados()) {
            Usuario usuario=usuarios.buscarPorId(emp.getIdUsuario());
            validarUsuario(usuario);
            acl.atualizarMulta(emp.getId(),contexto(usuario,referencia));
        }
    }
    private JSONObject contexto(Usuario usuario,LocalDate referencia) {
        return new JSONObject().put("tipoUsuario",usuario.getTipo()).put("dataReferencia",referencia.toString());
    }
    private void validarUsuario(Usuario usuario) {
        if(usuario==null || usuario.getTipo()==null || usuario.getTipo().trim().isEmpty())
            throw new IllegalArgumentException("Usuario inexistente ou tipo invalido");
    }
}
