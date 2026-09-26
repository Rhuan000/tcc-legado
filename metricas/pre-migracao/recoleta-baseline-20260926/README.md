# Recoleta rastreavel da linha de base

Esta campanha caracteriza novamente o estado pre-migracao sem substituir a
coleta historica armazenada diretamente em `metricas/pre-migracao/`.

## Identidade da campanha

- Estado: `pre-migracao`
- ColetaId: `recoleta-baseline-20260926`
- Codigo medido: commit `c6e238912a33b3a9c74f8a914f82df1931c7f7ea`
- Coletores: commit `d0f91d8dfa2a4867227b4b57d12ee81751b2f100`
- Escopo do hash: `monolito-biblioteca/pom.xml` e
  `monolito-biblioteca/src`
- Arvore Git do monolito: `b9583897943dced89f7508bb2a5d6007a96e9a11`
- Arvore limpa no momento do registro: sim
- JDK do monolito: `1.8.0_201`
- Maven: `3.9.11`
- JBoss EAP: `7.4.0.GA`
- CK: `0.7.1-SNAPSHOT`
- SHA-256 do CK:
  `2F32FA347EF739E97152741828675A9939AEAFE61BEE3F196EE5AF264E612162`

Os demais identificadores do ambiente, hashes e caminhos efetivos estao nos
CSVs da campanha.

## Resultado auditado

| Medida | n | Media | Mediana | DP amostral | Minimo | Maximo |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| Build (s) | 10 | 9,489546 | 9,094534 | 0,988810 | 9,057714 | 12,151952 |
| Inicializacao (s) | 10 | 5,110341 | 4,951688 | 0,582123 | 4,588122 | 6,352641 |
| CBO por tipo | 40 | 4,625000 | 4,000000 | 3,628149 | 0 | 13 |

Todos os builds terminaram com codigo zero. Todas as inicializacoes retornaram
HTTP 200. As dez inicializacoes utilizaram um unico WAR, identificado pelo
SHA-256 `A1DA639B2BACA7CC12AC98BF10038142D2B9612A70263DB85656A4D0798C8E51`.
O CBO por tipo coincide com a coleta historica nos 40 tipos analisados.

O criterio de inicializacao e disponibilidade HTTP do endpoint `/health`; ele
nao comprova prontidao funcional do banco de dados ou de integracoes externas.

## Como reproduzir

Uma reproducao deve usar outro `ColetaId`, pois os coletores recusam
sobrescrita. O exemplo abaixo isola tanto o codigo medido quanto a versao dos
coletores:

```powershell
$repoPrincipal = 'C:\desenvolvimento\tcc-legado'
$baselineWorktree = 'C:\desenvolvimento\worktrees\tcc-baseline-c6e2389'
$coletoresWorktree = 'C:\desenvolvimento\worktrees\tcc-coletores-d0f91d8'
$resultadosRaiz = Join-Path $repoPrincipal 'metricas'
$novaColetaId = 'reproducao-baseline-aaaammdd'

git -C $repoPrincipal worktree add --detach $baselineWorktree c6e2389
git -C $repoPrincipal worktree add --detach $coletoresWorktree d0f91d8

& "$coletoresWorktree\medicoes\registrar-estado.ps1" `
    -Estado pre-migracao `
    -ColetaId $novaColetaId `
    -Repositorio $baselineWorktree `
    -ResultadosRaiz $resultadosRaiz

& "$coletoresWorktree\medicoes\medir-build.ps1" `
    -Estado pre-migracao `
    -ColetaId $novaColetaId `
    -Repositorio $baselineWorktree `
    -ResultadosRaiz $resultadosRaiz `
    -Repeticoes 10

& "$coletoresWorktree\medicoes\medir-inicializacao.ps1" `
    -Estado pre-migracao `
    -ColetaId $novaColetaId `
    -Repositorio $baselineWorktree `
    -ResultadosRaiz $resultadosRaiz `
    -Repeticoes 10

& "$coletoresWorktree\medicoes\medir-cbo.ps1" `
    -Estado pre-migracao `
    -ColetaId $novaColetaId `
    -Repositorio $baselineWorktree `
    -ResultadosRaiz $resultadosRaiz `
    -CkJar "$repoPrincipal\metricas\ck.jar"
```

Antes de iniciar, as portas 18080 e 19990 devem estar livres. Dependencias
Maven devem estar resolvidas, e o WAR usado nas inicializacoes deve ser aquele
produzido pela ultima repeticao do coletor de build.

Depois de conferir os resultados, os worktrees podem ser removidos com
`git worktree remove`, informando individualmente os dois caminhos acima.
