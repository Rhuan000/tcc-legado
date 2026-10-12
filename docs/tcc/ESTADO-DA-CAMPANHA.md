# Estado da campanha revisada

Registro: 12/10/2026. Este documento separa implementacao, validacao e medicao.

| Item | Situacao | Evidencia |
| --- | --- | --- |
| Legado corrigido | Commitado | `a95c2dc` e `48aad74` |
| Pos integrado na main | Concluido localmente | Merge `d5d7507` |
| Suite do servico | Aprovada | 154 testes, sem falhas/erros/ignorados; inclui massa mista no agendador real |
| Compilacao e codificacao do legado | Aprovadas | WAR gerado; 16 JSPs UTF-8 |
| Piloto CBO | Cobertura conferida | 40 arquivos/tipos pre; 39+26 pos |
| Piloto build | Aprovado nos quatro escopos | Pre monolito; pos monolito, servico e conjunto |
| Coletores v2 | Em validacao | Pilotos locais, sem resultados finais |
| Funcionamento pre/pos em JBoss | Informado pelo autor | Integracao conferida antes/depois das correcoes; falta consolidar evidencias pareadas |
| Jobs integrados | Pendente | Massa reproduzivel em `validacao/jobs/`; disparo Quarkus testado com dependencias HTTP simuladas |
| Tags finais v2 | Pendentes | Criar somente apos validacao |
| Campanhas quantitativas finais | Nao iniciadas | Exigem evidencias e ambiente fixado |
| Tabelas e conclusoes revisadas | Pendentes | Derivar das novas campanhas |

## Ambiente de ensaio

Existe um PostgreSQL `biblioteca-postgres` publicado na porta 5432. Os DAOs do
legado fixam `localhost:5432/biblioteca`. O autor escolheu reutilizar um banco
descartavel, recriado entre pre e pos a partir de init-db e, no pos, migracao.
Isolamento temporal substitui containers permanentes separados. Parar aplicacoes
antes do reset e nao preservar dados de apresentacao nesse banco durante ensaios.
Reexecutar somente init-db nao basta para remover journals criados pela migracao.
Os testes dos novos scripts usaram outro container temporario, sem alterar o
banco do autor. Os comandos destrutivos do roteiro nao foram executados nele.

## Ensaios do instrumento

Worktrees locais ignorados: `.experimento/pre` em `a95c2dc` e `.experimento/pos`
em `d5d7507`. Pilotos em `.experimento/pilotos`, com identificadores distintos.
Os hashes de cada revisao ensaiada constam nos manifestos. Nao misturar esses
ensaios, realizados durante desenvolvimento do instrumento, com dados finais.

Os primeiros pilotos revelaram tratamento de stderr do CK como erro pelo
PowerShell e perda de ExitCode sem reter o handle do processo. O coletor passou
a separar logs e reter o handle. A saida `java -version` tambem alternou o modo
de compartilhamento de classes; a identidade registra versao e hash do
executavel, sem comparar esse detalhe dinamico como mudanca de instalacao.

Foram verificadas recusas de sobrescrita, registro final sem evidencia funcional
e codigo com arquivo nao versionado no escopo. Inicializacao ainda precisa de
ensaio em ambiente isolado. O instrumento ensaiado nos pilotos pre-04 e pos-03
tem SHA-256 `CF3A00827DE23BA64D6057BDD6F881D166AC62ABECA3254866F8EAB01737C111`.
Os registros estao arquivados em `metricas/pilotos/revisao-20261012/`; incluem
tentativas incompletas, logs e manifestos. Sao verificacoes do instrumento,
nao campanhas de desempenho comparaveis; houve tarefas de verificacao em paralelo.

## Sequencia restante

1. Registrar a configuracao e o reset do banco descartavel em cada estado.
2. Validar funcionalidades equivalentes e corrigir/revalidar defeitos encontrados.
3. Concluir o piloto de inicializacao e revisar o instrumento final.
4. Fixar tags e commit do instrumento; registrar evidencias por snapshot.
5. Executar as dez repeticoes e CBO de cada escopo conforme `medicoes/README.md`.
6. Conferir resultados e falhas, versionar dados brutos e atualizar a analise.

As campanhas antigas, inclusive a recoleta de 26/09, estao excluidas da nova
comparacao. Permanecem como registros historicos. Nenhuma afirmacao de reducao
de CBO, build ou inicializacao e sustentada pelos pilotos deste documento.

Roteiro detalhado, revisao documental e capturas: `PLANO-DE-FECHAMENTO.md`.
Antes de coletar, reconciliar a Tabela 1/secao 2.4 do PDF com o coletor: o PDF
descreve tempos por componente dentro de dez rodadas conjuntas; o instrumento
atual realiza series individuais e uma serie conjunta separada.
