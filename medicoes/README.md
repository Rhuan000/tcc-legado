# Coletores e protocolo de medição

Este diretório mantém os **três instrumentos exatos da recoleta de 23/10/2026**.
Os dados publicados estão em [metricas](../metricas/README.md). O procedimento
abaixo detalha a preparação do ambiente e as chamadas diretas aos três
coletores.

| Instrumento | Finalidade | SHA-256 |
| --- | --- | --- |
| `campanha-v2.ps1` | Registro, build com testes, inicialização HTTP e CBO. | `1ED17E68874356015D0707818A20DBD0E1BADE07E3B3C358E1482ACD24EDA17B` |
| `medir-build-monolito-sem-testes.ps1` | Build suplementar do monólito pré/pós. | `55C1BDDEB5CE5F67AF145EE55F5EFC029A15BD714DBEB601127637DEC92759B7` |
| `medir-build-servico-sem-testes.ps1` | Build suplementar do serviço posterior. | `78D274A109BEC3AACAD2583CA22363EED1B02316AC1F4062F63BE39B144E9336` |

O `.gitattributes` preserva esses bytes, inclusive codificação e fins de linha.
Não salve os instrumentos com outra codificação durante uma campanha.
`-Etapa conferir` exige os mesmos código, instrumento, ambiente e parâmetros
do registro; não é um validador independente das campanhas históricas.

## 1. Preparar código e ferramentas

Use PowerShell no Windows com Git disponível. Escolha uma raiz nova **sem
espaços**, exigência do caminho de `JDK_JAVA_OPTIONS` do build do serviço.
Substitua as instalações abaixo se necessário e registre as versões.
Não execute ensaios ou builds concorrentes.

O ambiente da campanha utilizou JDK 1.8.0_201 no monólito, JDK 21.0.8 no serviço
e no CK, Maven 3.9.11, JBoss EAP 7.4, CK 0.7.1-SNAPSHOT e Docker Desktop com
containers Linux. Resolva as dependências Maven antes da coleta: o build medido
é offline (`-o`). Disponibilize também as imagens exigidas pelos testes do
serviço; Quarkus DevServices/Testcontainers gerencia seus containers nos testes.

O CK não é versionado. O SHA-256 exigido de seu JAR é
`2F32FA347EF739E97152741828675A9939AEAFE61BEE3F196EE5AF264E612162`.
A configuração `standalone.xml` do JBoss da campanha tem SHA-256
`07141FF10A4D96A8044AD10B81F38495459E5ED6560B790D76CB66D8FAA029CD`.
Os manifestos registram a configuração realmente usada em cada nova execução.

```powershell
$ErrorActionPreference = 'Stop'
$raiz = 'C:\tcc-auditoria-20261023'
$repoPrincipal = Join-Path $raiz 'repositorio-main'
$repoPre = Join-Path $raiz 'codigo-pre'
$repoPos = Join-Path $raiz 'codigo-pos'
$resultados = Join-Path $raiz 'resultados'
$java8 = 'C:\Program Files (x86)\Java\jdk1.8.0_201'
$java21 = 'C:\Program Files\Java\jdk-21'
$maven = 'C:\Program Files\Maven\apache-maven-3.9.11-bin\apache-maven-3.9.11\bin\mvn.cmd'
$jboss = 'C:\desenvolvimento\jboss-eap-7.4'
$ck = 'C:\desenvolvimento\ck\target\ck-0.7.1-SNAPSHOT-jar-with-dependencies.jar'

if (Test-Path -LiteralPath $raiz) { throw 'Escolha uma raiz nova, sem espacos.' }
New-Item -ItemType Directory -Path $raiz | Out-Null
git -c core.autocrlf=true clone --branch main https://github.com/Rhuan000/tcc-legado.git $repoPrincipal
if ($LASTEXITCODE -ne 0) { throw 'Falha ao clonar o repositorio.' }
git -C $repoPrincipal config core.autocrlf true
if ($LASTEXITCODE -ne 0) { throw 'Falha ao configurar os fins de linha das worktrees.' }
git -C $repoPrincipal worktree add --detach $repoPre aa55edd88c412725c84c10bff92e88a503e39fcb
if ($LASTEXITCODE -ne 0) { throw 'Falha ao preparar o codigo anterior.' }
git -C $repoPrincipal worktree add --detach $repoPos 45ff20b3d68ff484143a7b28e35b7c1d93fc361b
if ($LASTEXITCODE -ne 0) { throw 'Falha ao preparar o codigo posterior.' }

$coletor = Join-Path $repoPrincipal 'medicoes\campanha-v2.ps1'
$semTestesMonolito = Join-Path $repoPrincipal 'medicoes\medir-build-monolito-sem-testes.ps1'
$semTestesServico = Join-Path $repoPrincipal 'medicoes\medir-build-servico-sem-testes.ps1'
Get-FileHash -LiteralPath $coletor, $semTestesMonolito, $semTestesServico -Algorithm SHA256
Get-FileHash -LiteralPath $ck -Algorithm SHA256
```

