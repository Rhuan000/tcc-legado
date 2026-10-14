# FAL-01, OBS-01, OBS-02 e PEN-01 — pré-migração

## Correções

- FAL-01: usuários com qualquer empréstimo, ativo ou devolvido, não podem ser
  excluídos. Livros com histórico também são preservados. A consulta falha de
  forma fechada; erro de persistência não produz sucesso na tela. As FKs de
  empréstimo usam RESTRICT, inclusive para proteger a corrida entre consulta
  e exclusão. A cascata de credenciais continua válida para usuários sem histórico.
- OBS-01: ano ausente aparece como `-`, igual ao detalhe.
- OBS-02: validações de empréstimo/devolução usam a exceção de aplicação já
  existente. A tela apresenta apenas a mensagem de negócio; falhas técnicas
  recebem mensagem genérica e stack trace no log.
- PEN-01: teste dispara Quartz de verdade, executando job, EJB, serviço e DAO.
  Consulta de atrasados indisponível agora faz o job falhar, sem anunciar sucesso.

JNDI, CDI/EJB, dependência circular, cache global, JDBC e validação duplicada
do legado foram preservados. Não se trata de refatoração arquitetural.

## Banco existente

Com a aplicação parada, aplicar `migracoes/002-preservar-historico.sql` ao
banco **pré-migração**. Não recriar o banco com init-db para atualizar uma base
que precisa ser preservada. O script é transacional e reexecutável.
Para uma nova massa, o init-db corrigido já inclui as restrições.
Empréstimos anteriormente apagados não são recuperados pelo patch: exigem backup
ou outra evidência confiável. Nenhum dado do ensaio original foi reconstruído.

## Validação reproduzível

Usar JDK 8 (o Java padrão da máquina é 21):

```powershell
$env:JAVA_HOME='C:/Program Files (x86)/Java/jdk1.8.0_201'
mvn -B -f monolito-biblioteca/pom.xml test
mvn -B -f monolito-biblioteca/pom.xml '-Dtest.jdbc.url=jdbc:postgresql://localhost:5432/biblioteca' package
```

Os testes criam e removem somente schemas com UUID, sem alterar `public`.
`RegressaoUsuariosTest` repete LIM-01 com usuário 37, livro 32 sem estoque e
empréstimo 82, verificando empréstimo, credencial, usuário e estoque após a recusa.
Também cobre histórico devolvido, exclusão sem histórico, erro de consulta,
erro de persistência e exclusão direta protegida por FK.
`RegressaoEmprestimosTest` cobre EMP-09 e LIM-02 pela action e EJB/serviço.

No teste `MultaDiariaJobTest`, seis multas começam em 7,00. Após disparo Quartz,
os valores são **20,00; 5,00; 10,00; 20,00; 7,00; 7,00**: aluno, professor,
bolsista e bibliotecário com dez dias úteis de atraso; não vencido e devolvido
inalterados. Reexecução mantém os mesmos valores. O calendário e os usuários são
controlados; empréstimos são persistidos no banco selecionado. Só o lookup JNDI
é substituído no teste. Isso comprova execução controlada, não observação de
JBoss à meia-noite nem chamada real à BrasilAPI.

Resultado desta revisão: 31 testes aprovados em H2 e 31 em PostgreSQL local;
WAR empacotado com sucesso. PEN-01 validado por disparo controlado em ambos.
O SQL real da migração também foi aplicado duas vezes a uma cópia da massa
com as antigas cascatas, em schema isolado e transação revertida ao final:
exclusões diretas foram recusadas e os 77 empréstimos foram preservados.
O schema `public` e os dados do ensaio original não foram alterados.
