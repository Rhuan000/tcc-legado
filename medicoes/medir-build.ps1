[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [ValidateSet('pre-migracao', 'pos-migracao')]
    [string]$Estado,

    [ValidateRange(1, 100)]
    [int]$Repeticoes = 10,

    [Parameter(Mandatory = $true)]
    [ValidatePattern('^[a-z0-9][a-z0-9._-]{0,63}$')]
    [string]$ColetaId,

    [string]$Repositorio,
    [string]$ResultadosRaiz,

    [string]$JavaHome = 'C:\Program Files (x86)\Java\jdk1.8.0_201',
    [string]$MavenExecutable = 'C:\Program Files\Maven\apache-maven-3.9.11-bin\apache-maven-3.9.11\bin\mvn.cmd'
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
$projeto = (Resolve-Path (Join-Path $repo 'monolito-biblioteca')).Path
$saida = Join-Path $raizResultados "$Estado\$ColetaId\build"
$logs = Join-Path $saida 'logs'
$csv = Join-Path $saida 'medicoes-build.csv'
$registroEstado = Join-Path $raizResultados "$Estado\$ColetaId\metadados-estado.csv"

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
if (-not (Test-Path -LiteralPath (Join-Path $JavaHome 'bin\java.exe'))) {
    throw "Java nao encontrado em $JavaHome"
}
if (-not (Test-Path -LiteralPath $MavenExecutable)) {
    throw "Maven nao encontrado em $MavenExecutable"
}

New-Item -ItemType Directory -Force -Path $logs | Out-Null

$javaHomeAnterior = $env:JAVA_HOME
$pathAnterior = $env:Path
$resultados = @()

try {
    $env:JAVA_HOME = $JavaHome
    $env:Path = "$(Join-Path $JavaHome 'bin');$(Split-Path $MavenExecutable);$pathAnterior"

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
    $coletorSha256 = (Get-FileHash -LiteralPath $PSCommandPath -Algorithm SHA256).Hash
    $preferenciaErroAnterior = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    $javaVersion = (& (Join-Path $JavaHome 'bin\java.exe') -version 2>&1 | Select-Object -First 1).ToString()
    $ErrorActionPreference = $preferenciaErroAnterior
    $mavenVersion = (& $MavenExecutable -version 2>&1 | Select-Object -First 1).ToString()

    for ($i = 1; $i -le $Repeticoes; $i++) {
        $stdout = Join-Path $logs ("build-{0:D2}.stdout.log" -f $i)
        $stderr = Join-Path $logs ("build-{0:D2}.stderr.log" -f $i)
        $inicio = Get-Date
        $cronometro = [System.Diagnostics.Stopwatch]::StartNew()
        $processo = Start-Process -FilePath $MavenExecutable `
            -ArgumentList @('clean', 'package') `
            -WorkingDirectory $projeto `
            -RedirectStandardOutput $stdout `
            -RedirectStandardError $stderr `
            -WindowStyle Hidden `
            -Wait `
            -PassThru
        $cronometro.Stop()

        $resultado = [pscustomobject]@{
            estado = $Estado
            coleta_id = $ColetaId
            repeticao = $i
            inicio_iso = $inicio.ToString('o')
            duracao_ms = [math]::Round($cronometro.Elapsed.TotalMilliseconds, 3)
            duracao_s = [math]::Round($cronometro.Elapsed.TotalSeconds, 6)
            codigo_saida = $processo.ExitCode
            sucesso = ($processo.ExitCode -eq 0)
            commit = $commit
            coletor_sha256 = $coletorSha256
            java = $javaVersion
            maven = $mavenVersion
            maven_executavel = $MavenExecutable
            comando = 'mvn clean package'
        }
        $resultados += $resultado
        $resultados | Export-Csv -LiteralPath $csv -NoTypeInformation -Encoding UTF8

        if (-not $resultado.sucesso) {
            throw "Build $i falhou com codigo $($processo.ExitCode). Consulte $stderr"
        }
    }
}
finally {
    $env:JAVA_HOME = $javaHomeAnterior
    $env:Path = $pathAnterior
}

$resultados