Compare os valores exibidos com os hashes acima antes de continuar.
As duas worktrees mantêm os snapshots separados; não é necessário trocar de
branch durante a coleta. **Chame os instrumentos de `repositorio-main`**,
e não as versões históricas das worktrees. Preserve `pom.xml` e `src` dos
projetos sem alterações.

## 2. Preparar variáveis, integrações e portas

As variáveis são locais ao processo PowerShell. Os tokens distintos de
48 caracteres servem ao ambiente de ensaio e não devem ser publicados.

```powershell
$env:JAVA_HOME = $java8
$env:Path = "$java8\bin;" + $env:Path
$env:JBOSS_HOME = $jboss
foreach ($nome in @(
    'JDK_JAVA_OPTIONS','JAVA_TOOL_OPTIONS','_JAVA_OPTIONS','JAVA_OPTS',
    'JBOSS_MODULES_SYSTEM_PKGS','STANDALONE_CONF','JBOSS_CONFIG_DIR',
    'JBOSS_LOG_DIR','JBOSS_MODULEPATH','MAVEN_OPTS','MAVEN_ARGS',
    'EMPRESTIMO_MULTAS_CRON'
)) {
    [Environment]::SetEnvironmentVariable($nome, $null, 'Process')
}
$env:EMPRESTIMO_DB_URL = 'jdbc:postgresql://localhost:5432/biblioteca'
$env:EMPRESTIMO_DB_USERNAME = 'postgres'
$env:EMPRESTIMO_DB_PASSWORD = 'postgres'
$env:EMPRESTIMO_INTEGRATION_TOKEN = [guid]::NewGuid().ToString('N') + [guid]::NewGuid().ToString('N').Substring(0,16)
do {
    $env:LEGADO_USUARIOS_INTEGRATION_TOKEN = [guid]::NewGuid().ToString('N') + [guid]::NewGuid().ToString('N').Substring(0,16)
} while ($env:LEGADO_USUARIOS_INTEGRATION_TOKEN -eq $env:EMPRESTIMO_INTEGRATION_TOKEN)
$env:EMPRESTIMO_SERVICE_URL = 'http://127.0.0.1:18081'
$env:LEGADO_USUARIOS_URL = 'http://127.0.0.1:18080/monolito-biblioteca'
$env:LEGADO_USUARIOS_ALLOW_LOCAL_HTTP = 'true'

Get-NetTCPConnection -State Listen -ErrorAction SilentlyContinue |
    Where-Object LocalPort -In @(5432,4712,4713,8443,18080,18081,19990) |
    Select-Object LocalAddress,LocalPort,OwningProcess
```

