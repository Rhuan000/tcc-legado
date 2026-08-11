-- =====================================================
-- SCRIPT DE MOCK COMPLETO - BIBLIOTECA LEGADA
-- GERADO EM: 06/08/2026 (VERSÃO FINAL - SEPARADO)
-- =====================================================

-- =====================================================
-- 1. LIMPEZA (ordem correta para FK)
-- =====================================================
DROP TABLE IF EXISTS livro_destaque CASCADE;
DROP TABLE IF EXISTS emprestimo CASCADE;
DROP TABLE IF EXISTS livro CASCADE;
DROP TABLE IF EXISTS usuario_auth CASCADE;
DROP TABLE IF EXISTS usuario CASCADE;

-- =====================================================
-- 2. CRIAÇÃO DAS TABELAS
-- =====================================================

-- Usuários (DOMÍNIO - Regras de negócio)
CREATE TABLE usuario (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    matricula VARCHAR(20) UNIQUE NOT NULL,
    email VARCHAR(100),
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('ALUNO', 'PROFESSOR', 'BOLSISTA', 'ADMIN', 'BIBLIOTECARIO', 'CONSULTA'))
);

-- Livros
CREATE TABLE livro (
    id SERIAL PRIMARY KEY,
    titulo VARCHAR(200) NOT NULL,
    autor VARCHAR(200),
    isbn VARCHAR(20) UNIQUE,
    ano INT,
    editora VARCHAR(100),
    quantidade INT DEFAULT 0 CHECK (quantidade >= 0)
);

-- Empréstimos
CREATE TABLE emprestimo (
    id SERIAL PRIMARY KEY,
    id_livro INT NOT NULL REFERENCES livro(id) ON DELETE CASCADE,
    id_usuario INT NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    data_emprestimo DATE NOT NULL DEFAULT CURRENT_DATE,
    data_prevista_devolucao DATE NOT NULL,
    data_devolucao_real DATE,
    multa DECIMAL(10,2) DEFAULT 0.00
);

-- Livros em Destaque
CREATE TABLE livro_destaque (
    id SERIAL PRIMARY KEY,
    id_livro INT NOT NULL REFERENCES livro(id) ON DELETE CASCADE,
    titulo VARCHAR(200) NOT NULL,
    descricao TEXT,
    desconto DECIMAL(5,2) DEFAULT 0,
    categoria VARCHAR(50) CHECK (categoria IN ('Promoção', 'Bestseller', 'Novo Lançamento', 'Recomendado', 'Clássico', 'Geral', 'MAIS_EMPRESTADO')),
    data_inicio DATE,
    data_fim DATE,
    ativo BOOLEAN DEFAULT TRUE,
    visualizacoes INT DEFAULT 0
);

-- Autenticação (INFRAESTRUTURA - APENAS credenciais)
CREATE TABLE usuario_auth (
    matricula VARCHAR(20) PRIMARY KEY,
    senha_hash VARCHAR(64) NOT NULL, -- SHA-256 = 64 caracteres hex
    CONSTRAINT fk_usuario_auth_usuario 
        FOREIGN KEY (matricula) 
        REFERENCES usuario(matricula) 
        ON DELETE CASCADE
);

