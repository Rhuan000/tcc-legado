[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [ValidateSet('pre-migracao', 'intermediario', 'pos-migracao')]
    [string]$Estado,

    [ValidateRange(1, 100)]
    [int]$Repeticoes = 10,

    [string]$JavaHome = 'C:\Program Files (x86)\Java\jdk1.8.0_201',
    [string]$MavenExecutable = 'C:\Program Files\Maven\apache-maven-3.9.11-bin\apache-maven-3.9.11\bin\mvn.cmd'
)

$ErrorActionPreference = 'Stop'

$repo = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$projeto = (Resolve-Path (Join-Path $repo 'monolito-biblioteca')).Path
$saida = Join-Path $repo "metricas\$Estado\build"
$logs = Join-Path $saida 'logs'
$csv = Join-Path $saida 'medicoes-build.csv'

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
            repeticao = $i
            inicio_iso = $inicio.ToString('o')
            duracao_ms = [math]::Round($cronometro.Elapsed.TotalMilliseconds, 3)
            duracao_s = [math]::Round($cronometro.Elapsed.TotalSeconds, 6)
            codigo_saida = $processo.ExitCode
            sucesso = ($processo.ExitCode -eq 0)
            commit = $commit
            java = $javaVersion
            maven = $mavenVersion
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
