[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [ValidateSet('pre-migracao', 'intermediario', 'pos-migracao')]
    [string]$Estado
)

$ErrorActionPreference = 'Stop'
$repo = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$saida = Join-Path $repo "metricas\$Estado\metadados-estado.csv"

$arquivos = @(
    git -C $repo ls-files -- 'monolito-biblioteca/pom.xml' 'monolito-biblioteca/src/**' |
        Sort-Object
)
if ($arquivos.Count -eq 0) {
    throw 'Nenhum arquivo versionado do monolito foi encontrado.'
}

$linhasHash = foreach ($arquivo in $arquivos) {
    $caminhoCompleto = Join-Path $repo $arquivo
    $hashArquivo = (Get-FileHash -LiteralPath $caminhoCompleto -Algorithm SHA256).Hash.ToLowerInvariant()
    "$($arquivo.Replace('\', '/'))=$hashArquivo"
}
$bytes = [System.Text.Encoding]::UTF8.GetBytes(($linhasHash -join "`n"))
$sha = [System.Security.Cryptography.SHA256]::Create()
try {
    $codigoSha256 = ([BitConverter]::ToString($sha.ComputeHash($bytes))).Replace('-', '').ToLowerInvariant()
}
finally {
    $sha.Dispose()
}

$statusEscopo = @(git -C $repo status --short -- 'monolito-biblioteca/pom.xml' 'monolito-biblioteca/src/**')
$diretorioSaida = Split-Path $saida
New-Item -ItemType Directory -Force -Path $diretorioSaida | Out-Null

[pscustomobject]@{
    estado = $Estado
    registro_iso = (Get-Date).ToString('o')
    commit_base = (& git -C $repo rev-parse HEAD).Trim()
    codigo_sha256 = $codigoSha256
    arquivos_no_hash = $arquivos.Count
    arvore_limpa_no_escopo = ($statusEscopo.Count -eq 0)
    alteracoes_no_escopo = ($statusEscopo -join '; ')
} | Export-Csv -LiteralPath $saida -NoTypeInformation -Encoding UTF8

Import-Csv -LiteralPath $saida