-- =====================================================
-- 3. DADOS MOCK - USUÁRIOS (30 registros + 3 para login)
-- =====================================================
INSERT INTO usuario (nome, matricula, email, tipo) VALUES
('Ana Paula Silva', 'mat001', 'ana.silva@email.com', 'ALUNO'),
('Bruno Costa', 'mat002', 'bruno.costa@email.com', 'PROFESSOR'),
('Carla Santos', 'mat003', 'carla.santos@email.com', 'ADMIN'),
('Daniel Oliveira', 'mat004', 'daniel.oliveira@email.com', 'ALUNO'),
('Elena Rodrigues', 'mat005', 'elena.rodrigues@email.com', 'PROFESSOR'),
('Fábio Almeida', 'mat006', 'fabio.almeida@email.com', 'ADMIN'),
('Gabriela Lima', 'mat007', 'gabriela.lima@email.com', 'ALUNO'),
('Henrique Ferreira', 'mat008', 'henrique.ferreira@email.com', 'PROFESSOR'),
('Isabela Pereira', 'mat009', 'isabela.pereira@email.com', 'ADMIN'),
('João Paulo Souza', 'mat010', 'joao.souza@email.com', 'ALUNO'),
('Karina Mendes', 'mat011', 'karina.mendes@email.com', 'PROFESSOR'),
('Leonardo Rocha', 'mat012', 'leonardo.rocha@email.com', 'ADMIN'),
('Mariana Nunes', 'mat013', 'mariana.nunes@email.com', 'ALUNO'),
('Nelson Barros', 'mat014', 'nelson.barros@email.com', 'PROFESSOR'),
('Olivia Cardoso', 'mat015', 'olivia.cardoso@email.com', 'ADMIN'),
('Paulo Henrique', 'mat016', 'paulo.henrique@email.com', 'ALUNO'),
('Renata Alves', 'mat017', 'renata.alves@email.com', 'PROFESSOR'),
('Sandro Moreira', 'mat018', 'sandro.moreira@email.com', 'ADMIN'),
('Tatiana Gonçalves', 'mat019', 'tatiana.goncalves@email.com', 'ALUNO'),
('Ubirajara Freitas', 'mat020', 'ubirajara.freitas@email.com', 'PROFESSOR'),
('Valentina Castro', 'mat021', 'valentina.castro@email.com', 'ADMIN'),
('William Torres', 'mat022', 'william.torres@email.com', 'ALUNO'),
('Ximena Cordeiro', 'mat023', 'ximena.cordeiro@email.com', 'PROFESSOR'),
('Yuri Lopes', 'mat024', 'yuri.lopes@email.com', 'ADMIN'),
('Zuleica Marques', 'mat025', 'zuleica.marques@email.com', 'ALUNO'),
('Adriano Machado', 'mat026', 'adriano.machado@email.com', 'PROFESSOR'),
('Bianca Fonseca', 'mat027', 'bianca.fonseca@email.com', 'ADMIN'),
('César Rangel', 'mat028', 'cesar.rangel@email.com', 'ALUNO'),
('Debora Viana', 'mat029', 'debora.viana@email.com', 'PROFESSOR'),
('Edson Guimarães', 'mat030', 'edson.guimaraes@email.com', 'ADMIN');

-- =====================================================
-- 4. DADOS DE AUTENTICAÇÃO (3 usuários com login)
-- Hashes gerados pelo HashUtil.main()
-- =====================================================
INSERT INTO usuario (nome, matricula, email, tipo) VALUES
('Administrador', '2024001', 'admin@biblioteca.com', 'ADMIN'),
('Professor Numero 1', '2024002', 'professor@biblioteca.com', 'PROFESSOR'),
('Usuario Consulta', '2024003', 'aluno@biblioteca.com', 'ALUNO');

