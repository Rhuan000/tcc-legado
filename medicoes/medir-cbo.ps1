[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [ValidateSet('pre-migracao', 'intermediario', 'pos-migracao')]
    [string]$Estado,

    [string]$JavaHome = 'C:\Program Files\Java\jdk-21',
    [switch]$Force
)

$ErrorActionPreference = 'Stop'

$repo = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$fontes = (Resolve-Path (Join-Path $repo 'monolito-biblioteca\src\main\java')).Path
$ckJar = (Resolve-Path (Join-Path $repo 'metricas\ck.jar')).Path
$saida = Join-Path $repo "metricas\$Estado\cbo"
$metadados = Join-Path $saida 'metadados-cbo.csv'

if (-not (Test-Path -LiteralPath (Join-Path $JavaHome 'bin\java.exe'))) {
    throw "Java do analisador nao encontrado em $JavaHome"
}

if (Test-Path -LiteralPath $saida) {
    $arquivosExistentes = @(Get-ChildItem -LiteralPath $saida -Force)
    if ($arquivosExistentes.Count -gt 0 -and -not $Force) {
        throw "O diretorio $saida ja contem resultados. Use -Force somente para uma nova coleta consciente."
    }
}
New-Item -ItemType Directory -Force -Path $saida | Out-Null
if ($Force) {
    Get-ChildItem -LiteralPath $saida -File | Remove-Item -Force
}

$javaExe = Join-Path $JavaHome 'bin\java.exe'
$commit = (& git -C $repo rev-parse HEAD).Trim()
$preferenciaErroAnterior = $ErrorActionPreference
$ErrorActionPreference = 'Continue'
$javaVersion = (& $javaExe -version 2>&1 | Select-Object -First 1).ToString()
$ErrorActionPreference = $preferenciaErroAnterior
$hash = (Get-FileHash -LiteralPath $ckJar -Algorithm SHA256).Hash
$inicio = Get-Date

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
    coleta_iso = $inicio.ToString('o')
    repeticoes = 1
    commit = $commit
    ferramenta = 'CK 0.7.1-SNAPSHOT'
    ck_sha256 = $hash
    java_analisador = $javaVersion
    diretorio_analisado = $fontes
    comando = 'java -jar ck.jar <fontes> false 0 true'
} | Export-Csv -LiteralPath $metadados -NoTypeInformation -Encoding UTF8

Import-Csv -LiteralPath (Join-Path $saida 'class.csv') |
    Select-Object class, type, cbo, cboModified, fanin, fanout, wmc, loc
