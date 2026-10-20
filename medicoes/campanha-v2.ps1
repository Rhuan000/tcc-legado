[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)][ValidateSet('pre-migracao', 'pos-migracao')][string]$Estado,
    [Parameter(Mandatory = $true)][ValidatePattern('^[a-z0-9][a-z0-9._-]{0,63}$')][string]$ColetaId,
    [Parameter(Mandatory = $true)][string]$Repositorio,
    [Parameter(Mandatory = $true)][ValidateSet('registrar', 'build', 'inicializacao', 'cbo', 'conferir')][string]$Etapa,
    [string]$ResultadosRaiz = (Join-Path $PSScriptRoot '..\metricas'),
    [string]$EvidenciaFuncional,
    [switch]$Piloto,
    [string]$JavaLegado = 'C:\Program Files (x86)\Java\jdk1.8.0_201',
    [string]$JavaServico = 'C:\Program Files\Java\jdk-21',
    [string]$Maven = 'C:\Program Files\Maven\apache-maven-3.9.11-bin\apache-maven-3.9.11\bin\mvn.cmd',
    [string]$JbossHome = 'C:\desenvolvimento\jboss-eap-7.4',
    [string]$CkJar = (Join-Path $PSScriptRoot '..\metricas\ck.jar'),
    [string]$ServicoDbUrl = 'jdbc:postgresql://localhost:5432/biblioteca',
    [string]$ServicoDbUsuario = 'postgres',
    [string]$ServicoDbSenha = 'postgres',
    [int]$PortaMonolito = 18080,
    [int]$PortaServico = 18081,
    [int]$PortaGerenciamento = 19990,
    [ValidateRange(10, 600)][int]$TimeoutInicializacao = 120
)

$ErrorActionPreference = 'Stop'
$repo = (Resolve-Path -LiteralPath $Repositorio).Path
$campanha = Join-Path ([IO.Path]::GetFullPath($ResultadosRaiz)) "$Estado\$ColetaId"
$registro = Join-Path $campanha 'manifesto.json'
$escopos = if ($Estado -eq 'pre-migracao') { @('monolito') } else { @('monolito', 'servico', 'conjunto') }
$projetos = @{ monolito = 'monolito-biblioteca'; servico = 'tcc-moderno/emprestimo-service' }
$javas = @{ monolito = $JavaLegado; servico = $JavaServico }
$repeticoes = if ($Piloto) { 1 } else { 10 }
$hashColetor = (Get-FileHash -LiteralPath $PSCommandPath -Algorithm SHA256).Hash
$hashCkEsperado = '2F32FA347EF739E97152741828675A9939AEAFE61BEE3F196EE5AF264E612162'

function Executar-Git {
    param([string[]]$Argumentos)
    $resultado = @(& git -C $repo @Argumentos)
    if ($LASTEXITCODE -ne 0) { throw "Falha ao consultar Git: $Argumentos" }
    return $resultado
}

function Obter-HashTexto {
    param([string]$Texto)
    $sha = [Security.Cryptography.SHA256]::Create()
    try { return ([BitConverter]::ToString($sha.ComputeHash([Text.Encoding]::UTF8.GetBytes($Texto)))).Replace('-', '') }
    finally { $sha.Dispose() }
}

function Obter-Codigo {
    $caminhos = @()
    foreach ($nome in @('monolito', 'servico')) {
        if ($nome -eq 'servico' -and $Estado -eq 'pre-migracao') { continue }
        $caminhos += "$($projetos[$nome])/pom.xml"
        $caminhos += "$($projetos[$nome])/src"
    }
    $status = @(Executar-Git (@('status', '--porcelain', '--untracked-files=all', '--') + $caminhos))
    if ($status.Count -gt 0) { throw "Codigo medido deve estar limpo: $($status -join '; ')" }
    $arquivos = @(Executar-Git (@('ls-files', '--') + $caminhos) | Sort-Object)
    if ($arquivos.Count -eq 0) { throw 'Nenhum arquivo de codigo encontrado.' }
    $linhas = foreach ($arquivo in $arquivos) {
        "$arquivo=$((Get-FileHash -LiteralPath (Join-Path $repo $arquivo) -Algorithm SHA256).Hash)"
    }
    return [pscustomobject]@{
        commit = (Executar-Git @('rev-parse', 'HEAD')) -join ''
        arvore = (Executar-Git @('rev-parse', 'HEAD^{tree}')) -join ''
        hash_fisico = Obter-HashTexto ($linhas -join "`n")
        arquivos = $arquivos.Count
    }
}

