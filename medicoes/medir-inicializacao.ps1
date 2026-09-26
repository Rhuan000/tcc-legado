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
    [string]$JbossHome = 'C:\desenvolvimento\jboss-eap-7.4',
    [int]$PortaHttp = 18080,
    [int]$PortaGerenciamento = 19990,
    [int]$IntervaloConsultaMs = 100,
    [int]$TimeoutSegundos = 60
)

$ErrorActionPreference = 'Stop'

function Testar-PortaTcp {
    param([int]$Porta)
    $cliente = New-Object System.Net.Sockets.TcpClient
    try {
        $async = $cliente.BeginConnect('127.0.0.1', $Porta, $null, $null)
        if (-not $async.AsyncWaitHandle.WaitOne(300)) {
            return $false
        }
        $cliente.EndConnect($async)
        return $true
    }
    catch {
        return $false
    }
    finally {
        $cliente.Dispose()
    }
}

function Obter-StatusHttp {
    param([string]$Url)
    try {
        $requisicao = [System.Net.HttpWebRequest]::Create($Url)
        $requisicao.AllowAutoRedirect = $false
        $requisicao.Timeout = 500
        $requisicao.ReadWriteTimeout = 500
        $requisicao.Method = 'GET'
        $resposta = [System.Net.HttpWebResponse]$requisicao.GetResponse()
        try {
            return [int]$resposta.StatusCode
        }
        finally {
            $resposta.Dispose()
        }
    }
    catch [System.Net.WebException] {
        if ($_.Exception.Response) {
            $respostaErro = [System.Net.HttpWebResponse]$_.Exception.Response
            try {
                return [int]$respostaErro.StatusCode
            }
            finally {
                $respostaErro.Dispose()
            }
        }
        return 0
    }
}

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
$war = Join-Path $repo 'monolito-biblioteca\target\monolito-biblioteca.war'
$standalone = Join-Path $JbossHome 'bin\standalone.bat'
$cli = Join-Path $JbossHome 'bin\jboss-cli.bat'
$configuracao = Join-Path $JbossHome 'standalone\configuration'
$saida = Join-Path $raizResultados "$Estado\$ColetaId\inicializacao"
$logs = Join-Path $saida 'logs'
$csv = Join-Path $saida 'medicoes-inicializacao.csv'
$urlSaude = "http://127.0.0.1:$PortaHttp/monolito-biblioteca/health"
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
foreach ($arquivoObrigatorio in @((Join-Path $JavaHome 'bin\java.exe'), $war, $standalone, $cli, (Join-Path $configuracao 'standalone.xml'))) {
    if (-not (Test-Path -LiteralPath $arquivoObrigatorio)) {
        throw "Arquivo obrigatorio nao encontrado: $arquivoObrigatorio"
    }
}
if (Testar-PortaTcp $PortaHttp) {
    throw "A porta HTTP $PortaHttp ja esta em uso."
}
if (Testar-PortaTcp $PortaGerenciamento) {
    throw "A porta de gerenciamento $PortaGerenciamento ja esta em uso."
}

New-Item -ItemType Directory -Force -Path $logs | Out-Null

$javaHomeAnterior = $env:JAVA_HOME
$pathAnterior = $env:Path
$noPauseAnterior = $env:NOPAUSE
$resultados = @()

