[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)][ValidateSet('pre-migracao', 'pos-migracao')][string]$Estado,
    [Parameter(Mandatory = $true)][ValidatePattern('^[a-z0-9][a-z0-9._-]{0,63}$')][string]$ColetaId,
    [Parameter(Mandatory = $true)][string]$Repositorio,
    [string]$ResultadosRaiz = (Join-Path $PSScriptRoot '..\metricas'),
    [ValidateRange(1, 100)][int]$Repeticoes = 10,
    [string]$JavaLegado = 'C:\Program Files (x86)\Java\jdk1.8.0_201',
    [string]$Maven = 'C:\Program Files\Maven\apache-maven-3.9.11-bin\apache-maven-3.9.11\bin\mvn.cmd'
)

$ErrorActionPreference = 'Stop'
$repo = (Resolve-Path -LiteralPath $Repositorio).Path
$projeto = Join-Path $repo 'monolito-biblioteca'
$saida = Join-Path ([IO.Path]::GetFullPath($ResultadosRaiz)) "$Estado\$ColetaId\monolito\build-sem-testes"
$escopoGit = @('monolito-biblioteca/pom.xml', 'monolito-biblioteca/src')
$status = @(& git -C $repo status --porcelain --untracked-files=all -- $escopoGit)
if ($LASTEXITCODE -ne 0) { throw 'Falha ao consultar o repositorio.' }
if ($status.Count -gt 0) { throw "Codigo medido deve estar limpo: $($status -join '; ')" }
if (Test-Path -LiteralPath $saida) { throw "Saida ja existe, sem sobrescrita: $saida" }
New-Item -ItemType Directory -Path $saida | Out-Null

$commit = @(& git -C $repo rev-parse HEAD) -join ''
$arvoreEscopo = @(& git -C $repo rev-parse 'HEAD:monolito-biblioteca') -join ''
$hashColetor = (Get-FileHash -LiteralPath $PSCommandPath -Algorithm SHA256).Hash
$pathAnterior = $env:Path
$javaAnterior = $env:JAVA_HOME
$jdkOptionsAnterior = $env:JDK_JAVA_OPTIONS
$resultados = @()

function Obter-Versao {
    param([string]$Executavel, [string]$Argumento)
    $anterior = $ErrorActionPreference
    try {
        $ErrorActionPreference = 'Continue'
        return ((@(& $Executavel $Argumento 2>&1) | ForEach-Object { $_.ToString() }) -join "`n")
    } finally { $ErrorActionPreference = $anterior }
}

try {
    $env:JAVA_HOME = $JavaLegado
    $env:Path = "$JavaLegado\bin;$pathAnterior"
    $env:JDK_JAVA_OPTIONS = $null
    for ($repeticao = 1; $repeticao -le $Repeticoes; $repeticao++) {
        $prefixo = Join-Path $saida ("monolito-{0:D2}" -f $repeticao)
        $inicio = (Get-Date).ToString('o')
        $cronometro = [Diagnostics.Stopwatch]::StartNew()
        $processo = Start-Process -FilePath $Maven -ArgumentList @('-B', '-o', 'clean', 'package', '-Dmaven.test.skip=true') -WorkingDirectory $projeto -RedirectStandardOutput "$prefixo.stdout.log" -RedirectStandardError "$prefixo.stderr.log" -WindowStyle Hidden -PassThru
        $processo.Handle | Out-Null
        if (-not $processo.WaitForExit(1200000)) {
            & taskkill.exe /PID $processo.Id /T /F | Out-Null
            throw "Timeout no build sem testes, repeticao $repeticao."
        }
        $processo.WaitForExit()
        $cronometro.Stop()
        $resultados += [pscustomobject]@{
            estado = $Estado
            coleta_id = $ColetaId
            escopo = 'monolito'
            variante = 'sem-compilacao-ou-execucao-de-testes'
            repeticao = $repeticao
            inicio_iso = $inicio
            duracao_ms = $cronometro.Elapsed.TotalMilliseconds
            sucesso = ($processo.ExitCode -eq 0)
            codigo_saida = $processo.ExitCode
            commit = $commit
            arvore_escopo = $arvoreEscopo
            coletor_sha256 = $hashColetor
        }
        $resultados | Export-Csv -LiteralPath (Join-Path $saida 'medicoes.csv') -NoTypeInformation -Encoding UTF8
        if ($processo.ExitCode -ne 0) { throw "Build sem testes falhou na repeticao $repeticao." }
    }
    $artefato = Join-Path $projeto 'target\monolito-biblioteca.war'
    [pscustomobject]@{
        protocolo = 'suplementar-build-sem-testes-v1'
        estado = $Estado
        coleta_id = $ColetaId
        repeticoes = $Repeticoes
        commit = $commit
        arvore_escopo = $arvoreEscopo
        coletor_sha256 = $hashColetor
        java = @((Obter-Versao "$JavaLegado\bin\java.exe" '-version').Split("`n") | Where-Object { $_ -match '^(java|openjdk) version' })[0]
        maven = @((Obter-Versao $Maven '-version').Split("`n") | Where-Object { $_ -match '^Apache Maven ' })[0]
        comando = 'mvn -B -o clean package -Dmaven.test.skip=true'
        sem_testes = 'nao compila nem executa fontes de teste'
        artefato_sha256 = (Get-FileHash -LiteralPath $artefato -Algorithm SHA256).Hash
    } | ConvertTo-Json -Depth 4 | Set-Content -LiteralPath (Join-Path $saida 'manifesto.json') -Encoding UTF8
} finally {
    $env:JAVA_HOME = $javaAnterior
    $env:Path = $pathAnterior
    $env:JDK_JAVA_OPTIONS = $jdkOptionsAnterior
}

Write-Output "Coleta suplementar concluida: $saida"
