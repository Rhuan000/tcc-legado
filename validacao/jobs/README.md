# Validacao observavel dos jobs

Esta massa e para validacao funcional e capturas de evidencia, nao para as
medicoes de desempenho. Nao altera Java de producao nem `init-db.sql`.
Usar as mesmas entradas no pre e no pos, restaurando a base entre ensaios.

## O que ja existia e o que faltava

`AtualizacaoMultasIntegrationTest` ja exercitava erros HTTP, contexto invalido,
retomada, tipo atual, devolvidos, devolucao pendente e um disparo real do
agendador Quarkus. O legado e a BrasilAPI eram simulados por WireMock, enquanto
o PostgreSQL era real e temporario. Isso nao era evidencia de JBoss real.

O novo teste `agendadorProcessaMassaMistaComValoresExatos` confere sete casos em
uma rodada do agendador real. Com calendario simulado sem feriados e atraso de
14 dias corridos, exige 20,00 para aluno, 5,00 para professor e 10,00 para
bolsista. Preserva o devolvido e a devolucao pendente, e nao consulta usuarios
de emprestimos nao vencidos. Suite completa verificada: 161 testes aprovados,
incluindo os novos casos de bibliotecario em prazo, multa, API e contexto legado.

O `init-db.sql` tem datas fixas entre junho e setembro de 2026. Ha atrasados,
mas o ranking usa o mes atual e pode ficar vazio posteriormente. Esta massa
resolve a falta de cenarios identificados e dados mensais renovaveis.

## 1. Preparar a base e a data

Parar JBoss e servico antes de preparar dados. Reutilizar o container atual;
nao sao necessarios tres containers nem separar fisicamente os bancos.
Os comandos de reset abaixo apagam os dados do banco descartavel `biblioteca`.
Se existirem dados a preservar, exporta-los antes. Nao usar contra producao.

```powershell
docker exec biblioteca-postgres dropdb -U postgres --if-exists biblioteca
docker exec biblioteca-postgres createdb -U postgres biblioteca
docker cp init-db.sql biblioteca-postgres:/tmp/tcc-init-db.sql
docker exec biblioteca-postgres psql -X -U postgres -d biblioteca -v ON_ERROR_STOP=1 -f /tmp/tcc-init-db.sql
```

Para o pre, usar o `init-db.sql` do worktree/tag pre. Para o pos, acrescentar:

```powershell
docker cp migracoes/001-comandos-emprestimo.sql biblioteca-postgres:/tmp/tcc-migracao.sql
docker exec biblioteca-postgres psql -X -U postgres -d biblioteca -v ON_ERROR_STOP=1 -f /tmp/tcc-migracao.sql
```

Escolher uma data de referencia igual para os ensaios comparados. Sem parametro,
a massa usa CURRENT_DATE do banco. Para observar o job legado da meia-noite,
preparar a massa para a data do proximo disparo com `-v data_referencia=AAAA-MM-DD`.
Nao alterar o relogio do computador. Conferir datas e fusos do banco/JVM.

```powershell
docker cp validacao/jobs/preparar-massa.sql biblioteca-postgres:/tmp/tcc-preparar-massa.sql
docker exec biblioteca-postgres psql -X -U postgres -d biblioteca -f /tmp/tcc-preparar-massa.sql
docker cp validacao/jobs/consultar-evidencias.sql biblioteca-postgres:/tmp/tcc-consultar.sql
docker exec biblioteca-postgres psql -X -U postgres -d biblioteca -f /tmp/tcc-consultar.sql
```

Para fixar outra data, acrescentar `-v data_referencia=AAAA-MM-DD` antes de `-f`.
Nao executar a massa duas vezes: ela recusa IDs ocupados. Restaurar a base
para repetir. Sao adicionados tres usuarios, seis livros e 216 emprestimos;
as sequencias sao alinhadas para permitir operacoes posteriores pela interface.
Usuarios de teste usam matriculas 2091001, 2091002 e 2091003 e recebem perfil
CONSULTA com a senha de ensaio Desenvolvedores. Para operacoes administrativas,
entrar com 2024001. Nao capturar senha ou token nas imagens.

## 2. Multas antes e depois do disparo

| ID | Cenario | Antes | Resultado apos job |
| --- | --- | --- | --- |
| 911001 | Aluno atrasado 14 dias | 777,00 | Dias uteis do intervalo x 2,00 |
| 911002 | Professor atrasado 14 dias | 777,00 | Dias uteis do intervalo x 0,50 |
| 911003 | Bolsista atrasado 14 dias | 777,00 | Dias uteis do intervalo x 1,00 |
| 911004 | Vencimento futuro | 0,00 | Permanece 0,00 |
| 911005 | Vencimento na referencia | 0,00 | Permanece 0,00 |
| 911006 | Ja devolvido | 19,50 | Permanece 19,50 |

777,00 e sentinela para tornar a mudanca visivel, nao multa esperada. Os 19,50
do devolvido tambem sao um valor sentinela para verificar que nao e reprocessado.
Com a BrasilAPI real, nao assumir dez dias uteis: arquivar os feriados efetivos.

No pos, para ensaio acelerado, iniciar o servico em um terminal com:

