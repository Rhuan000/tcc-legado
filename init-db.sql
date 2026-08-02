-- init-db.sql
-- Criação das tabelas

CREATE TABLE IF NOT EXISTS livro (
    id SERIAL PRIMARY KEY,
    titulo VARCHAR(200) NOT NULL,
    autor VARCHAR(200),
    isbn VARCHAR(20) UNIQUE,
    ano INT,
    editora VARCHAR(100),
    quantidade INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS usuario (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    matricula VARCHAR(20) UNIQUE NOT NULL,
    email VARCHAR(100),
    tipo VARCHAR(20) NOT NULL  -- ALUNO, PROFESSOR, BOLSISTA
);

CREATE TABLE IF NOT EXISTS emprestimo (
    id SERIAL PRIMARY KEY,
    id_livro INT NOT NULL,
    id_usuario INT NOT NULL,
    data_emprestimo DATE NOT NULL,
    data_prevista_devolucao DATE NOT NULL,
    data_devolucao_real DATE,
    multa DECIMAL(10,2) DEFAULT 0.00,
    FOREIGN KEY (id_livro) REFERENCES livro(id),
    FOREIGN KEY (id_usuario) REFERENCES usuario(id)
);

-- Inserção de dados iniciais (opcional)
INSERT INTO livro (titulo, autor, isbn, ano, editora, quantidade)
VALUES 
    ('Arquitetura de Software', 'Martin Fowler', '978-85-7522-123-4', 2020, 'Novatec', 5),
    ('Engenharia de Software', 'Ian Sommerville', '978-85-352-1254-5', 2018, 'Pearson', 3)
ON CONFLICT (isbn) DO NOTHING;

INSERT INTO usuario (nome, matricula, email, tipo)
VALUES 
    ('João Silva', '20210001', 'joao@email.com', 'ALUNO'),
    ('Maria Souza', '20210002', 'maria@email.com', 'PROFESSOR'),
    ('Carlos Santos', '20210003', 'carlos@email.com', 'BOLSISTA')
ON CONFLICT (matricula) DO NOTHING;
