package tcc.moderno.emprestimo.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDate;
import org.jboss.logging.Logger;
import tcc.moderno.emprestimo.dtos.ContextoUsuarioDTO;
import tcc.moderno.emprestimo.repositories.EmprestimoRepository;

@ApplicationScoped
public class AtualizacaoMultasService {
    private static final Logger LOG = Logger.getLogger(AtualizacaoMultasService.class);
    private final EmprestimoRepository emprestimos;
    private final ContextoUsuarioService usuarios;
    private final ComandosEmprestimoService comandos;

    @Inject
    public AtualizacaoMultasService(EmprestimoRepository emprestimos,
            ContextoUsuarioService usuarios, ComandosEmprestimoService comandos) {
        this.emprestimos = emprestimos;
        this.usuarios = usuarios;
        this.comandos = comandos;
    }

    public void atualizar(LocalDate referencia) {
        int falhas = 0;
        for (var emprestimo : emprestimos.buscarAtrasados()) {
            try {
                String tipo = usuarios.consultarTipoAtual(emprestimo.getIdUsuario());
                comandos.atualizarMulta(emprestimo.getId(), new ContextoUsuarioDTO(tipo, referencia));
            } catch (RuntimeException e) {
                // Preserva a multa existente. Sem fallback de tipo ou resposta/segredo no log.
                LOG.warnf("Multa nao atualizada para emprestimo %d; nova tentativa no proximo ciclo (%s)",
                        emprestimo.getId(), e.getClass().getSimpleName());
                falhas++;
            }
        }
        if (falhas > 0) throw new IllegalStateException("Processamento de multas com " + falhas + " falha(s)");
    }
}