```powershell
$env:EMPRESTIMO_MULTAS_CRON = '0/15 * * * * ?'
```

Iniciar a aplicacao pelo procedimento normal, com banco e tokens de integracao
configurados. O agendador le essa variavel ao subir. Registrar que o cron foi
acelerado apenas no ensaio. Depois, remover a variavel e reiniciar com o cron
padrao antes das medicoes. Nao modificar silenciosamente o snapshot legado:
o cron de multas pre esta fixado em meia-noite; planejar o disparo nesse horario
ou documentar separadamente uma futura instrumentacao de teste.

Capturar a consulta dos seis IDs antes, iniciar as aplicacoes e aguardar o job,
sem devolver os emprestimos manualmente nem chamar o calculo por HTTP. Capturar
a mesma consulta depois, com horario. A conferencia numerica e:

```powershell
.\validacao\jobs\conferir-multas.ps1 -Saida .experimento/evidencias/pos-multas-ciclo1
```

O conferidor so le banco e BrasilAPI, salva resposta de feriados, observado,
esperado, resultado e contexto. Ele nao executa o job nem corrige valores.
Tem que rodar na data de referencia da massa. No pos com cron acelerado,
repetir apos outro ciclo com outra pasta e conferir que multas nao acumulam.
No pre com cron diario, o segundo ciclo ocorre em outra data: registrar novo
esperado/calendario em ensaio separado, sem chamar este conferidor com a massa
do dia anterior. Nao reutilizar pasta de saida.
Datas de fronteira de ano seguem a regra atual: feriados do ano do vencimento.

Prova adicional do tipo atual: alterar o usuario `2091001` para PROFESSOR
pela tela Editar Usuario, aguardar outro ciclo e conferir novamente. Arquivar
esse ensaio separadamente; nao reutilizar essa base como estado inicial do pre.

## 3. Destaques mensais

O job do legado executa a cada cinco minutos. A massa cria contagens mensais
decrescentes para os livros 910101 a 910106. Os casos abertos podem adicionar
unidades a essas contagens; usar a consulta SQL como oraculo, nao memorizar
somente 60/50/40/30/20. A ordem esperada em base restaurada e 910101 a 910105.

Executar `consultar-evidencias.sql` antes e depois de uma fronteira de cinco
minutos. Capturar ranking SQL, linhas MAIS_EMPRESTADO e tela Destaques. No pos,
o job continua no JBoss e consulta contagens por HTTP; nao afirmar que todos
os jobs migraram. Conferir novamente apos outro ciclo: continuam cinco
destaques da categoria, sem duplicacao. Os demais destaques nao sao o alvo.

## 4. Recuperacao de pendencia, apenas no pos

Executar este cenario depois do ranking, em massa limpa, com aplicacoes paradas.
O script injeta o estado persistido apos reserva de estoque e antes da conclusao
remota. E uma falha simulada por estado, nao prova de timeout HTTP real.

```powershell
docker cp validacao/jobs/preparar-recuperacao-pos.sql biblioteca-postgres:/tmp/tcc-pendente.sql
docker exec biblioteca-postgres psql -X -U postgres -d biblioteca -f /tmp/tcc-pendente.sql
docker cp validacao/jobs/consultar-recuperacao-pos.sql biblioteca-postgres:/tmp/tcc-recuperacao.sql
docker exec biblioteca-postgres psql -X -U postgres -d biblioteca -f /tmp/tcc-recuperacao.sql
```

Antes: chave `91000000-0000-4000-8000-000000000001` PENDENTE; estoque do livro
910106 igual a 49; nenhuma operacao remota para essa chave. Iniciar servico e
JBoss, aguardar a recuperacao a cada 30 segundos e repetir a consulta.
Depois: fluxo e operacao CONCLUIDA, IDs de emprestimo iguais e registro existente.
O estoque permanece 49. Aguardar mais dois ciclos: o mesmo ID e estoque devem
permanecer, sem nova criacao. Capturar consultas com horarios distintos.

Para demonstrar tambem uma falha HTTP real, fazer ensaio separado: interromper
apenas o servico, tentar criar pela interface, registrar erro e pendencia,
restaurar servico e observar recuperacao. Nao afirmar esse cenario aprovado
apenas porque a injecao SQL foi recuperada.

## 5. Evidencias e limites

Guardar comando, commit de cada aplicacao, data, cron/fuso, entrada, esperado,
observado, screenshot e logs originais. Uma imagem da tela nao prova sozinha
que a atualizacao veio do job. Conjugar captura antes/depois, configuracao de
agendamento, registros temporais e testes do disparo real.

Os scripts SQL foram verificados em PostgreSQL 14 descartavel. A suite passou
com 154 testes. O conferidor identificou as tres sentinelas nao processadas.
Essas verificacoes nao significam que o ensaio completo em JBoss ja foi feito.
O autor informou que validou criacao/devolucao antes e depois das correcoes;
faltam consolidar registros e observar os jobs com essa massa.

Depois dos ensaios, restaurar init-db/migracoes e configuracao padrao para as
coletas. A adicao do teste muda o custo de build do pos: fixar o novo commit e
informar 154 testes na campanha, sem reutilizar tempos de pilotos anteriores.