function Salvar-Json {
    param($Valor, [string]$Arquivo)
    $Valor | ConvertTo-Json -Depth 8 | Set-Content -LiteralPath $Arquivo -Encoding UTF8
}

function Obter-Versao {
    param([string]$Executavel, [string]$Argumento)
    $anterior = $ErrorActionPreference
    try {
        $ErrorActionPreference = 'Continue'
        $linhas = @(& $Executavel $Argumento 2>&1)
        if ($LASTEXITCODE -ne 0) { throw "Falha ao obter versao de $Executavel" }
        return ($linhas | ForEach-Object { $_.ToString() }) -join "`n"
    } finally { $ErrorActionPreference = $anterior }
}

function Obter-Ambiente {
    $config = Join-Path $JbossHome 'standalone\configuration\standalone.xml'
    return [pscustomobject]@{
        java_legado = (Obter-Versao (Join-Path $JavaLegado 'bin\java.exe') '-version').Split("`n")[0]
        java_servico_analisador = (Obter-Versao (Join-Path $JavaServico 'bin\java.exe') '-version').Split("`n")[0]
        java_legado_sha256 = (Get-FileHash -LiteralPath "$JavaLegado\bin\java.exe" -Algorithm SHA256).Hash
        java_servico_sha256 = (Get-FileHash -LiteralPath "$JavaServico\bin\java.exe" -Algorithm SHA256).Hash
        maven = Obter-Versao $Maven '-version'
        maven_executavel = $Maven
        jboss_home = $JbossHome
        jboss_config_sha256 = (Get-FileHash -LiteralPath $config -Algorithm SHA256).Hash
        ck_sha256 = (Get-FileHash -LiteralPath $CkJar -Algorithm SHA256).Hash
        servico_db_url = $ServicoDbUrl
        servico_db_usuario = $ServicoDbUsuario
        sistema = [Environment]::OSVersion.VersionString
        processador = $env:PROCESSOR_IDENTIFIER
        processadores_logicos = [Environment]::ProcessorCount
        powershell = $PSVersionTable.PSVersion.ToString()
        fuso = [TimeZoneInfo]::Local.Id
    }
}

function Nova-Saida {
    param([string]$Caminho)
    if (Test-Path -LiteralPath $Caminho) { throw "Saida ja existe, sem sobrescrita: $Caminho. Use outra campanha apos uma falha." }
    New-Item -ItemType Directory -Path $Caminho | Out-Null
}

function Confirmar-PortaLivre {
    param([int]$Porta)
    $cliente = New-Object Net.Sockets.TcpClient
    try {
        $tentativa = $cliente.BeginConnect('127.0.0.1', $Porta, $null, $null)
        if ($tentativa.AsyncWaitHandle.WaitOne(300)) {
            try { $cliente.EndConnect($tentativa) } catch { return }
            throw "Porta $Porta ocupada; nenhuma instancia existente sera encerrada."
        }
    } finally { $cliente.Dispose() }
}

function Aguardar-PortaLivre {
    param([int]$Porta)
    $limite = [Diagnostics.Stopwatch]::StartNew()
    while ($limite.Elapsed.TotalSeconds -lt 10) {
        $cliente = New-Object Net.Sockets.TcpClient
        try {
            $tentativa = $cliente.BeginConnect('127.0.0.1', $Porta, $null, $null)
            if (-not $tentativa.AsyncWaitHandle.WaitOne(100)) { return }
            try { $cliente.EndConnect($tentativa) } catch { return }
        } finally { $cliente.Dispose() }
        Start-Sleep -Milliseconds 100
    }
    throw "Porta $Porta permaneceu ocupada apos o encerramento da instancia medida."
}

function Obter-Http {
    param([string]$Url)
    try {
        $request = [Net.HttpWebRequest]::Create($Url)
        $request.AllowAutoRedirect = $false
        $request.Timeout = 500
        $response = $request.GetResponse()
        try { return [int]$response.StatusCode } finally { $response.Dispose() }
    } catch [Net.WebException] {
        if ($_.Exception.Response) { $_.Exception.Response.Dispose() }
        return 0
    }
}

