package tcc.moderno.emprestimo.services;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import tcc.moderno.emprestimo.dtos.CriarEmprestimoDTO;
import tcc.moderno.emprestimo.dtos.ContextoUsuarioDTO;
import tcc.moderno.emprestimo.models.Emprestimo;
import tcc.moderno.emprestimo.models.OperacaoEmprestimo;
import tcc.moderno.emprestimo.repositories.EmprestimoRepository;
import tcc.moderno.emprestimo.repositories.OperacaoEmprestimoRepository;

@ApplicationScoped
public class ComandosEmprestimoService {
    @Inject EmprestimoRepository emprestimos;
    @Inject OperacaoEmprestimoRepository operacoes;
    @Inject PrazoService prazos;
    @Inject MultaService multas;
    @Inject CalendarioService calendario;

    public Emprestimo criar(CriarEmprestimoDTO r) {
        String chave=UUID.fromString(r.getChave()).toString();
        OperacaoEmprestimo op=operacoes.buscar(chave).orElse(null);
        if(op==null) {
            op=new OperacaoEmprestimo();
            op.chave=chave; op.tipo="CRIAR"; op.idLivro=r.getIdLivro(); op.idUsuario=r.getIdUsuario();
            op.tipoUsuario=r.getTipoUsuario(); op.dataEmprestimo=r.getDataEmprestimo();
            op.dataPrevista=prazos.calcularDataPrevista(op.dataEmprestimo,op.tipoUsuario,
                    calendario.buscarFeriados(op.dataEmprestimo.getYear()));
            op.multa=BigDecimal.ZERO;
            op=operacoes.registrar(op);
        }
        if(!"CRIAR".equals(op.tipo) || op.idLivro!=r.getIdLivro() || op.idUsuario!=r.getIdUsuario()
                || !op.tipoUsuario.equals(r.getTipoUsuario()) || !op.dataEmprestimo.equals(r.getDataEmprestimo()))
            throw new WebApplicationException("Chave ja usada com outro conteudo",409);
        return concluir(chave);
    }

    public Emprestimo devolver(long id, ContextoUsuarioDTO contexto) {
        String chave="devolucao-"+id;
        if(operacoes.buscar(chave).isEmpty()) {
            Emprestimo emp=emprestimos.buscarPorId(id).orElseThrow(NotFoundException::new);
            if(emp.getDataDevolucaoReal()!=null) return emp;
            OperacaoEmprestimo op=new OperacaoEmprestimo();
            op.chave=chave; op.tipo="DEVOLVER"; op.idEmprestimo=id;
            op.idLivro=emp.getIdLivro(); op.idUsuario=emp.getIdUsuario();
            op.tipoUsuario=contexto.getTipoUsuario(); op.dataReferencia=contexto.getDataReferencia();
            op.multa=calcularMulta(emp,op.tipoUsuario,op.dataReferencia);
            operacoes.registrar(op);
        }
        return concluir(chave);
    }

    private Emprestimo concluir(String chave) {
        OperacaoEmprestimo op;
        try { op=operacoes.processar(chave); }
        catch(RuntimeException e) { throw new WebApplicationException("Repetir o mesmo comando",e,503); }
        return emprestimos.buscarPorId(op.idEmprestimo).orElseThrow();
    }

    public Emprestimo atualizarMulta(long id, ContextoUsuarioDTO contexto) {
        Emprestimo emp=emprestimos.buscarPorId(id).orElseThrow(NotFoundException::new);
        if(emp.getDataDevolucaoReal()==null && emp.getDataPrevistaDevolucao().isBefore(contexto.getDataReferencia()))
            emprestimos.atualizarMulta(id,calcularMulta(emp,contexto.getTipoUsuario(),contexto.getDataReferencia()));
        return emprestimos.buscarPorId(id).orElseThrow();
    }

    private BigDecimal calcularMulta(Emprestimo emp,String tipo,LocalDate referencia) {
        List<LocalDate> feriados=referencia.isAfter(emp.getDataPrevistaDevolucao())
                ? calendario.buscarFeriados(emp.getDataPrevistaDevolucao().getYear()):List.of();
        return BigDecimal.valueOf(multas.calcularMulta(emp.getDataPrevistaDevolucao(),tipo,referencia,feriados));
    }
}
