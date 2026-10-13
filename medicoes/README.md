# Campanha revisada: pre e pos-migracao

Status: instrumento em validacao. Build e CBO foram ensaiados; inicializacao
e validacao funcional integrada ainda estao pendentes. Nao iniciar uma campanha
final antes de concluir essas etapas e revisar `docs/tcc/ESTADO-DA-CAMPANHA.md`.

O instrumento revisado e `campanha-v2.ps1`. Os coletores anteriores e
`PROTOCOLO-V1.md` permanecem para reproducao historica. O intermediario tem
documentacao qualitativa, sem coleta quantitativa.

## Codigo e evidencias

- Legado corrigido: `experimento/pre-migracao-revisada`, commit `c643e44`.
- Pos integrado: main e `experimento/migracao-revisada`, apos a integracao
  de credenciais e bibliotecario registrada no diario em 13/10/2026.
- Merge anterior: `d5d7507`, com conteudo de `562c074`; nao inclui esses ajustes.
- Main original: primeiro pai do merge, `cc01568`, preservado no historico.
- Correcoes de interface: `48aad74` e `a95c2dc`.

Sao candidatos a snapshots finais. Antes de fixar tags imutaveis
`experimento/pre-migracao-v2` e `experimento/pos-migracao-v2`, validar os mesmos
cenarios de criacao, devolucao, estoque, prazos, multas e mudanca de tipo nos
dois estados. Validar tambem integracao e job do moderno no JBoss real.
Registrar entradas, resultados esperados/observados, ambiente e limitacoes.
Build e health nao demonstram equivalencia funcional.

Usar worktrees limpos e a mesma revisao do coletor nos dois snapshots. O
registro final exige `-EvidenciaFuncional <arquivo.json>` contendo `estado`,
`commit` completo, `resultado: "aprovado"` e `evidencias` identificando registros
reais de execucao. O coletor confere identidade e arquiva o JSON; isso nao
substitui a revisao humana das evidencias. Nao declarar aprovado antecipadamente.

## Escopos

| Estado | Escopo | Build | Inicializacao | CBO |
| --- | --- | --- | --- | --- |
| Pre | monolito | 10 | 10 | 1 analise |
| Pos | monolito remanescente | 10 | 10 | 1 analise |
| Pos | servico | 10 | 10 | 1 analise |
| Pos | conjunto | 10 | 10 | consolidacao das duas analises |

Build: `mvn -B -o clean package`, com dependencias e imagens Docker previamente
carregadas. Inclui testes existentes e empacotamento. O conjunto mede tempo de
parede do monolito seguido do servico, incluindo orquestracao; nao soma medias.
A suite do servico e a ausencia de suite interna do legado devem ser declaradas.

Inicializacao: processos novos a cada repeticao. No conjunto, iniciar servico
e depois JBoss sem esperar health entre as partidas; cronometrar desde antes
da primeira partida ate ambos retornarem HTTP 200 na mesma rodada. Endpoints:
`/monolito-biblioteca/health` e `/q/health/live`. Ambos medem disponibilidade
HTTP, sem exigir banco ou BrasilAPI apenas de um lado. Intervalo solicitado:
100 ms; timeout por consulta: 500 ms. O custo das consultas sequenciais integra
o tempo observado. Nao e garantia de prontidao funcional completa.

O JBoss recebe base temporaria e WAR antes do cronometro. Artefatos sao
reutilizados entre repeticoes e seus hashes registrados em `artefatos.json`.
Encerramento forcado das arvores criadas pelo coletor ocorre fora do tempo
medido. Bases temporarias sao preservadas para auditoria. Instancias existentes
nao sao encerradas; portas ocupadas impedem a coleta.

Agendadores conservam suas configuracoes. Usar banco descartavel de ensaio,
registrar dados iniciais e efeitos dos jobs. Pode-se reutilizar um unico banco,
recriando-o entre estados e sem uso concorrente: isolamento temporal, nao uma
exigencia de tres containers. Nao preservar dados de apresentacao nessa base
durante a campanha. Configurar conexoes e integracao sem versionar segredos.

## Ambiente e execucao

Mesma maquina Windows, sem ensaios/builds concorrentes; Maven 3.9.11; monolito
com JDK 1.8.0_201 e JBoss EAP 7.4.0.GA; servico e analisador com JDK 21.
CK 0.7.1-SNAPSHOT, SHA-256
`2F32FA347EF739E97152741828675A9939AEAFE61BEE3F196EE5AF264E612162`.

O instrumento registra versoes, hash da configuracao JBoss, codigo fisico,
arvore Git e hash do proprio script. Remove opcoes JVM herdadas e restaura o
ambiente ao terminar. No build do servico aplica o fallback de sockets do
diario: `-Djdk.net.unixdomain.tmpdir=<target>/sockets-nao-existentes`, caminho
que deve permanecer inexistente. Registrar tambem o commit dos coletores.

Executar um piloto antes da campanha final. Os comandos abaixo sao um modelo;
substituir os caminhos e a evidencia pelos registros reais.

```powershell
$parametros = @{
    Estado = 'pre-migracao'
    ColetaId = 'final-v2-pre-01'
    Repositorio = 'C:\caminho\worktree-pre'
    ResultadosRaiz = 'C:\desenvolvimento\tcc-legado\metricas'
    CkJar = 'C:\desenvolvimento\ck\target\ck-0.7.1-SNAPSHOT-jar-with-dependencies.jar'
}
.\medicoes\campanha-v2.ps1 @parametros -Etapa registrar -EvidenciaFuncional 'C:\caminho\validacao-pre.json'
.\medicoes\campanha-v2.ps1 @parametros -Etapa build
.\medicoes\campanha-v2.ps1 @parametros -Etapa inicializacao
.\medicoes\campanha-v2.ps1 @parametros -Etapa cbo
.\medicoes\campanha-v2.ps1 @parametros -Etapa conferir
```

No pos, mudar estado, identificador e worktree. Para piloto, adicionar `-Piloto`
em todas as etapas: uma repeticao, identificada no manifesto e fora da analise.
Cada etapa recusa sobrescrita. Apos falha, preservar a campanha incompleta,
documentar a causa e usar outro identificador, sem selecionar amostras.

## Analise e limites

Relatar observacoes, media, mediana, dispersao e diferencas absolutas/relativas.
Dez repeticoes locais nao sao dez sistemas independentes nem demonstram
significancia estatistica por si mesmas. Arquitetura, refatoracao, runtime,
framework e testes mudam simultaneamente; nao isolar causalmente microsservicos.

CBO analisa `src/main/java`, sem testes. Apresentar numero de tipos, distribuicao
e media; consolidar as linhas dos dois componentes identificando sua origem.
A media conjunta e ponderada pelo numero de tipos, nao a soma das medias.
O coletor verifica cobertura dos arquivos; isso nao garante correcao semantica
do CK. Preservar o instrumento e reanalisar ambos os snapshots se ele mudar.

Contratos HTTP, disponibilidade e propriedade dos dados nao sao integralmente
capturados pelo CBO. A regra de tipo atual do usuario conserva dependencia do
legado no job moderno e deve ser declarada na analise.

Todas as campanhas anteriores em `metricas/pre-migracao/`, inclusive a recoleta
de 26/09, estao excluidas da nova analise. Preservar pastas e tags historicas.
Somente campanhas finais identificadas e aprovadas entram nas tabelas.
CSV/logs sao dados primarios; planilha e graficos sao derivados. Commitar os
resultados depois do codigo medido, sem editar manualmente amostras.
