# Protocolo de medicao experimental

Este diretorio contem os coletores usados nos tres estados do experimento:

- `pre-migracao`
- `intermediario`
- `pos-migracao`

O protocolo usa dez repeticoes independentes para tempo de build e tempo de
inicializacao. O CBO e coletado uma unica vez por estado, pois e uma metrica
estatica e deterministica para o mesmo codigo-fonte e a mesma versao da
ferramenta.

## Ambiente de referencia

- Sistema operacional: Windows 11
- JDK do monolito: Oracle JDK 1.8.0_201, 32 bits
- Maven: 3.9.11
- Servidor: JBoss EAP 7.4.0.GA
- CK: 0.7.1-SNAPSHOT
- JAR local do CK: `metricas/ck.jar` (nao versionado)
- SHA-256 esperado do CK:
  `2F32FA347EF739E97152741828675A9939AEAFE61BEE3F196EE5AF264E612162`
- Endpoint de prontidao do monolito:
  `http://127.0.0.1:18080/monolito-biblioteca/health`
- Codigo HTTP exigido para prontidao: 200

O caminho de Java 8 descrito anteriormente no texto do TCC como
`jdk1.8.0_411` nao existe nesta maquina. As medicoes usam a instalacao real em
`C:\Program Files (x86)\Java\jdk1.8.0_201` e essa versao deve ser refletida no
texto final.

## Tempo de build

Cada observacao mede o tempo de parede do comando `mvn clean package`, desde a
criacao do processo ate seu encerramento. O diretorio `target` e removido pelo
proprio Maven em todas as repeticoes. Dependencias Maven devem estar resolvidas
antes da coleta; a validacao previa nao integra as dez amostras.

```powershell
.\medicoes\medir-build.ps1 -Estado pre-migracao -Repeticoes 10
```

## Tempo de inicializacao

O WAR e construido antes das repeticoes e nao integra o tempo de inicializacao.
Para cada observacao, o coletor cria uma base temporaria isolada do JBoss,
inclui o WAR antes de iniciar o cronometro, inicia um novo processo e consulta o
endpoint de saude a cada 100 ms. O cronometro para na primeira resposta HTTP
200. Em seguida o servidor e encerrado; o tempo de desligamento nao integra a
medicao.

```powershell
.\medicoes\medir-inicializacao.ps1 -Estado pre-migracao -Repeticoes 10
```

## CBO

O CK analisa o mesmo diretorio de fontes uma vez por estado. O coletor registra
o commit, a versao da ferramenta e o SHA-256 do JAR para permitir reproducao.

```powershell
.\medicoes\medir-cbo.ps1 -Estado pre-migracao
```

## Ordem de coleta

1. Confirmar que nao ha outra instancia usando as portas 18080 e 19990.
2. Executar as dez medicoes de build.
3. Executar as dez medicoes de inicializacao.
4. Executar uma coleta de CBO.
5. Registrar o hash do codigo com
   `.\medicoes\registrar-estado.ps1 -Estado <estado>`.
6. Nao editar manualmente os CSVs brutos.

Os resultados sao gravados em `metricas/<estado>/`. Logs individuais ficam ao
lado dos CSVs e permitem auditar falhas sem misturar mensagens do processo com
os tempos registrados.

Os CSVs sao os dados primarios do experimento. A planilha XLSX e um artefato
derivado para inspecao e apresentacao; sua geracao nao faz parte da coleta e
nao exige Node.js. Os resultados publicados devem sempre poder ser conferidos
diretamente nos CSVs brutos.
