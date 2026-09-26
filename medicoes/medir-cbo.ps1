[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [ValidateSet('pre-migracao', 'pos-migracao')]
    [string]$Estado,

    [Parameter(Mandatory = $true)]
    [ValidatePattern('^[a-z0-9][a-z0-9._-]{0,63}$')]
    [string]$ColetaId,

    [string]$Repositorio,
    [string]$ResultadosRaiz,

    [string]$JavaHome = 'C:\Program Files\Java\jdk-21',
    [string]$CkJar,

    [string]$CkSha256Esperado = '2F32FA347EF739E97152741828675A9939AEAFE61BEE3F196EE5AF264E612162'
)

$ErrorActionPreference = 'Stop'

$repo = if ([string]::IsNullOrWhiteSpace($Repositorio)) {
    (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
} else {
    (Resolve-Path -LiteralPath $Repositorio).Path
}
$raizResultados = if ([string]::IsNullOrWhiteSpace($ResultadosRaiz)) {
    Join-Path $repo 'metricas'
} else {
    [System.IO.Path]::GetFullPath($ResultadosRaiz)
}
$fontes = (Resolve-Path (Join-Path $repo 'monolito-biblioteca\src\main\java')).Path
$ckJar = if ([string]::IsNullOrWhiteSpace($CkJar)) {
    (Resolve-Path (Join-Path $PSScriptRoot '..\metricas\ck.jar')).Path
} else {
    (Resolve-Path -LiteralPath $CkJar).Path
}
$saida = Join-Path $raizResultados "$Estado\$ColetaId\cbo"
$metadados = Join-Path $saida 'metadados-cbo.csv'
$registroEstado = Join-Path $raizResultados "$Estado\$ColetaId\metadados-estado.csv"

if (-not (Test-Path -LiteralPath (Join-Path $JavaHome 'bin\java.exe'))) {
    throw "Java do analisador nao encontrado em $JavaHome"
}

$statusEscopo = @(git -C $repo status --short --untracked-files=all -- 'monolito-biblioteca/pom.xml' 'monolito-biblioteca/src/**')
if ($LASTEXITCODE -ne 0) {
    throw "Nao foi possivel consultar o estado Git de $repo."
}
if ($statusEscopo.Count -gt 0) {
    throw "A coleta exige arvore limpa no escopo do monolito: $($statusEscopo -join '; ')"
}
if (Test-Path -LiteralPath $saida) {
    throw "A saida $saida ja existe. Use outro ColetaId; resultados nunca sao sobrescritos."
}
if (-not (Test-Path -LiteralPath $registroEstado)) {
    throw "Metadados da campanha nao encontrados em $registroEstado. Execute registrar-estado.ps1 primeiro."
}
$javaExe = Join-Path $JavaHome 'bin\java.exe'
$commit = (& git -C $repo rev-parse HEAD).Trim()
if ($LASTEXITCODE -ne 0) {
    throw "Nao foi possivel identificar o commit de $repo."
}
$estadoRegistrado = Import-Csv -LiteralPath $registroEstado
if ($estadoRegistrado.estado -ne $Estado -or
    $estadoRegistrado.coleta_id -ne $ColetaId -or
    $estadoRegistrado.commit_base -ne $commit) {
    throw 'Os metadados da campanha nao correspondem ao estado, ao ColetaId e ao commit atuais.'
}
$preferenciaErroAnterior = $ErrorActionPreference
$ErrorActionPreference = 'Continue'
$javaVersion = (& $javaExe -version 2>&1 | Select-Object -First 1).ToString()
$ErrorActionPreference = $preferenciaErroAnterior
$hash = (Get-FileHash -LiteralPath $ckJar -Algorithm SHA256).Hash
if ($hash -ne $CkSha256Esperado) {
    throw "SHA-256 inesperado para o CK. Esperado: $CkSha256Esperado; obtido: $hash"
}
$inicio = Get-Date
New-Item -ItemType Directory -Path $saida | Out-Null

Push-Location $saida
try {
    & $javaExe -jar $ckJar $fontes false 0 true
    if ($LASTEXITCODE -ne 0) {
        throw "CK encerrou com codigo $LASTEXITCODE"
    }
}
finally {
    Pop-Location
}

foreach ($nome in @('class.csv', 'method.csv', 'field.csv', 'variable.csv')) {
    if (-not (Test-Path -LiteralPath (Join-Path $saida $nome))) {
        throw "CK nao produziu o arquivo esperado: $nome"
    }
}

[pscustomobject]@{
    estado = $Estado
    coleta_id = $ColetaId
    coleta_iso = $inicio.ToString('o')
    repeticoes = 1
    commit = $commit
    coletor_sha256 = (Get-FileHash -LiteralPath $PSCommandPath -Algorithm SHA256).Hash
    ferramenta = 'CK 0.7.1-SNAPSHOT'
    ck_sha256 = $hash
    java_analisador = $javaVersion
    diretorio_analisado = $fontes
    comando = 'java -jar ck.jar <fontes> false 0 true'
} | Export-Csv -LiteralPath $metadados -NoTypeInformation -Encoding UTF8

Import-Csv -LiteralPath (Join-Path $saida 'class.csv') |
    Select-Object class, type, cbo, cboModified, fanin, fanout, wmc, loc
