package tcc.legado.ejb.destaque;

import tcc.legado.model.LivroDestaque;
import java.util.List;

public interface ILivroDestaqueEJB {
    List<LivroDestaque> listarTodos();
    List<LivroDestaque> listarAtivos();
    LivroDestaque buscarPorId(Long id);
    void salvar(LivroDestaque livroDestaque);
    void atualizar(LivroDestaque livroDestaque);
    void excluir(Long id);
    void visualizar(Long id);
}
