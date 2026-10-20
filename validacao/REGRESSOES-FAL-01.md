# FAL-01, OBS-01, OBS-02 e PEN-01 — pós-migração

## Correções

- FAL-01: a exclusão de usuário consulta a API de empréstimos pela ACL e é
  recusada para empréstimos ativos ou devolvidos. Uma falha da API impede a
  exclusão. O mesmo bloqueio por histórico se aplica aos livros.
- O diário local `fluxo_emprestimo` referencia usuário e livro com RESTRICT.
  Isso protege também a reserva pendente, antes da chamada HTTP: ou a reserva
  acontece primeiro e impede a exclusão, ou a exclusão acontece primeiro e
  impede a reserva. O empréstimo continua pertencendo ao serviço moderno;
  não se recriam FKs da tabela remota nem consultas SQL entre domínios.
- Falhas de persistência na exclusão chegam à action, sem falso sucesso.
- OBS-01: lista e detalhe usam `-` para ano ausente.
- OBS-02: indisponibilidade de estoque tem mensagem de negócio; falhas técnicas
  têm texto genérico na tela e stack trace no log. A orientação para repetir
  o mesmo formulário após falha de comunicação foi preservada.

A devolução no pós continua **idempotente**: repetir o comando confirma o
mesmo resultado, sem incrementar o estoque outra vez. A recusa da repetição
do legado não deve substituir esse contrato de recuperação do serviço moderno.
O serviço moderno não precisou de mudança nas regras de multa/devolução.

## Aplicação em banco existente

Com as aplicações paradas, aplicar o arquivo desta branch:
`migracoes/002-preservar-historico.sql`, depois da migração 001 já existente.
Ele é transacional, reexecutável e preserva os registros. Se houver referências
órfãs no diário anterior, a validação da FK falha e a transação é revertida;
é necessário reconciliar esses dados com evidência antes de reaplicar.
Para base nova, init-db + 001 desta revisão já incluem as proteções.

O arquivo 002 da branch pré-migração é diferente e exclusivo daquele estágio.
Não usar init-db para atualizar dados a preservar. Registros apagados antes
desta correção não são recuperados automaticamente.

## Evidência executada nesta revisão

```powershell
$env:JAVA_HOME='C:/Program Files (x86)/Java/jdk1.8.0_201'
mvn -B -f monolito-biblioteca/pom.xml '-Dtest.jdbc.url=jdbc:postgresql://localhost:5432/biblioteca' package
$env:JAVA_HOME='C:/Program Files/Java/jdk-21'
mvn -B -f tcc-moderno/emprestimo-service/pom.xml verify
```

- Monólito pós: **32 testes aprovados**, nenhum ignorado; WAR gerado.
  Schemas isolados por UUID, removidos ao final. LIM-01 reproduzido na action,
  serviço e DAO, simulando a fronteira HTTP. Inclui empréstimos devolvidos,
  falhas de consulta/persistência, reservas pendentes, usuário excluído antes
  da reserva, falta de estoque e mensagens sem detalhes técnicos.
- Serviço moderno: **161 testes aprovados**, nenhum ignorado, incluindo 20
  de `AtualizacaoMultasIntegrationTest`; artefato Quarkus gerado.
  A fase Failsafe está desativada pela configuração existente; os testes
  Quarkus de integração acima rodam via Surefire.
- PEN-01: `agendadorProcessaMassaMistaComValoresExatos` disparou o agendador
  real com PostgreSQL temporário e APIs simuladas. Antes, os sete valores
  eram `[0, 0, 0, 0, 19.50, 7, 0]`; depois ficaram
  **`[20, 5, 10, 0, 19.50, 7, 0]`**. Não vencidos, devolvido e devolução
  pendente foram preservados. Outro teste confirma reexecução sem acumulação.
- SQL real das duas branches validado no PostgreSQL: aplicação repetida da
  migração, recusa de exclusões e preservação dos 77 empréstimos da massa.
  Ensaio em schema isolado com rollback; o schema `public` não foi alterado.

PEN-01 fica comprovado por disparo controlado, sem afirmar observação do
ambiente implantado à meia-noite. Nenhum deploy nem alteração do banco do
ensaio original foi realizado. Os antipadrões do legado permanecem na branch pré.
