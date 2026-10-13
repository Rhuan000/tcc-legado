\set ON_ERROR_STOP on
\if :{?data_referencia}
\else
SELECT CURRENT_DATE AS data_referencia \gset
\endif
BEGIN;
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM usuario WHERE id BETWEEN 910001 AND 910003)
       OR EXISTS (SELECT 1 FROM livro WHERE id BETWEEN 910101 AND 910106)
       OR EXISTS (SELECT 1 FROM emprestimo WHERE id BETWEEN 911001 AND 912999) THEN
        RAISE EXCEPTION 'Faixa reservada ocupada. Restaure a base de ensaio antes de reaplicar a massa.';
    END IF;
END $$;

INSERT INTO usuario(id,nome,matricula,email,tipo) VALUES
(910001,'TCC JOB ALUNO','2091001','aluno@example.invalid','ALUNO'),
(910002,'TCC JOB PROFESSOR','2091002','professor@example.invalid','PROFESSOR'),
(910003,'TCC JOB BOLSISTA','2091003','bolsista@example.invalid','BOLSISTA');

INSERT INTO usuario_auth(matricula,senha_hash,perfil)
SELECT matricula, '0f74ff3c49990ac2093828e43bebbafd8e7ebd5f0483ef340ab9cc779f2e709a', 'CONSULTA'
FROM usuario WHERE id BETWEEN 910001 AND 910003;

INSERT INTO livro(id,titulo,autor,isbn,ano,editora,quantidade)
SELECT 910100 + posicao, 'TCC JOB Livro ' || posicao, 'Autor de ensaio',
       'TCC-JOB-' || posicao, 2026, 'Ensaio', 50
FROM generate_series(1,6) AS numeros(posicao);

INSERT INTO emprestimo(id,id_livro,id_usuario,data_emprestimo,data_prevista_devolucao,data_devolucao_real,multa) VALUES
(911001,910101,910001,:'data_referencia'::date-21,:'data_referencia'::date-14,NULL,777.00),
(911002,910102,910002,:'data_referencia'::date-21,:'data_referencia'::date-14,NULL,777.00),
(911003,910103,910003,:'data_referencia'::date-21,:'data_referencia'::date-14,NULL,777.00),
(911004,910104,910001,:'data_referencia'::date,:'data_referencia'::date+7,NULL,0.00),
(911005,910105,910001,:'data_referencia'::date-7,:'data_referencia'::date,NULL,0.00),
(911006,910106,910001,:'data_referencia'::date-21,:'data_referencia'::date-14,:'data_referencia'::date-7,19.50);

UPDATE livro SET quantidade = quantidade - (
    SELECT count(*) FROM emprestimo
    WHERE emprestimo.id_livro=livro.id AND data_devolucao_real IS NULL
) WHERE id BETWEEN 910101 AND 910106;

INSERT INTO emprestimo(id,id_livro,id_usuario,data_emprestimo,data_prevista_devolucao,data_devolucao_real,multa)
SELECT 912000 + (ranking.posicao-1)*100 + ocorrencia.numero,
       910100 + ranking.posicao, 910001,
       date_trunc('month',:'data_referencia'::date)::date,
       date_trunc('month',:'data_referencia'::date)::date+7,
       date_trunc('month',:'data_referencia'::date)::date, 0.00
FROM (VALUES (1,60),(2,50),(3,40),(4,30),(5,20),(6,10)) AS ranking(posicao,quantidade)
CROSS JOIN LATERAL generate_series(1,ranking.quantidade) AS ocorrencia(numero);

SELECT setval(pg_get_serial_sequence('usuario','id'),(SELECT max(id) FROM usuario));
SELECT setval(pg_get_serial_sequence('livro','id'),(SELECT max(id) FROM livro));
SELECT setval(pg_get_serial_sequence('emprestimo','id'),(SELECT max(id) FROM emprestimo));
COMMIT;

SELECT :'data_referencia'::date AS referencia, current_setting('TimeZone') AS fuso_banco,
       (SELECT count(*) FROM emprestimo WHERE id BETWEEN 911001 AND 912999) AS emprestimos_de_ensaio;
