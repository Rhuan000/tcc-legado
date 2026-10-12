[CmdletBinding()]
param(
    [string]$Container = 'biblioteca-postgres',
    [string]$Banco = 'biblioteca',
    [string]$UsuarioBanco = 'postgres',
    [Parameter(Mandatory = $true)][string]$Saida,
    [datetime]$DataReferencia = [datetime]::Today
)

$ErrorActionPreference = 'Stop'
if (Test-Path -LiteralPath $Saida) { throw 'A pasta de evidencia ja existe; escolha outro nome.' }
$sql = @'
SELECT e.id,u.tipo,e.data_emprestimo,e.data_prevista_devolucao,e.data_devolucao_real,e.multa,
       CURRENT_DATE AS data_banco,current_setting('TimeZone') AS fuso_banco
FROM emprestimo e JOIN usuario u ON u.id=e.id_usuario
WHERE e.id BETWEEN 911001 AND 911006 ORDER BY e.id
'@
$bruto = @(& docker exec $Container psql -X -U $UsuarioBanco -d $Banco --csv -c $sql)
if ($LASTEXITCODE -ne 0) { throw 'Falha ao consultar o banco.' }
$registros = @($bruto | ConvertFrom-Csv)
if ($registros.Count -ne 6) { throw 'A massa deve conter os seis casos de multas.' }
$referencia = $DataReferencia.Date
$formato = [Globalization.CultureInfo]::InvariantCulture
$dataBanco = [datetime]::ParseExact($registros[0].data_banco, 'yyyy-MM-dd', $formato)
$geracao = [datetime]::ParseExact($registros[0].data_emprestimo, 'yyyy-MM-dd', $formato).AddDays(21)
if ($referencia -ne $dataBanco -or $referencia -ne $geracao) { throw 'Use a data de referencia da massa e alinhe as datas do banco e da aplicacao.' }
New-Item -ItemType Directory -Path $Saida | Out-Null
$bruto | Set-Content -LiteralPath (Join-Path $Saida 'observado.csv') -Encoding UTF8
$feriados = @{}
$resultados = foreach ($registro in $registros) {
    $vencimento = [datetime]::ParseExact($registro.data_prevista_devolucao, 'yyyy-MM-dd', $formato)
    $esperado = [decimal]0
    $diasUteis = 0
    if ($registro.id -eq '911006') {
        $esperado = [decimal]19.50
    } elseif ($vencimento -lt $referencia) {
        $ano = $vencimento.Year
        if (-not $feriados.ContainsKey($ano)) {
            $resposta = Invoke-WebRequest -UseBasicParsing -Uri "https://brasilapi.com.br/api/feriados/v1/$ano" -TimeoutSec 15
            $resposta.Content | Set-Content -LiteralPath (Join-Path $Saida "feriados-$ano.json") -Encoding UTF8
            $feriados[$ano] = @($resposta.Content | ConvertFrom-Json | ForEach-Object { $_.date })
        }
        for ($dia = $vencimento.AddDays(1); $dia -le $referencia; $dia = $dia.AddDays(1)) {
            if ($dia.DayOfWeek -notin @([DayOfWeek]::Saturday,[DayOfWeek]::Sunday) -and $dia.ToString('yyyy-MM-dd') -notin $feriados[$ano]) { $diasUteis++ }
        }
        $tarifa = switch ($registro.tipo) { 'PROFESSOR' { [decimal]0.50 }; 'BOLSISTA' { [decimal]1.00 }; default { [decimal]2.00 } }
        $esperado = $diasUteis * $tarifa
    }
    $observado = [decimal]::Parse($registro.multa, $formato)
    [pscustomobject]@{ id = $registro.id; tipo = $registro.tipo; referencia = $referencia.ToString('yyyy-MM-dd'); dias_uteis = $diasUteis; esperado = $esperado; observado = $observado; confere = ($esperado -eq $observado) }
}
$resultados | Export-Csv -LiteralPath (Join-Path $Saida 'conferencia.csv') -NoTypeInformation -Encoding UTF8
[pscustomobject]@{ instante = (Get-Date).ToString('o'); container = $Container; banco = $Banco; fuso_banco = $registros[0].fuso_banco; fuso_host = [TimeZoneInfo]::Local.Id; script_sha256 = (Get-FileHash -LiteralPath $PSCommandPath -Algorithm SHA256).Hash } | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $Saida 'contexto.json') -Encoding UTF8
$resultados | Format-Table -AutoSize
if (@($resultados | Where-Object { -not $_.confere }).Count -gt 0) { throw 'Ha divergencias. Preserve a evidencia e investigue antes de aprovar o cenario.' }