function Encerrar-Processo {
    param($Processo)
    if ($Processo -and -not $Processo.HasExited) {
        & taskkill.exe /PID $Processo.Id /T /F | Out-Null
        if (-not $Processo.WaitForExit(10000)) { throw "Processo $($Processo.Id) nao encerrou." }
    }
}

function Medir-Build {
    param([string]$Escopo, [string]$Saida)
    $componentes = if ($Escopo -eq 'conjunto') { @('monolito', 'servico') } else { @($Escopo) }
    $resultados = @()
    for ($repeticao = 1; $repeticao -le $repeticoes; $repeticao++) {
        $inicio = (Get-Date).ToString('o')
        $codigoSaida = 0
        $cronometro = [Diagnostics.Stopwatch]::StartNew()
        foreach ($componente in $componentes) {
            $env:JAVA_HOME = $javas[$componente]
            $env:Path = "$env:JAVA_HOME\bin;$pathAnterior"
            $env:JDK_JAVA_OPTIONS = if ($componente -eq 'servico') { "-Djdk.net.unixdomain.tmpdir=$repo\tcc-moderno\emprestimo-service\target\sockets-nao-existentes" } else { $null }
            $prefixo = Join-Path $Saida ("{0}-{1:D2}" -f $componente, $repeticao)
            $processo = Start-Process -FilePath $Maven -ArgumentList @('-B', '-o', 'clean', 'package') -WorkingDirectory (Join-Path $repo $projetos[$componente]) -RedirectStandardOutput "$prefixo.stdout.log" -RedirectStandardError "$prefixo.stderr.log" -WindowStyle Hidden -PassThru
            $processo.Handle | Out-Null
            if (-not $processo.WaitForExit(1200000)) { Encerrar-Processo $processo; $codigoSaida = -1; break }
            $processo.WaitForExit()
            $codigoSaida = $processo.ExitCode
            if ($codigoSaida -ne 0) { break }
        }
        $cronometro.Stop()
        $resultados += [pscustomobject]@{ estado = $Estado; coleta_id = $ColetaId; escopo = $Escopo; repeticao = $repeticao; inicio_iso = $inicio; duracao_ms = $cronometro.Elapsed.TotalMilliseconds; sucesso = ($codigoSaida -eq 0); codigo_saida = $codigoSaida; commit = $codigo.commit; coletor_sha256 = $hashColetor }
        $resultados | Export-Csv -LiteralPath (Join-Path $Saida 'medicoes.csv') -NoTypeInformation -Encoding UTF8
        if ($codigoSaida -ne 0) { throw "Build falhou: $Escopo, repeticao $repeticao. Consulte $Saida." }
    }
    Salvar-Json @(Obter-Artefatos $Escopo) (Join-Path $Saida 'artefatos.json')
}

function Obter-Artefatos {
    param([string]$Escopo)
    $arquivos = @()
    if ($Escopo -ne 'servico') { $arquivos += Join-Path $repo 'monolito-biblioteca\target\monolito-biblioteca.war' }
    if ($Escopo -ne 'monolito') {
        $pasta = Join-Path $repo 'tcc-moderno\emprestimo-service\target\quarkus-app'
        if (-not (Test-Path -LiteralPath (Join-Path $pasta 'quarkus-run.jar'))) { throw 'Artefato do servico ausente.' }
        $arquivos += @(Get-ChildItem -LiteralPath $pasta -Recurse -File | Sort-Object FullName | Select-Object -ExpandProperty FullName)
    }
    foreach ($arquivo in $arquivos) { [pscustomobject]@{ caminho = $arquivo.Substring($repo.Length); sha256 = (Get-FileHash -LiteralPath $arquivo -Algorithm SHA256).Hash } }
}

