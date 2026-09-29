# 🏛️ Sistema de Biblioteca Legado – TCC

**Este projeto é o monolito legado utilizado no Trabalho de Conclusão de Curso (TCC) para demonstrar a migração incremental de um sistema monolítico para microsserviços, utilizando o padrão Strangler Fig e a Anti-Corruption Layer.**

---

## 📌 Visão Geral

O sistema simula uma aplicação corporativa legada com **alto acoplamento**, **baixa coesão** e **ausência de testes automatizados**, características típicas de sistemas que evoluíram organicamente. A arquitetura foi propositalmente construída com **"venenos"** para representar um cenário real de modernização.

O objetivo principal é extrair incrementalmente a gestão de empréstimos para um microsserviço independente, começando pelas regras de prazo e multa e avançando até consultas, persistência, criação, devolução e processamento agendado. O impacto é avaliado por CBO, tempo de build e tempo de inicialização, sem pressupor melhora em todas as métricas.

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

Esta descrição e os requisitos abaixo registram a arquitetura de origem.
O estado atual da extração está descrito na seção de integração ao final.

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
```

## Integração com o serviço de empréstimos

As consultas, a criação e a devolução de empréstimos são consumidas do `emprestimo-service` pela
Anti-Corruption Layer do monólito. Por padrão, o serviço é procurado em
`http://localhost:8081`. A URL pode ser alterada por uma destas configurações:

- variável de ambiente `EMPRESTIMO_SERVICE_URL`;
- propriedade da JVM `-Demprestimo.service.url=http://host:porta`.

O Struts delega os comandos ao `CoordenadorEmprestimo`. Ele consulta os
cadastros locais e chama a ACL com IDs, tipo do usuário e datas. Não calcula
prazo/multa e não acessa a tabela de empréstimos. Consultas das telas continuam
pela ACL. O DAO e o EJB antigos de empréstimos foram retirados.

O Quartz coordena a atualização diária das multas e retoma fluxos pendentes
a cada 30 segundos. O tipo do usuário é consultado novamente a cada execução
do job de multas e enviado ao serviço; a regra e a escrita continuam remotas.
O cron de multas pode ser alterado com a propriedade `emprestimo.multas.cron`.
O experimento usa uma instância do legado e o fuso da JVM deve ser
`America/Sao_Paulo`.

O job de destaques permanece no legado: recebe IDs e contagens pela ACL e
consulta os livros localmente, sem usuários nem JOIN com empréstimos.
Consulta e carregamento precedem a exclusão dos destaques existentes;
a substituição ainda usa escritas legadas sem transação única.

### Coordenação e recuperação

Na criação, reserva de estoque e registro em `fluxo_emprestimo` são confirmados
na mesma transação local antes da chamada HTTP. Na devolução, o serviço confirma
o empréstimo primeiro; depois, estoque e conclusão do fluxo são gravados juntos.
A reserva anterior à chamada evita aceitar mais empréstimos que exemplares
disponíveis. Uma falha posterior pode manter o exemplar reservado até a retomada.

Não há transação distribuída: estoque e empréstimo podem divergir temporariamente.
Em timeout, a mesma chave é reenviada; o serviço deduplica o comando. O Quartz
retoma operações pendentes mesmo sem nova ação do operador. Falhas persistentes
ou rejeições definitivas exigem inspeção e correção operacional; não há prazo
garantido nem compensação automática. Não apagar os diários de deduplicação.

A chave é gerada pelo formulário. Reenviar o mesmo formulário preserva a chave;
abrir outro representa uma nova solicitação. Tipo e ID de usuário vêm do
cadastro consultado no servidor, nunca de campos aceitos diretamente do navegador.

A ACL envia `X-Integration-Token`, obtido da propriedade
`emprestimo.integracao.token` ou da variável `EMPRESTIMO_INTEGRATION_TOKEN`.
Configurar o mesmo segredo no serviço. Não existe mais
`/integracao/emprestimos`: a comunicação de negócio segue apenas do legado
para o microsserviço. Login, sessão e permissões dos operadores permanecem locais.

Aplicar o script de corte conforme o
[README do serviço](../tcc-moderno/emprestimo-service/README.md#comandos-recebidos-do-legado).
