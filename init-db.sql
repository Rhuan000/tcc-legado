-- =====================================================
-- SCRIPT DE CRIAÇÃO DO BANCO DE DADOS - BIBLIOTECA LEGADA
-- =====================================================

-- Remove tabelas (ordem correta para respeitar chaves estrangeiras)
DROP TABLE IF EXISTS livro_destaque CASCADE;
DROP TABLE IF EXISTS emprestimo CASCADE;
DROP TABLE IF EXISTS livro CASCADE;
DROP TABLE IF EXISTS usuario CASCADE;

-- =====================================================
-- TABELA USUARIO
-- =====================================================
CREATE TABLE usuario (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    matricula VARCHAR(20) UNIQUE NOT NULL,
    email VARCHAR(100),
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('ALUNO', 'PROFESSOR', 'BOLSISTA'))
);

-- =====================================================
-- TABELA LIVRO
-- =====================================================
CREATE TABLE livro (
    id SERIAL PRIMARY KEY,
    titulo VARCHAR(200) NOT NULL,
    autor VARCHAR(200),
    isbn VARCHAR(20) UNIQUE,
    ano INT,
    editora VARCHAR(100),
    quantidade INT DEFAULT 0 CHECK (quantidade >= 0)
);

-- =====================================================
-- TABELA EMPRESTIMO
-- =====================================================
CREATE TABLE emprestimo (
    id SERIAL PRIMARY KEY,
    id_livro INT NOT NULL REFERENCES livro(id) ON DELETE CASCADE,
    id_usuario INT NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    data_emprestimo DATE NOT NULL DEFAULT CURRENT_DATE,
    data_prevista_devolucao DATE NOT NULL,
    data_devolucao_real DATE,
    multa DECIMAL(10,2) DEFAULT 0.00
);

-- =====================================================
-- TABELA LIVRO_DESTAQUE (Domínio extra para o TCC)
-- =====================================================
CREATE TABLE livro_destaque (
    id SERIAL PRIMARY KEY,
    id_livro INT NOT NULL REFERENCES livro(id) ON DELETE CASCADE,
    titulo VARCHAR(200) NOT NULL,
    descricao TEXT,
    desconto DECIMAL(5,2) DEFAULT 0,
    categoria VARCHAR(50) CHECK (categoria IN ('Promoção', 'Bestseller', 'Novo Lançamento', 'Recomendado', 'Clássico', 'Geral')),
    data_inicio DATE,
    data_fim DATE,
    ativo BOOLEAN DEFAULT TRUE,
    visualizacoes INT DEFAULT 0
);

-- =====================================================
-- DADOS INICIAIS (MOCK)
-- =====================================================

-- Usuários
INSERT INTO usuario (nome, matricula, email, tipo) VALUES 
('Administrador do Sistema', 'admin', 'admin@biblioteca.com', 'PROFESSOR'),
('João Silva', 'joao123', 'joao@email.com', 'ALUNO'),
('Maria Oliveira', 'maria456', 'maria@email.com', 'PROFESSOR'),
('Carlos Souza', 'carlos789', 'carlos@email.com', 'BOLSISTA');

-- Livros
INSERT INTO livro (titulo, autor, isbn, ano, editora, quantidade) VALUES
('O Alquimista', 'Paulo Coelho', '978-85-657-1', 1988, 'HarperCollins', 5),
('Dom Casmurro', 'Machado de Assis', '978-85-321-1', 1899, 'Martin Claret', 2),
('Clean Code', 'Robert C. Martin', '978-85-331-0', 2008, 'Alta Books', 10),
('Arquitetura de Software', 'Martin Fowler', '978-85-322-0', 2003, 'Bookman', 1),
('Java Efetivo', 'Joshua Bloch', '978-85-333-2', 2018, 'Alta Books', 3),
('Spring Boot na Prática', 'Vladimir Khorikov', '978-85-344-5', 2020, 'Casa do Código', 0);

-- Empréstimos (alguns ativos, alguns devolvidos, alguns com multa)
INSERT INTO emprestimo (id_livro, id_usuario, data_emprestimo, data_prevista_devolucao, data_devolucao_real, multa) VALUES
(1, 2, '2026-07-10', '2026-07-17', '2026-07-16', 0.00),  -- devolvido no prazo
(2, 3, '2026-07-01', '2026-07-15', '2026-07-18', 6.00),  -- devolvido com atraso (Professor = R$0,50/dia * 3 dias úteis? O código calcula, mas já deixamos um valor)
(3, 4, '2026-07-20', '2026-08-03', NULL, 0.00),          -- ativo (Bolsista tem 10 dias úteis)
(4, 2, '2026-07-25', '2026-08-01', NULL, 0.00);          -- ativo (Aluno tem 7 dias úteis)

-- Livros em Destaque (categorias variadas para testar o filtro)
INSERT INTO livro_destaque (id_livro, titulo, descricao, desconto, categoria, data_inicio, data_fim, ativo, visualizacoes) VALUES
(3, 'O Livro do Programador', 'Este é o livro mais vendido entre nossos usuários!', 15.00, 'Bestseller', '2026-07-01', '2026-08-31', TRUE, 45),
(1, 'Obra Clássica Brasileira', 'Obras de destaque da literatura nacional', 0.00, 'Clássico', NULL, NULL, TRUE, 12),
(5, 'Novo Livro de Java', 'Recém chegada! Aprenda as novidades do Java 17.', 10.00, 'Novo Lançamento', '2026-07-20', '2026-09-30', TRUE, 8),
(6, 'Promoção de Primavera', 'Livros com desconto especial para estudantes', 25.00, 'Promoção', '2026-08-01', '2026-08-15', TRUE, 30);

-- =====================================================
-- CONSULTAS DE VERIFICAÇÃO (Opcional)
-- =====================================================
-- SELECT * FROM usuario;
-- SELECT * FROM livro;
-- SELECT * FROM emprestimo;
-- SELECT * FROM livro_destaque;