INSERT INTO usuario_auth (matricula, senha_hash) VALUES
('2024001', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918'), -- senha: admin
('2024002', '17c1532ca6cff8f6a3a8200028af6c2580bf37f39e10cb0966e8a573e3b24a1f'), -- senha: professor
('2024003', 'a21d6f3803f0491c32444ef91a0836be243cc4da5186357e805b7009a5b0669b'); -- senha: aluno

-- =====================================================
-- 5. DADOS MOCK - LIVROS (30 registros)
-- =====================================================
INSERT INTO livro (titulo, autor, isbn, ano, editora, quantidade) VALUES
('O Alquimista', 'Paulo Coelho', '978-85-6571-001-0', 1988, 'HarperCollins', 5),
('Dom Casmurro', 'Machado de Assis', '978-85-3211-002-0', 1899, 'Martin Claret', 2),
('Clean Code', 'Robert C. Martin', '978-85-3310-003-0', 2008, 'Alta Books', 10),
('Arquitetura de Software', 'Martin Fowler', '978-85-3220-004-0', 2003, 'Bookman', 1),
('Java Efetivo', 'Joshua Bloch', '978-85-3332-005-0', 2018, 'Alta Books', 3),
('Spring Boot na Prática', 'Vladimir Khorikov', '978-85-3445-006-0', 2020, 'Casa do Código', 0),
('Microservices com Spring Cloud', 'John Doe', '978-85-4567-007-0', 2021, 'TechBooks', 4),
('Python para Análise de Dados', 'Wes McKinney', '978-85-5678-008-0', 2017, 'O''Reilly', 6),
('JavaScript: O Guia Definitivo', 'David Flanagan', '978-85-6789-009-0', 2016, 'Alta Books', 2),
('Design Patterns em Java', 'Erich Gamma', '978-85-7890-010-0', 1995, 'Addison-Wesley', 1),
('Banco de Dados para Web', 'Jennifer Widom', '978-85-8901-011-0', 2015, 'Saraiva', 8),
('Inteligência Artificial', 'Stuart Russell', '978-85-9012-012-0', 2010, 'Pearson', 4),
('Redes de Computadores', 'Andrew Tanenbaum', '978-85-0123-013-0', 2011, 'Prentice Hall', 3),
('Sistemas Operacionais', 'Abraham Silberschatz', '978-85-1234-014-0', 2012, 'LTC', 2),
('Engenharia de Software', 'Ian Sommerville', '978-85-2345-015-0', 2016, 'Pearson', 5),
('Algoritmos e Estruturas de Dados', 'Robert Sedgewick', '978-85-3456-016-0', 2013, 'Addison-Wesley', 3),
('TDD com Java', 'Maurício Aniche', '978-85-4567-017-0', 2019, 'Casa do Código', 7),
('Kubernetes para Desenvolvedores', 'Marko Luksa', '978-85-5678-018-0', 2020, 'Alta Books', 2),
('Docker: Prático', 'Elton Stoneman', '978-85-6789-019-0', 2018, 'Alta Books', 1),
('SQL Completo', 'Alan Beaulieu', '978-85-7890-020-0', 2014, 'O''Reilly', 4),
('Git para Profissionais', 'Scott Chacon', '978-85-8901-021-0', 2014, 'Alta Books', 6),
('Angular: A Prática', 'John Papa', '978-85-9012-022-0', 2019, 'Bookman', 0),
('React: Aprenda na Prática', 'Eric Wastl', '978-85-0123-023-0', 2020, 'Casa do Código', 5),
('Node.js: Além do Básico', 'Pedro Figueiredo', '978-85-1234-024-0', 2018, 'Novatec', 3),
('TypeScript: Guia Completo', 'Boris Cherny', '978-85-2345-025-0', 2019, 'Alta Books', 2),
('Spring Security em Ação', 'Laurentiu Spilca', '978-85-3456-026-0', 2021, 'Bookman', 1),
('Hibernate Eficaz', 'Christian Bauer', '978-85-4567-027-0', 2017, 'Alta Books', 4),
('JPA com Spring Data', 'Anghel Leonard', '978-85-5678-028-0', 2020, 'Casa do Código', 2),
('Kafka: Do Zero ao Expert', 'Neha Narkhede', '978-85-6789-029-0', 2021, 'O''Reilly', 0),
('Elasticsearch para Desenvolvedores', 'Clinton Gormley', '978-85-7890-030-0', 2015, 'O''Reilly', 3);

-- =====================================================
-- 6. DADOS MOCK - EMPRÉSTIMOS (120 registros)
-- =====================================================
INSERT INTO emprestimo (id_livro, id_usuario, data_emprestimo, data_prevista_devolucao, data_devolucao_real, multa) VALUES
(1, 2, '2026-06-01', '2026-06-08', '2026-06-07', 0.00),
(3, 5, '2026-06-03', '2026-06-17', '2026-06-18', 0.50),
(5, 7, '2026-06-05', '2026-06-12', '2026-06-14', 4.00),
(7, 10, '2026-06-07', '2026-06-21', '2026-06-20', 0.00),
(9, 12, '2026-06-09', '2026-06-16', '2026-06-20', 8.00),
(11, 15, '2026-06-11', '2026-06-25', '2026-06-24', 0.00),
(13, 18, '2026-06-13', '2026-06-20', '2026-06-22', 2.00),
(15, 20, '2026-06-15', '2026-06-29', '2026-06-28', 0.00),
(17, 22, '2026-06-17', '2026-06-24', '2026-06-25', 1.00),
(19, 25, '2026-06-19', '2026-07-03', '2026-07-02', 0.00),
(21, 27, '2026-06-21', '2026-06-28', '2026-06-28', 0.00),
(23, 29, '2026-06-23', '2026-07-07', '2026-07-06', 0.00),
(25, 1, '2026-06-25', '2026-07-09', '2026-07-08', 0.00),
(27, 3, '2026-06-27', '2026-07-04', '2026-07-05', 0.50),
(29, 6, '2026-06-29', '2026-07-13', '2026-07-12', 0.00),
(2, 4, '2026-07-01', '2026-07-15', '2026-07-14', 0.00),
(4, 8, '2026-07-02', '2026-07-09', '2026-07-10', 0.50),
(6, 11, '2026-07-03', '2026-07-17', NULL, 0.00),
(8, 14, '2026-07-04', '2026-07-11', '2026-07-11', 0.00),
(10, 16, '2026-07-05', '2026-07-19', '2026-07-18', 0.00),
(12, 19, '2026-07-06', '2026-07-13', '2026-07-15', 2.00),
(14, 21, '2026-07-07', '2026-07-21', '2026-07-20', 0.00),
(16, 23, '2026-07-08', '2026-07-15', '2026-07-16', 2.00),
(18, 26, '2026-07-09', '2026-07-23', '2026-07-22', 0.00),
(20, 28, '2026-07-10', '2026-07-17', '2026-07-18', 2.00),
(22, 30, '2026-07-11', '2026-07-25', NULL, 0.00),
(24, 2, '2026-07-12', '2026-07-19', '2026-07-21', 4.00),
(26, 5, '2026-07-13', '2026-07-27', '2026-07-26', 0.00),
(28, 7, '2026-07-14', '2026-07-21', '2026-07-20', 0.00),
(30, 9, '2026-07-15', '2026-07-29', '2026-07-28', 0.00),
(1, 13, '2026-07-16', '2026-07-23', '2026-07-24', 2.00),
(3, 17, '2026-07-17', '2026-07-31', '2026-07-30', 0.00),
(5, 22, '2026-07-18', '2026-07-25', '2026-07-26', 2.00),
(7, 24, '2026-07-19', '2026-08-02', NULL, 0.00),
(9, 1, '2026-07-20', '2026-07-27', '2026-07-28', 2.00),
(11, 3, '2026-07-21', '2026-08-04', NULL, 0.00),
(13, 6, '2026-07-22', '2026-07-29', '2026-07-30', 0.50),
(15, 10, '2026-07-23', '2026-08-06', NULL, 0.00),
(17, 12, '2026-07-24', '2026-07-31', '2026-08-01', 2.00),
(19, 15, '2026-07-25', '2026-08-08', NULL, 0.00),
(21, 18, '2026-07-26', '2026-08-02', '2026-08-03', 0.50),
(23, 20, '2026-07-27', '2026-08-10', NULL, 0.00),
(25, 23, '2026-07-28', '2026-08-04', '2026-08-05', 2.00),
(27, 25, '2026-07-29', '2026-08-12', NULL, 0.00),
(29, 27, '2026-07-30', '2026-08-06', '2026-08-06', 0.00),
(2, 29, '2026-07-31', '2026-08-14', NULL, 0.00),
(4, 1, '2026-08-01', '2026-08-08', '2026-08-07', 0.00),
(6, 4, '2026-08-02', '2026-08-09', '2026-08-10', 2.00),
(8, 8, '2026-08-03', '2026-08-10', NULL, 0.00),
(10, 11, '2026-08-04', '2026-08-11', '2026-08-12', 2.00),
(12, 14, '2026-08-05', '2026-08-12', NULL, 0.00),
(14, 16, '2026-08-06', '2026-08-13', '2026-08-13', 0.00),
(16, 19, '2026-08-07', '2026-08-14', '2026-08-15', 2.00),
(18, 21, '2026-08-08', '2026-08-15', NULL, 0.00),
(20, 26, '2026-08-09', '2026-08-16', '2026-08-17', 2.00),
(22, 28, '2026-08-10', '2026-08-17', NULL, 0.00),
(24, 30, '2026-08-11', '2026-08-18', '2026-08-18', 0.00),
(26, 2, '2026-08-12', '2026-08-19', '2026-08-20', 2.00),
(28, 5, '2026-08-13', '2026-08-20', NULL, 0.00),
(30, 7, '2026-08-14', '2026-08-21', '2026-08-21', 0.00),
(1, 9, '2026-08-15', '2026-08-22', '2026-08-23', 2.00),
(3, 13, '2026-08-16', '2026-08-23', NULL, 0.00),
(5, 17, '2026-08-17', '2026-08-24', '2026-08-24', 0.00),
(7, 22, '2026-08-18', '2026-08-25', '2026-08-26', 2.00),
(9, 24, '2026-08-19', '2026-08-26', NULL, 0.00),
(11, 1, '2026-08-20', '2026-08-27', '2026-08-28', 2.00),
(13, 3, '2026-08-21', '2026-08-28', NULL, 0.00),
(15, 6, '2026-08-22', '2026-08-29', '2026-08-29', 0.00),
(17, 10, '2026-08-23', '2026-08-30', '2026-08-31', 2.00),
(19, 12, '2026-08-24', '2026-08-31', NULL, 0.00),
(21, 15, '2026-08-25', '2026-09-01', '2026-09-01', 0.00),
(23, 18, '2026-08-26', '2026-09-02', NULL, 0.00),
(25, 20, '2026-08-27', '2026-09-03', '2026-09-03', 0.00),
(27, 23, '2026-08-28', '2026-09-04', '2026-09-05', 2.00),
(29, 25, '2026-08-29', '2026-09-05', NULL, 0.00),
(2, 27, '2026-08-30', '2026-09-06', '2026-09-06', 0.00),
(4, 29, '2026-08-31', '2026-09-07', '2026-09-08', 2.00);

-- =====================================================
-- 7. DADOS MOCK - LIVROS EM DESTAQUE (15 registros)
-- =====================================================
INSERT INTO livro_destaque (id_livro, titulo, descricao, desconto, categoria, data_inicio, data_fim, ativo, visualizacoes) VALUES
(3, 'O Código Limpo em Destaque', 'Um dos livros mais recomendados para programadores.', 15.00, 'Bestseller', '2026-07-01', '2026-08-31', TRUE, 120),
(1, 'Clássico da Literatura Brasileira', 'Obra-prima de Machado de Assis em destaque.', 0.00, 'Clássico', '2026-06-01', '2026-09-30', TRUE, 45),
(5, 'Java Moderno', 'Novidades do Java 17 e práticas avançadas.', 10.00, 'Novo Lançamento', '2026-07-15', '2026-10-15', TRUE, 78),
(9, 'JavaScript na Prática', 'Guia definitivo para desenvolvimento front-end.', 5.00, 'Promoção', '2026-08-01', '2026-08-15', TRUE, 32),
(11, 'Bancos de Dados', 'Livro essencial para quem trabalha com dados.', 0.00, 'Recomendado', '2026-07-01', '2026-08-31', TRUE, 15),
(13, 'Redes de Computadores', 'Fundamentos e práticas modernas.', 20.00, 'Promoção', '2026-08-01', '2026-08-31', TRUE, 9),
(15, 'Engenharia de Software', 'Abordagem prática e atualizada.', 0.00, 'Geral', '2026-06-01', '2026-12-31', TRUE, 22),
(17, 'TDD na Prática', 'Aprenda Test-Driven Development com Java.', 25.00, 'Bestseller', '2026-07-01', '2026-09-30', TRUE, 67),
(19, 'Docker e Containers', 'Guia essencial para implantação de aplicações.', 10.00, 'Novo Lançamento', '2026-08-01', '2026-09-15', TRUE, 41),
(21, 'Git do Básico ao Avançado', 'Controle de versão para todos os níveis.', 0.00, 'Recomendado', '2026-07-01', '2026-08-31', TRUE, 28),
(23, 'React para Iniciantes', 'Desenvolvimento de interfaces com React.', 15.00, 'Promoção', '2026-08-01', '2026-08-20', TRUE, 36),
(25, 'TypeScript Avançado', 'Explore todo o potencial do TypeScript.', 0.00, 'Geral', '2026-07-01', '2026-09-30', TRUE, 14),
(27, 'Hibernate Eficiente', 'Otimize suas aplicações JPA.', 30.00, 'Bestseller', '2026-06-15', '2026-08-15', TRUE, 55),
(28, 'Spring Data JPA', 'Simplifique o acesso a dados com Spring.', 0.00, 'Recomendado', '2026-08-01', '2026-09-30', TRUE, 19),
(30, 'Elasticsearch para Devs', 'Busca e análise em tempo real.', 20.00, 'Novo Lançamento', '2026-07-01', '2026-08-31', TRUE, 7);