try {
    $env:JAVA_HOME = $JavaHome
    $env:Path = "$(Join-Path $JavaHome 'bin');$pathAnterior"
    $env:NOPAUSE = '1'
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
    $warSha256 = (Get-FileHash -LiteralPath $war -Algorithm SHA256).Hash
    $preferenciaErroAnterior = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    $javaVersion = (& (Join-Path $JavaHome 'bin\java.exe') -version 2>&1 | Select-Object -First 1).ToString()
    $ErrorActionPreference = $preferenciaErroAnterior

    for ($i = 1; $i -le $Repeticoes; $i++) {
        $base = Join-Path $env:TEMP ("tcc-jboss-medicao-{0}-{1}" -f $Estado, [guid]::NewGuid().ToString('N'))
        $configBase = Join-Path $base 'configuration'
        $deployBase = Join-Path $base 'deployments'
        New-Item -ItemType Directory -Path $configBase, $deployBase | Out-Null
        Copy-Item -Path (Join-Path $configuracao '*') -Destination $configBase -Recurse -Force
        Copy-Item -LiteralPath $war -Destination (Join-Path $deployBase 'monolito-biblioteca.war')

        $stdout = Join-Path $logs ("inicializacao-{0:D2}.stdout.log" -f $i)
        $stderr = Join-Path $logs ("inicializacao-{0:D2}.stderr.log" -f $i)
        $inicio = Get-Date
        $processo = $null
        $statusHttp = 0
        $pronto = $false

        try {
            $argumentos = @(
                "-Djboss.server.base.dir=$base",
                "-Djboss.http.port=$PortaHttp",
                "-Djboss.management.http.port=$PortaGerenciamento"
            )
            $cronometro = [System.Diagnostics.Stopwatch]::StartNew()
            $processo = Start-Process -FilePath $standalone `
                -ArgumentList $argumentos `
                -RedirectStandardOutput $stdout `
                -RedirectStandardError $stderr `
                -WindowStyle Hidden `
                -PassThru

            while ($cronometro.Elapsed.TotalSeconds -lt $TimeoutSegundos) {
                $statusHttp = Obter-StatusHttp $urlSaude
                if ($statusHttp -eq 200) {
                    $pronto = $true
                    break
                }
                if ($processo.HasExited) {
                    break
                }
                Start-Sleep -Milliseconds $IntervaloConsultaMs
            }
            $cronometro.Stop()

            $resultado = [pscustomobject]@{
                estado = $Estado
                coleta_id = $ColetaId
                repeticao = $i
                inicio_iso = $inicio.ToString('o')
                duracao_ms = [math]::Round($cronometro.Elapsed.TotalMilliseconds, 3)
                duracao_s = [math]::Round($cronometro.Elapsed.TotalSeconds, 6)
                status_http = $statusHttp
                sucesso = $pronto
                processo_codigo_saida = if ($processo.HasExited) { $processo.ExitCode } else { $null }
                commit = $commit
                coletor_sha256 = $coletorSha256
                war_sha256 = $warSha256
                java = $javaVersion
                jboss = '7.4.0.GA'
                jboss_home = $JbossHome
                endpoint = $urlSaude
                intervalo_consulta_ms = $IntervaloConsultaMs
                timeout_segundos = $TimeoutSegundos
            }
            $resultados += $resultado
            $resultados | Export-Csv -LiteralPath $csv -NoTypeInformation -Encoding UTF8

            if (-not $pronto) {
                throw "Inicializacao $i nao retornou HTTP 200 em $TimeoutSegundos segundos. Ultimo status: $statusHttp. Base preservada em $base"
            }
        }
        finally {
            if ($processo -and -not $processo.HasExited) {
                $cliStdout = Join-Path $logs ("shutdown-{0:D2}.stdout.log" -f $i)
                $cliStderr = Join-Path $logs ("shutdown-{0:D2}.stderr.log" -f $i)
                $processoCli = Start-Process -FilePath $cli `
                    -ArgumentList @('--connect', "--controller=127.0.0.1:$PortaGerenciamento", '--command=:shutdown') `
                    -RedirectStandardOutput $cliStdout `
                    -RedirectStandardError $cliStderr `
                    -WindowStyle Hidden `
                    -Wait `
                    -PassThru
                $processo.WaitForExit(30000) | Out-Null
                if (-not $processo.HasExited) {
                    & taskkill.exe /PID $processo.Id /T /F | Out-Null
                    $processo.WaitForExit(10000) | Out-Null
                }
            }

            if ($pronto -and (Test-Path -LiteralPath $base)) {
                $baseResolvida = (Resolve-Path -LiteralPath $base).Path
                $tempResolvido = (Resolve-Path -LiteralPath $env:TEMP).Path
                if (-not $baseResolvida.StartsWith($tempResolvido, [System.StringComparison]::OrdinalIgnoreCase) -or
                    -not (Split-Path $baseResolvida -Leaf).StartsWith('tcc-jboss-medicao-')) {
                    throw "Recusa de limpeza: diretorio temporario inesperado $baseResolvida"
                }
                Remove-Item -LiteralPath $baseResolvida -Recurse -Force
            }
        }
    }
}
finally {
    $env:JAVA_HOME = $javaHomeAnterior
    $env:Path = $pathAnterior
    $env:NOPAUSE = $noPauseAnterior
}

$resultados
