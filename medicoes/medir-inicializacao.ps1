[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [ValidateSet('pre-migracao', 'intermediario', 'pos-migracao')]
    [string]$Estado,

    [ValidateRange(1, 100)]
    [int]$Repeticoes = 10,

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

$repo = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$war = Join-Path $repo 'monolito-biblioteca\target\monolito-biblioteca.war'
$standalone = Join-Path $JbossHome 'bin\standalone.bat'
$cli = Join-Path $JbossHome 'bin\jboss-cli.bat'
$configuracao = Join-Path $JbossHome 'standalone\configuration'
$saida = Join-Path $repo "metricas\$Estado\inicializacao"
$logs = Join-Path $saida 'logs'
$csv = Join-Path $saida 'medicoes-inicializacao.csv'
$urlSaude = "http://127.0.0.1:$PortaHttp/monolito-biblioteca/health"

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
                repeticao = $i
                inicio_iso = $inicio.ToString('o')
                duracao_ms = [math]::Round($cronometro.Elapsed.TotalMilliseconds, 3)
                duracao_s = [math]::Round($cronometro.Elapsed.TotalSeconds, 6)
                status_http = $statusHttp
                sucesso = $pronto
                commit = $commit
                java = $javaVersion
                jboss = '7.4.0.GA'
                endpoint = $urlSaude
                intervalo_consulta_ms = $IntervaloConsultaMs
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