function Medir-Inicializacao {
    param([string]$Escopo, [string]$Saida)
    $usaMonolito = $Escopo -ne 'servico'
    $usaServico = $Escopo -ne 'monolito'
    $war = Join-Path $repo 'monolito-biblioteca\target\monolito-biblioteca.war'
    $jar = Join-Path $repo 'tcc-moderno\emprestimo-service\target\quarkus-app\quarkus-run.jar'
    $urls = @()
    if ($usaServico) { $urls += "http://127.0.0.1:$PortaServico/q/health/live" }
    if ($usaMonolito) { $urls += "http://127.0.0.1:$PortaMonolito/monolito-biblioteca/health" }
    $hashes = @(Obter-Artefatos $Escopo)
    $ultimoEscopoBuild = if ($Estado -eq 'pre-migracao') { 'monolito' } else { 'conjunto' }
    $buildMedido = @(Import-Csv -LiteralPath (Join-Path $campanha "$ultimoEscopoBuild\build\medicoes.csv"))
    if ($buildMedido.Count -ne $repeticoes -or @($buildMedido | Where-Object { $_.sucesso -ne 'True' -or $_.commit -ne $codigo.commit }).Count -gt 0) { throw 'Build medido incompleto ou divergente.' }
    $artefatosBuild = @(Get-Content -LiteralPath (Join-Path $campanha "$ultimoEscopoBuild\build\artefatos.json") -Raw | ConvertFrom-Json)
    foreach ($arquivo in $hashes) {
        if (@($artefatosBuild | Where-Object { $_.caminho -eq $arquivo.caminho -and $_.sha256 -eq $arquivo.sha256 }).Count -ne 1) { throw "Artefato nao corresponde ao build medido: $($arquivo.caminho)" }
    }
    Salvar-Json $hashes (Join-Path $Saida 'artefatos.json')
    $hashArtefatos = Obter-HashTexto ($hashes | ConvertTo-Json -Compress)
    $resultados = @()
    for ($repeticao = 1; $repeticao -le $repeticoes; $repeticao++) {
        if ($usaServico) { Confirmar-PortaLivre $PortaServico }
        if ($usaMonolito) { Confirmar-PortaLivre $PortaMonolito; Confirmar-PortaLivre $PortaGerenciamento }
        $base = $null
        if ($usaMonolito) {
            $base = Join-Path $env:TEMP ('tcc-campanha-' + [guid]::NewGuid().ToString('N'))
            New-Item -ItemType Directory -Path "$base\configuration", "$base\deployments" | Out-Null
            Copy-Item -Path "$JbossHome\standalone\configuration\*" -Destination "$base\configuration" -Recurse
            Copy-Item -LiteralPath $war -Destination "$base\deployments\monolito-biblioteca.war"
        }
        $processos = @()
        $pronto = $false
        $inicio = (Get-Date).ToString('o')
        $cronometro = [Diagnostics.Stopwatch]::StartNew()
        try {
            $env:JDK_JAVA_OPTIONS = $null
            if ($usaServico) {
                $env:EMPRESTIMO_DB_URL = $ServicoDbUrl
                $env:EMPRESTIMO_DB_USERNAME = $ServicoDbUsuario
                $env:EMPRESTIMO_DB_PASSWORD = $ServicoDbSenha
                $argumentos = @("-Dquarkus.http.port=$PortaServico", '-jar', ('"' + $jar + '"'))
                $processos += Start-Process -FilePath "$JavaServico\bin\java.exe" -ArgumentList $argumentos -RedirectStandardOutput (Join-Path $Saida "servico-$repeticao.stdout.log") -RedirectStandardError (Join-Path $Saida "servico-$repeticao.stderr.log") -WindowStyle Hidden -PassThru
            }
            if ($usaMonolito) {
                $env:JAVA_HOME = $JavaLegado
                $env:Path = "$JavaLegado\bin;$pathAnterior"
                $env:NOPAUSE = '1'
                $argumentos = @(('"-Djboss.server.base.dir=' + $base + '"'), "-Djboss.http.port=$PortaMonolito", "-Djboss.management.http.port=$PortaGerenciamento")
                $processos += Start-Process -FilePath "$JbossHome\bin\standalone.bat" -ArgumentList $argumentos -RedirectStandardOutput (Join-Path $Saida "monolito-$repeticao.stdout.log") -RedirectStandardError (Join-Path $Saida "monolito-$repeticao.stderr.log") -WindowStyle Hidden -PassThru
            }
            while ($cronometro.Elapsed.TotalSeconds -lt $TimeoutInicializacao) {
                $estadosHttp = @($urls | ForEach-Object { Obter-Http $_ })
                if (@($estadosHttp | Where-Object { $_ -ne 200 }).Count -eq 0 -and @($processos | Where-Object { $_.HasExited }).Count -eq 0) { $pronto = $true; break }
                if (@($processos | Where-Object { $_.HasExited }).Count -gt 0) { break }
                Start-Sleep -Milliseconds 100
            }
            $cronometro.Stop()
        } finally {
            $cronometro.Stop()
            $resultados += [pscustomobject]@{ estado = $Estado; coleta_id = $ColetaId; escopo = $Escopo; repeticao = $repeticao; inicio_iso = $inicio; duracao_ms = $cronometro.Elapsed.TotalMilliseconds; sucesso = $pronto; endpoints = ($urls -join ';'); commit = $codigo.commit; coletor_sha256 = $hashColetor; artefatos_sha256 = $hashArtefatos; base_jboss = $base }
            $resultados | Export-Csv -LiteralPath (Join-Path $Saida 'medicoes.csv') -NoTypeInformation -Encoding UTF8
            foreach ($processo in $processos) { Encerrar-Processo $processo }
            if ($usaServico) { Aguardar-PortaLivre $PortaServico }
            if ($usaMonolito) { Aguardar-PortaLivre $PortaMonolito; Aguardar-PortaLivre $PortaGerenciamento }
        }
        if (-not $pronto) { throw "Inicializacao falhou: $Escopo, repeticao $repeticao. Consulte $Saida." }
    }
}

