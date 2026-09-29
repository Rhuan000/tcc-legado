package tcc.moderno.emprestimo.dtos;

public final class ContagemEmprestimosDTO {
    private final long idLivro;
    private final long totalEmprestimos;

    public ContagemEmprestimosDTO(long idLivro, long totalEmprestimos) {
        this.idLivro = idLivro;
        this.totalEmprestimos = totalEmprestimos;
    }

    public long getIdLivro() {
        return idLivro;
    }

    public long getTotalEmprestimos() {
        return totalEmprestimos;
    }
}
