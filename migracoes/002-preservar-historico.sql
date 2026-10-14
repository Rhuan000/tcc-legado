-- Somente PRE-migracao. Aplicar com a aplicacao parada; nao apaga registros.
-- Reexecutavel, inclusive em bancos criados pelo init-db.sql corrigido.
BEGIN;
ALTER TABLE emprestimo DROP CONSTRAINT IF EXISTS emprestimo_id_usuario_fkey;
ALTER TABLE emprestimo DROP CONSTRAINT IF EXISTS emprestimo_id_livro_fkey;
ALTER TABLE emprestimo ADD CONSTRAINT emprestimo_id_usuario_fkey
    FOREIGN KEY (id_usuario) REFERENCES usuario(id) ON DELETE RESTRICT;
ALTER TABLE emprestimo ADD CONSTRAINT emprestimo_id_livro_fkey
    FOREIGN KEY (id_livro) REFERENCES livro(id) ON DELETE RESTRICT;
COMMIT;
