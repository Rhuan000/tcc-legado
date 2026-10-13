\set ON_ERROR_STOP on
BEGIN;
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM livro WHERE id=910106 AND titulo='TCC JOB Livro 6' AND quantidade=50) THEN
        RAISE EXCEPTION 'Execute a massa comum em uma base limpa; livro 910106 deve ter estoque 50.';
    END IF;
END $$;

INSERT INTO fluxo_emprestimo(chave,id_livro,matricula,delta,dados,estado)
VALUES ('91000000-0000-4000-8000-000000000001',910106,'2091001',-1,
        json_build_object('chave','91000000-0000-4000-8000-000000000001',
                          'idLivro',910106,'idUsuario',910001,'tipoUsuario','ALUNO',
                          'dataEmprestimo',CURRENT_DATE::text)::text,'PENDENTE');
UPDATE livro SET quantidade=quantidade-1 WHERE id=910106;
COMMIT;

SELECT chave,estado,id_livro,id_emprestimo FROM fluxo_emprestimo
WHERE chave='91000000-0000-4000-8000-000000000001';
SELECT id,quantidade FROM livro WHERE id=910106;
