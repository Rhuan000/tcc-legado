-- Estrutura de leitura derivada de init-db.sql; exclusiva do banco de testes.
-- O Dev Services inicia um PostgreSQL vazio e isolado. Não executar no banco do legado.
CREATE TABLE emprestimo (
    id SERIAL PRIMARY KEY,
    id_livro INT NOT NULL,
    id_usuario INT NOT NULL,
    data_emprestimo DATE NOT NULL DEFAULT CURRENT_DATE,
    data_prevista_devolucao DATE NOT NULL,
    data_devolucao_real DATE,
    multa DECIMAL(10,2) DEFAULT 0.00
);