As portas da lista precisam estar livres nesse ambiente. Identifique qualquer
processo/container listado antes de liberá-las. Se parar seu PostgreSQL habitual,
anote o identificador para reiniciá-lo ao terminar. Não reutilize sua base
habitual no ensaio. Os agendadores conservam suas configurações normais;
remova configurações locais de cron acelerado.

## 3. Criar PostgreSQL inequivocamente descartável

O exemplo cria nome e volume exclusivos, publica somente em localhost e guarda
o identificador retornado por `docker create`. Todos os SQLs seguintes apontam
exclusivamente para esse identificador.

```powershell
$nomeDbAuditoria = 'tcc-auditoria-descartavel-' + [guid]::NewGuid().ToString('N')
$volumeDbAuditoria = $nomeDbAuditoria + '-dados'
$imagemPostgres = 'postgres@sha256:f1341c01408dc7278e9d365ed4f860cd3f87dd16b4464ac326fc0f422083a579'
docker pull $imagemPostgres
if ($LASTEXITCODE -ne 0) { throw 'Falha ao disponibilizar a imagem PostgreSQL.' }
$idDbAuditoria = docker create --name $nomeDbAuditoria --label tcc.finalidade=auditoria-descartavel --publish 127.0.0.1:5432:5432 --env POSTGRES_DB=biblioteca --env POSTGRES_USER=postgres --env POSTGRES_PASSWORD=postgres --volume ($volumeDbAuditoria + ':/var/lib/postgresql/data') $imagemPostgres
if ($LASTEXITCODE -ne 0 -or $idDbAuditoria -notmatch '^[0-9a-f]{64}$') {
    throw 'Container descartavel nao criado; nao continue.'
}
docker start $idDbAuditoria
if ($LASTEXITCODE -ne 0) { throw 'Falha ao iniciar o banco de ensaio.' }
docker exec $idDbAuditoria pg_isready -U postgres -d biblioteca
```

Repita apenas a última chamada até retornar `accepting connections` e código
zero. O digest é o da imagem PostgreSQL 14 Alpine registrada na campanha.

## 4. Recriar a base para cada estado

Recrie a base **antes de cada piloto e de cada campanha final**, com as
aplicações encerradas. O bloco comum confere a identidade do container criado
no item 3 e recria somente seu banco `biblioteca`. Não substitua o identificador
por um container habitual.

```powershell
$infoDbAuditoria = @(docker inspect $idDbAuditoria | ConvertFrom-Json)[0]
if ($LASTEXITCODE -ne 0 -or
    $infoDbAuditoria.Name -ne ('/' + $nomeDbAuditoria) -or
    $infoDbAuditoria.Config.Labels.'tcc.finalidade' -ne 'auditoria-descartavel') {
    throw 'Identidade do container descartavel nao confirmada.'
}
docker exec $idDbAuditoria psql -v ON_ERROR_STOP=1 -U postgres -d postgres -c 'DROP DATABASE IF EXISTS biblioteca WITH (FORCE);'
if ($LASTEXITCODE -ne 0) { throw 'Falha ao recriar a base descartavel.' }
docker exec $idDbAuditoria psql -v ON_ERROR_STOP=1 -U postgres -d postgres -c 'CREATE DATABASE biblioteca;'
if ($LASTEXITCODE -ne 0) { throw 'Falha ao criar a base descartavel.' }
```

Para o **anterior**, aplique somente o SQL da worktree anterior:

```powershell
docker cp "$repoPre\init-db.sql" ($idDbAuditoria + ':/tmp/init-pre.sql')
if ($LASTEXITCODE -ne 0) { throw 'Falha ao copiar o SQL anterior.' }
docker exec $idDbAuditoria psql -v ON_ERROR_STOP=1 -U postgres -d biblioteca -f /tmp/init-pre.sql
if ($LASTEXITCODE -ne 0) { throw 'Falha ao preparar o estado anterior.' }
```

Para o **posterior**, após repetir o bloco comum de recriação, aplique o SQL
inicial e a migração da worktree posterior:

