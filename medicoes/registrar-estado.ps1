[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [ValidateSet('pre-migracao', 'pos-migracao')]
    [string]$Estado,

    [Parameter(Mandatory = $true)]
    [ValidatePattern('^[a-z0-9][a-z0-9._-]{0,63}$')]
    [string]$ColetaId,

    [string]$Repositorio,
    [string]$ResultadosRaiz
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
$diretorioCampanha = Join-Path $raizResultados "$Estado\$ColetaId"
$saida = Join-Path $diretorioCampanha 'metadados-estado.csv'

if (Test-Path -LiteralPath $diretorioCampanha) {
    throw "A campanha $diretorioCampanha ja existe. Use outro ColetaId; resultados nunca sao sobrescritos."
}

$statusEscopo = @(git -C $repo status --short --untracked-files=all -- 'monolito-biblioteca/pom.xml' 'monolito-biblioteca/src/**')
if ($LASTEXITCODE -ne 0) {
    throw "Nao foi possivel consultar o estado Git de $repo."
}
if ($statusEscopo.Count -gt 0) {
    throw "O registro exige arvore limpa no escopo do monolito: $($statusEscopo -join '; ')"
}

$commit = (& git -C $repo rev-parse HEAD).Trim()
if ($LASTEXITCODE -ne 0) {
    throw "Nao foi possivel identificar o commit de $repo."
}

$arquivos = @(
    git -C $repo ls-files -- 'monolito-biblioteca/pom.xml' 'monolito-biblioteca/src/**' |
        Sort-Object
)
if ($LASTEXITCODE -ne 0) {
    throw "Nao foi possivel enumerar os arquivos versionados de $repo."
}
if ($arquivos.Count -eq 0) {
    throw 'Nenhum arquivo versionado do monolito foi encontrado.'
}

$linhasHash = foreach ($arquivo in $arquivos) {
    $caminhoCompleto = Join-Path $repo $arquivo
    $hashArquivo = (Get-FileHash -LiteralPath $caminhoCompleto -Algorithm SHA256).Hash.ToLowerInvariant()
    "$($arquivo.Replace('\', '/'))=$hashArquivo"
}
$linhasHashGit = foreach ($arquivo in $arquivos) {
    $blob = (& git -C $repo rev-parse "HEAD:$arquivo").Trim()
    "$($arquivo.Replace('\', '/'))=$blob"
}
$bytes = [System.Text.Encoding]::UTF8.GetBytes(($linhasHash -join "`n"))
$bytesGit = [System.Text.Encoding]::UTF8.GetBytes(($linhasHashGit -join "`n"))
$sha = [System.Security.Cryptography.SHA256]::Create()
try {
    $codigoSha256 = ([BitConverter]::ToString($sha.ComputeHash($bytes))).Replace('-', '').ToLowerInvariant()
    $codigoGitSha256 = ([BitConverter]::ToString($sha.ComputeHash($bytesGit))).Replace('-', '').ToLowerInvariant()
}
finally {
    $sha.Dispose()
}

New-Item -ItemType Directory -Path $diretorioCampanha | Out-Null

$sistemaOperacional = [System.Environment]::OSVersion.VersionString
$processador = $env:PROCESSOR_IDENTIFIER
$memoriaFisicaBytes = $null
try {
    $sistema = Get-CimInstance -ClassName Win32_OperatingSystem -ErrorAction Stop
    $sistemaOperacional = $sistema.Caption + ' ' + $sistema.Version
    $memoriaFisicaBytes = [int64]$sistema.TotalVisibleMemorySize * 1KB
    $processador = (Get-CimInstance -ClassName Win32_Processor -ErrorAction Stop |
        Select-Object -First 1 -ExpandProperty Name).Trim()
}
catch {
    # Os identificadores portaveis permanecem registrados quando o CIM nao esta disponivel.
}

[pscustomobject]@{
    estado = $Estado
    coleta_id = $ColetaId
    registro_iso = (Get-Date).ToString('o')
    commit_base = $commit
    arvore_git_monolito = (& git -C $repo rev-parse 'HEAD:monolito-biblioteca').Trim()
    codigo_git_sha256 = $codigoGitSha256
    codigo_sha256 = $codigoSha256
    arquivos_no_hash = $arquivos.Count
    arvore_limpa_no_escopo = $true
    core_autocrlf = (& git -C $repo config --get core.autocrlf)
    sistema_operacional = $sistemaOperacional
    arquitetura_processo = [System.Runtime.InteropServices.RuntimeInformation]::ProcessArchitecture.ToString()
    processador = $processador
    memoria_fisica_bytes = $memoriaFisicaBytes
    powershell = $PSVersionTable.PSVersion.ToString()
    coletor_sha256 = (Get-FileHash -LiteralPath $PSCommandPath -Algorithm SHA256).Hash
} | Export-Csv -LiteralPath $saida -NoTypeInformation -Encoding UTF8

Import-Csv -LiteralPath $saida
