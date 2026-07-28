package tcc.legado.ejb.livro;

import tcc.legado.model.Livro;
import java.util.List;

public interface ILivroEJB {
    List<Livro> listarTodos();
    Livro buscarPorId(Long id);
    void salvar(Livro livro);
    void atualizar(Livro livro);
    void excluir(Long id);
}
