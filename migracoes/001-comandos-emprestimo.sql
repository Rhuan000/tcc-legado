-- Aplicar uma vez, com as aplicacoes paradas e backup, antes do corte dos comandos.
-- Mantem os emprestimos existentes e remove cascatas entre dominios.
BEGIN;
ALTER TABLE emprestimo DROP CONSTRAINT IF EXISTS emprestimo_id_livro_fkey;
ALTER TABLE emprestimo DROP CONSTRAINT IF EXISTS emprestimo_id_usuario_fkey;

CREATE TABLE emprestimo_operacao (
    chave VARCHAR(100) PRIMARY KEY,
    tipo VARCHAR(12) NOT NULL CHECK (tipo IN ('CRIAR','DEVOLVER')),
    id_emprestimo BIGINT NOT NULL,
    dados TEXT NOT NULL,
    estado VARCHAR(12) NOT NULL CHECK (estado IN ('PENDENTE','CONCLUIDA','REJEITADA')),
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX emprestimo_operacao_pendentes ON emprestimo_operacao(criado_em) WHERE estado='PENDENTE';

-- Diario local do coordenador; nao e persistencia de emprestimos.
CREATE TABLE fluxo_emprestimo (
    chave VARCHAR(100) PRIMARY KEY,
    id_livro BIGINT NOT NULL,
    matricula VARCHAR(100) NOT NULL,
    delta SMALLINT NOT NULL CHECK (delta IN (-1,1)),
    dados TEXT NOT NULL,
    estado VARCHAR(12) NOT NULL DEFAULT 'PENDENTE' CHECK (estado IN ('PENDENTE','CONCLUIDA')),
    id_emprestimo BIGINT,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
COMMIT;
