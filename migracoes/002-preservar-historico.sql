-- Somente POS-migracao, depois de 001-comandos-emprestimo.sql.
-- Aplicar com aplicacoes paradas. Nao recria FKs entre dominios.
-- O diario do coordenador e os cadastros sao locais ao monolito.
-- FK protege inclusive a corrida entre exclusao e reserva de novo emprestimo.
BEGIN;
ALTER TABLE fluxo_emprestimo DROP CONSTRAINT IF EXISTS fluxo_usuario_fkey;
ALTER TABLE fluxo_emprestimo DROP CONSTRAINT IF EXISTS fluxo_livro_fkey;
ALTER TABLE fluxo_emprestimo ADD CONSTRAINT fluxo_usuario_fkey
    FOREIGN KEY (matricula) REFERENCES usuario(matricula) ON DELETE RESTRICT;
ALTER TABLE fluxo_emprestimo ADD CONSTRAINT fluxo_livro_fkey
    FOREIGN KEY (id_livro) REFERENCES livro(id) ON DELETE RESTRICT;
COMMIT;