```powershell
docker cp "$repoPos\init-db.sql" ($idDbAuditoria + ':/tmp/init-pos.sql')
if ($LASTEXITCODE -ne 0) { throw 'Falha ao copiar o SQL posterior.' }
docker exec $idDbAuditoria psql -v ON_ERROR_STOP=1 -U postgres -d biblioteca -f /tmp/init-pos.sql
if ($LASTEXITCODE -ne 0) { throw 'Falha ao preparar a base posterior.' }
docker cp "$repoPos\migracoes\001-comandos-emprestimo.sql" ($idDbAuditoria + ':/tmp/migracao-pos.sql')
if ($LASTEXITCODE -ne 0) { throw 'Falha ao copiar a migracao.' }
docker exec $idDbAuditoria psql -v ON_ERROR_STOP=1 -U postgres -d biblioteca -f /tmp/migracao-pos.sql
if ($LASTEXITCODE -ne 0) { throw 'Falha ao aplicar a migracao posterior.' }
```

A massa inicial é a de `init-db.sql`. Os roteiros em
[validacao/jobs](../validacao/jobs/) atendem a verificações funcionais
complementares; não aplique massas adicionais no meio das séries de métricas.

## 5. Definir parâmetros e executar pilotos separados

Escolha identificadores novos para cada tentativa. O exemplo mantém os
resultados fora do repositório e não sobrescreve as campanhas publicadas.

```powershell
$comum = @{
    ResultadosRaiz = $resultados
    JavaLegado = $java8
    JavaServico = $java21
    Maven = $maven
    JbossHome = $jboss
    CkJar = $ck
    ServicoDbUrl = 'jdbc:postgresql://localhost:5432/biblioteca'
    ServicoDbUsuario = 'postgres'
    ServicoDbSenha = 'postgres'
}
$parametrosPre = $comum.Clone()
$parametrosPre.Estado = 'pre-migracao'
$parametrosPre.ColetaId = 'manual-final-pre-01'
$parametrosPre.Repositorio = $repoPre
$parametrosPos = $comum.Clone()
$parametrosPos.Estado = 'pos-migracao'
$parametrosPos.ColetaId = 'manual-final-pos-01'
$parametrosPos.Repositorio = $repoPos
$evidenciaPre = Join-Path $repoPrincipal 'validacao\validacao-funcional-pre-aa55edd.json'
$evidenciaPos = Join-Path $repoPrincipal 'validacao\validacao-funcional-pos-45ff20b.json'
```

Primeiro prepare a base anterior pelo item 4 e execute seu piloto:

```powershell
$pilotoPre = $parametrosPre.Clone()
$pilotoPre.ColetaId = 'manual-piloto-pre-01'
foreach ($etapa in @('registrar','build','inicializacao','cbo','conferir')) {
    & $coletor @pilotoPre -Etapa $etapa -Piloto -EvidenciaFuncional $evidenciaPre
}
```

Depois recrie a base posterior pelo item 4 e execute seu piloto:

```powershell
$pilotoPos = $parametrosPos.Clone()
$pilotoPos.ColetaId = 'manual-piloto-pos-01'
foreach ($etapa in @('registrar','build','inicializacao','cbo','conferir')) {
    & $coletor @pilotoPos -Etapa $etapa -Piloto -EvidenciaFuncional $evidenciaPos
}
```

`-Piloto` deve acompanhar todas as etapas. Ele faz uma repetição por série e
recebe manifesto próprio, fora da análise final. Se falhar, investigue e escolha
outro identificador antes de repetir.

## 6. Executar as duas campanhas finais

Recrie novamente a base anterior pelo item 4 e execute:

```powershell
& $coletor @parametrosPre -Etapa registrar -EvidenciaFuncional $evidenciaPre
& $coletor @parametrosPre -Etapa build
& $coletor @parametrosPre -Etapa inicializacao
& $coletor @parametrosPre -Etapa cbo
& $coletor @parametrosPre -Etapa conferir
```

