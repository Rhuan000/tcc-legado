\set ON_ERROR_STOP on
SELECT CURRENT_TIMESTAMP AS instante;
SELECT chave,estado,id_livro,id_emprestimo FROM fluxo_emprestimo
WHERE chave='91000000-0000-4000-8000-000000000001';
SELECT chave,tipo,estado,id_emprestimo FROM emprestimo_operacao
WHERE chave='91000000-0000-4000-8000-000000000001';
SELECT id,id_livro,id_usuario,data_emprestimo,data_prevista_devolucao FROM emprestimo
WHERE id=(SELECT id_emprestimo FROM fluxo_emprestimo
          WHERE chave='91000000-0000-4000-8000-000000000001');
SELECT id,quantidade FROM livro WHERE id=910106;
