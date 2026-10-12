\set ON_ERROR_STOP on
SELECT CURRENT_TIMESTAMP AS instante, current_database() AS banco, current_setting('TimeZone') AS fuso;

SELECT emprestimo.id, usuario.matricula, usuario.tipo,
       emprestimo.data_prevista_devolucao, emprestimo.data_devolucao_real,
       emprestimo.multa, livro.quantidade AS estoque
FROM emprestimo
JOIN usuario ON usuario.id=emprestimo.id_usuario
JOIN livro ON livro.id=emprestimo.id_livro
WHERE emprestimo.id BETWEEN 911001 AND 911006
ORDER BY emprestimo.id;

SELECT id_livro, count(*) AS emprestimos_no_mes
FROM emprestimo
WHERE data_emprestimo >= date_trunc('month',CURRENT_DATE)::date
  AND data_emprestimo < (date_trunc('month',CURRENT_DATE)+INTERVAL '1 month')::date
GROUP BY id_livro ORDER BY emprestimos_no_mes DESC, id_livro ASC LIMIT 5;

SELECT id_livro,titulo,categoria,data_inicio,data_fim,ativo
FROM livro_destaque WHERE categoria='MAIS_EMPRESTADO'
ORDER BY titulo,id_livro;