Recrie a base posterior pelo item 4 e execute:

```powershell
& $coletor @parametrosPos -Etapa registrar -EvidenciaFuncional $evidenciaPos
& $coletor @parametrosPos -Etapa build
& $coletor @parametrosPos -Etapa inicializacao
& $coletor @parametrosPos -Etapa cbo
& $coletor @parametrosPos -Etapa conferir
```

Cada série temporal final tem dez repetições. Mantenha os mesmos parâmetros e
instrumentos entre etapas. A evidência funcional vincula a campanha aos
documentos históricos dos commits medidos; fornecê-la não reexecuta os 41 casos
manuais.

## 7. Executar os três builds suplementares

Faça isto **depois de todas as inicializações principais**: `clean package`
substitui os artefatos de `target`. Use os mesmos snapshots pré/pós e os
instrumentos fixos da `main`.

```powershell
& $semTestesMonolito -Estado pre-migracao -ColetaId manual-sem-testes-pre-01 -Repositorio $repoPre -ResultadosRaiz $resultados -JavaLegado $java8 -Maven $maven
& $semTestesMonolito -Estado pos-migracao -ColetaId manual-sem-testes-pos-01 -Repositorio $repoPos -ResultadosRaiz $resultados -JavaLegado $java8 -Maven $maven
& $semTestesServico -ColetaId manual-sem-testes-pos-01 -Repositorio $repoPos -ResultadosRaiz $resultados -JavaServico $java21 -Maven $maven
```

O comando medido é `mvn -B -o clean package -Dmaven.test.skip=true`:
não compila nem executa testes. São dez observações por chamada. Não foi medido
um conjunto sem testes; eventual soma das médias isoladas é uma estimativa.

Ao terminar, pare somente o container descartável criado neste procedimento:

```powershell
docker stop $idDbAuditoria
```

O container e seu volume ficam preservados. Reinicie seu container habitual,
caso tenha sido parado, pelo identificador que você anotou. Preserve os
resultados e a identificação das ferramentas de cada nova campanha.

## O que cada medição representa

- **Build com testes:** `mvn -B -o clean package`. No conjunto, o cronômetro
  cobre monólito seguido do serviço e orquestração. As suítes diferem: 33 testes
  no anterior, 32 no monólito posterior com três ignorados (29 executados) e
  165 no serviço.
- **Inicialização HTTP:** processos novos com artefatos já construídos.
  O JBoss recebe base temporária e WAR antes do cronômetro. No conjunto,
  serviço e JBoss iniciam sem aguardar uma resposta de saúde intermediária.
  O tempo termina quando `/monolito-biblioteca/health` e `/q/health/live`
  respondem HTTP 200 na mesma rodada. Intervalo solicitado: 100 ms; timeout:
  500 ms por consulta; limite padrão: 120 segundos. A medida não é prontidão
  funcional completa. O coletor encerra as árvores de processos que iniciou.
- **CBO:** CK sobre `src/main/java`, sem testes, com
  `java -jar <CK> <fontes> false 0 true`. Há uma análise por projeto; o
  conjunto posterior reúne suas distribuições, sem quarta execução CK.

Os scripts registram commit, hashes, ambiente, comandos e saídas. Preparação das
bases e encerramento ficam fora do tempo de inicialização. Dez repetições
locais descrevem a variabilidade deste ambiente. Arquitetura, runtime, framework,
código e testes mudam simultaneamente; o estudo não isola uma única causa.

## Histórico

Os dados anteriores de 20/10 continuam no commit
[`0d83263d38860c5b271f10a3f668c7b78fba497a`](https://github.com/Rhuan000/tcc-legado/tree/0d83263d38860c5b271f10a3f668c7b78fba497a).
Não misture as séries antigas com a seleção de 23/10.