function Medir-Cbo {
    param([string]$Escopo, [string]$Saida)
    $fontes = Join-Path $repo "$($projetos[$Escopo])\src\main\java"
    $argumentos = @('-jar', ('"' + $CkJar + '"'), ('"' + $fontes + '"'), 'false', '0', 'true')
    $processo = Start-Process -FilePath "$JavaServico\bin\java.exe" -ArgumentList $argumentos -WorkingDirectory $Saida -RedirectStandardOutput (Join-Path $Saida 'ck.stdout.log') -RedirectStandardError (Join-Path $Saida 'ck.stderr.log') -WindowStyle Hidden -PassThru
    $processo.Handle | Out-Null
    if (-not $processo.WaitForExit(300000)) { Encerrar-Processo $processo; throw "Timeout CK em $Escopo" }
    $processo.WaitForExit()
    if ($processo.ExitCode -ne 0) { throw "CK falhou em $Escopo" }
    $classes = @(Import-Csv -LiteralPath (Join-Path $Saida 'class.csv'))
    if ($classes.Count -eq 0) { throw "CK nao retornou tipos em $Escopo" }
    $arquivos = @(Get-ChildItem -LiteralPath $fontes -Recurse -Filter *.java | Where-Object { $_.Name -notin @('package-info.java', 'module-info.java') })
    foreach ($arquivo in $arquivos) {
        if (@($classes | Where-Object { [IO.Path]::GetFullPath($_.file) -eq $arquivo.FullName }).Count -eq 0) { throw "CK omitiu $($arquivo.FullName)" }
    }
    Salvar-Json ([pscustomobject]@{ escopo = $Escopo; commit = $codigo.commit; ck_sha256 = $hashCkEsperado; arquivos = $arquivos.Count; tipos = $classes.Count; coletor_sha256 = $hashColetor }) (Join-Path $Saida 'metadados.json')
}

