# 🏛️ Sistema de Biblioteca Legado – TCC

**Este projeto é o monolito legado utilizado no Trabalho de Conclusão de Curso (TCC) para demonstrar a migração incremental de um sistema monolítico para microsserviços, utilizando o padrão Strangler Fig e a Anti-Corruption Layer.**

---

## 📌 Visão Geral

O sistema simula uma aplicação corporativa legada com **alto acoplamento**, **baixa coesão** e **ausência de testes automatizados**, características típicas de sistemas que evoluíram organicamente. A arquitetura foi propositalmente construída com **"venenos"** para representar um cenário real de modernização.

O objetivo principal é extrair a lógica de cálculo de multa – espalhada entre Action, EJB, Service e DAO – para um microsserviço independente, medindo o impacto por meio de métricas de qualidade (CBO, tempo de build e tempo de inicialização).

---

## 🛠️ Tecnologias

- **Linguagem:** Java 8 (JDK 1.8)
- **Framework Web:** Apache Struts 1.3.10
- **Camada de Negócio:** EJB 3.x (Session Beans)
- **Injeção de Dependência:** CDI (JSR‑299)
- **Agendamento:** Quartz Scheduler 2.3.2
- **Acesso a Dados:** JDBC (DriverManager)
- **Banco de Dados:** PostgreSQL 14+
- **Servidor de Aplicação:** Red Hat JBoss EAP 7.4
- **Gerenciador de Build:** Maven 3.8+

---

## 🧱 Arquitetura (Camadas)

A aplicação segue uma arquitetura em camadas:

1. **Struts Action** – Recebe requisições HTTP, faz lookup JNDI.
2. **EJB (Session Bean)** – Gerencia transações (`@TransactionAttribute`).
3. **Service (POJO)** – Contém regras de negócio (validações, cálculos).
4. **DAO (JDBC)** – Acesso a dados com `DriverManager`.
5. **PostgreSQL** – Banco de dados relacional.

**Componentes paralelos:**
- **Quartz Scheduler** – Lê configuração do `jobs.xml`.
- **Job (MultaDiariaJob)** – Processo agendado (00:00 e a cada 5 min).
- **EmprestimoEJB** – Reutiliza a lógica de negócio existente.

---

## 📋 Requisitos Funcionais (RF)

### Livros
- **RF01 – Cadastrar Livro**  
  Registrar novo livro com título, autor, ISBN, ano, editora e quantidade.  
  *Regras:* Título obrigatório; ISBN único; quantidade ≥ 0.

- **RF02 – Listar Livros**  
  Exibir todos os livros cadastrados, ordenados por título.  
  *Exibe:* ID, Título, Autor, ISBN, Ano, Editora e Quantidade.

- **RF03 – Buscar Livro**  
  Consultar livro por ID ou ISBN.  
  *Regra:* Mensagem de erro se não encontrado.

- **RF04 – Editar Livro**  
  Atualizar dados de um livro existente.  
  *Regras:* ISBN não pode ser alterado se já em uso; quantidade não pode ser negativa.

- **RF05 – Excluir Livro**  
  Remover um livro do sistema.  
  *Regras:* Não permite exclusão se quantidade > 0 ou se livro estiver em empréstimo ativo.

### Usuários
- **RF06 – Cadastrar Usuário**  
  Registrar novo usuário com nome, matrícula, email e tipo.  
  *Regras:* Matrícula única; tipo: `ALUNO`, `PROFESSOR` ou `BOLSISTA`.

- **RF07 – Listar Usuários**  
  Exibir todos os usuários cadastrados, ordenados por nome.  
  *Exibe:* ID, Nome, Matrícula, Email e Tipo.

- **RF08 – Editar Usuário**  
  Atualizar dados de um usuário.  
  *Regra:* Matrícula **não pode ser alterada** (proposital).

- **RF09 – Excluir Usuário**  
  Remover um usuário do sistema.  
  *Regra:* Não permite exclusão se tiver empréstimos ativos.

### Empréstimos e Multas
- **RF10 – Realizar Empréstimo**  
  Associar livro a usuário com datas de empréstimo e devolução prevista.  
  *Regras:* Livro disponível; usuário ativo; prazo: 7d (aluno), 14d (professor), 10d (bolsista).  
  ⚠️ **VENENO:** Lógica de multa "potencial" calculada no ato do empréstimo, espalhada entre Action, EJB, Service e DAO.

- **RF11 – Registrar Devolução**  
  Registrar data de devolução real e calcular multa, se houver atraso.  
  *Regras:* Devolução só permitida se empréstimo ativo; multa: Prof R$0,50/dia, Aluno R$2,00/dia, Bolsista R$1,00/dia.  
  ⚠️ **VENENO:** Lógica de multa duplicada em múltiplas camadas.

### Jobs Agendados
- **RF12 – Job Diário de Multa**  
  Processo agendado (00:00) que atualiza multas de empréstimos atrasados.  
  *Regras:* Busca empréstimos com `data_devolucao_real IS NULL` e `data_prevista_devolucao < CURRENT_DATE`; recalcula e atualiza multa.  
  ⚠️ **VENENO:** Configurado via `jobs.xml` (Quartz) e depende do EJB.

- **RF13 – Job de Teste (5 min)**  
  Versão do Job que roda a cada 5 minutos para testes.  
  *Regra:* Configurado no mesmo `jobs.xml`; útil para validação sem esperar a meia-noite.

---

## 🗄️ Diagrama do Banco de Dados (PostgreSQL)

```dbml
Table "livro" {
  "id" SERIAL [pk]
  "titulo" VARCHAR(200) [not null]
  "autor" VARCHAR(200)
  "isbn" VARCHAR(20) [unique]
  "ano" INT
  "editora" VARCHAR(100)
  "quantidade" INT [default: 0]
}

Table "usuario" {
  "id" SERIAL [pk]
  "nome" VARCHAR(100) [not null]
  "matricula" VARCHAR(20) [unique, not null]
  "email" VARCHAR(100)
  "tipo" VARCHAR(20) [not null]  // ALUNO, PROFESSOR ou BOLSISTA
}

Table "emprestimo" {
  "id" SERIAL [pk]
  "id_livro" INT [not null]
  "id_usuario" INT [not null]
  "data_emprestimo" DATE [not null]
  "data_prevista_devolucao" DATE [not null]
  "data_devolucao_real" DATE
  "multa" DECIMAL(10,2) [default: 0.00]
}

Ref "fk_emprestimo_livro" : "livro"."id" < "emprestimo"."id_livro"
Ref "fk_emprestimo_usuario" : "usuario"."id" < "emprestimo"."id_usuario"