$envAnterior = @{}
foreach ($nome in @('JAVA_HOME', 'Path', 'JDK_JAVA_OPTIONS', 'JAVA_TOOL_OPTIONS', '_JAVA_OPTIONS', 'NOPAUSE', 'EMPRESTIMO_DB_URL', 'EMPRESTIMO_DB_USERNAME', 'EMPRESTIMO_DB_PASSWORD')) { $envAnterior[$nome] = [Environment]::GetEnvironmentVariable($nome, 'Process') }
$pathAnterior = $env:Path
try {
    $env:JAVA_HOME = $JavaLegado
    $env:Path = "$JavaLegado\bin;$pathAnterior"
    $env:JDK_JAVA_OPTIONS = $null
    $env:JAVA_TOOL_OPTIONS = $null
    $env:_JAVA_OPTIONS = $null
    $codigo = Obter-Codigo
    $ambiente = Obter-Ambiente
    if ($ambiente.ck_sha256 -ne $hashCkEsperado) { throw 'CK diverge do instrumento fixado.' }
    if ($ambiente.maven -notmatch 'Apache Maven 3\.9\.11') { throw 'A campanha exige Maven 3.9.11.' }
    if ($Etapa -eq 'registrar') {
        $validacao = $null
        if (-not $Piloto) {
            if (-not $EvidenciaFuncional) { throw 'Campanha final exige evidencia funcional JSON; use -Piloto apenas para ensaio do instrumento.' }
            $validacao = Get-Content -LiteralPath $EvidenciaFuncional -Raw | ConvertFrom-Json
            if ($validacao.commit -ne $codigo.commit -or $validacao.estado -ne $Estado -or $validacao.resultado -ne 'aprovado' -or -not $validacao.evidencias) { throw 'Evidencia funcional deve aprovar este estado e commit e identificar os registros de execucao.' }
        }
        Nova-Saida $campanha
        if ($validacao) { Copy-Item -LiteralPath $EvidenciaFuncional -Destination (Join-Path $campanha 'validacao-funcional.json') }
        Salvar-Json ([pscustomobject]@{ protocolo = 'v2'; estado = $Estado; coleta_id = $ColetaId; piloto = [bool]$Piloto; repeticoes = $repeticoes; registro_iso = (Get-Date).ToString('o'); codigo = $codigo; ambiente = $ambiente; coletor_sha256 = $hashColetor; validacao_sha256 = if ($validacao) { (Get-FileHash -LiteralPath $EvidenciaFuncional -Algorithm SHA256).Hash } else { $null }; portas = @($PortaMonolito, $PortaServico, $PortaGerenciamento); timeout_inicializacao = $TimeoutInicializacao }) $registro
        Write-Output "Campanha registrada: $registro"
        return
    }
    $manifesto = Get-Content -LiteralPath $registro -Raw | ConvertFrom-Json
    if ($manifesto.estado -ne $Estado -or $manifesto.coleta_id -ne $ColetaId -or $manifesto.codigo.commit -ne $codigo.commit -or $manifesto.codigo.hash_fisico -ne $codigo.hash_fisico -or $manifesto.coletor_sha256 -ne $hashColetor -or $manifesto.piloto -ne [bool]$Piloto -or ($manifesto.ambiente | ConvertTo-Json -Compress) -ne ($ambiente | ConvertTo-Json -Compress) -or ($manifesto.portas -join ',') -ne (@($PortaMonolito, $PortaServico, $PortaGerenciamento) -join ',') -or $manifesto.timeout_inicializacao -ne $TimeoutInicializacao) { throw 'Codigo, instrumento, ambiente ou parametros divergem do registro da campanha.' }
    if ($Etapa -eq 'conferir') {
        foreach ($escopo in $escopos) {
            foreach ($metrica in @('build', 'inicializacao')) {
                $linhas = @(Import-Csv -LiteralPath (Join-Path $campanha "$escopo\$metrica\medicoes.csv"))
                if ($linhas.Count -ne $repeticoes -or @($linhas | Where-Object { $_.sucesso -ne 'True' -or $_.commit -ne $codigo.commit -or $_.coletor_sha256 -ne $hashColetor }).Count -gt 0) { throw "Campanha incompleta ou falha em $escopo/$metrica" }
            }
            if ($escopo -ne 'conjunto' -and -not (Test-Path -LiteralPath (Join-Path $campanha "$escopo\cbo\metadados.json"))) { throw "CBO ausente: $escopo" }
        }
        Write-Output "Campanha completa; piloto=$Piloto. Conferir evidencias funcionais antes de publicar."
        return
    }
    foreach ($escopo in $escopos) {
        if ($Etapa -eq 'cbo' -and $escopo -eq 'conjunto') { continue }
        $saida = Join-Path $campanha "$escopo\$Etapa"
        Nova-Saida $saida
        switch ($Etapa) {
            'build' { Medir-Build $escopo $saida }
            'inicializacao' { Medir-Inicializacao $escopo $saida }
            'cbo' { Medir-Cbo $escopo $saida }
        }
        if ((Obter-Codigo).hash_fisico -ne $codigo.hash_fisico) { throw 'Codigo mudou durante a medicao; campanha rejeitada.' }
    }
} finally {
    foreach ($nome in $envAnterior.Keys) { [Environment]::SetEnvironmentVariable($nome, $envAnterior[$nome], 'Process') }
}
