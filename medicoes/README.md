# Coletores e protocolo de medição

As campanhas utilizadas no TCC foram concluídas em 20/10/2026. O índice dos
dados está em [metricas/README.md](../metricas/README.md). Este diretório mantém
os três scripts necessários para novas execuções:

| Script | Finalidade |
| --- | --- |
| `campanha-v2.ps1` | Registro do ambiente, build com testes, inicialização e CBO nos estados anterior e posterior. |
| `medir-build-monolito-sem-testes.ps1` | Build suplementar sem compilação ou execução de testes, nos dois estados do monólito. |
| `medir-build-servico-sem-testes.ps1` | Build suplementar sem compilação ou execução de testes do serviço posterior. |

Os registros brutos das campanhas publicadas foram preservados. O script atual
é a referência para novas execuções; `-Etapa conferir` exige que o código, o
instrumento, o ambiente e os parâmetros sejam os mesmos do registro daquela
execução, portanto não é um validador independente das campanhas históricas.

## Estados e evidências

- Anterior: `aa55edd88c412725c84c10bff92e88a503e39fcb`.
- Posterior: `45ff20b3d68ff484143a7b28e35b7c1d93fc361b`.
- Evidências funcionais: os JSON de [validacao/](../validacao/) identificam os
  estados, os pacotes de 41 casos de cada versão e os limites das verificações.

Use worktrees com o `pom.xml` e o `src` dos projetos medidos limpos. O coletor
registra a revisão, a árvore Git, os hashes do código e do instrumento e as
versões do ambiente. Na etapa `registrar`, uma campanha final exige
`-EvidenciaFuncional`: JSON com `estado`, `commit` completo,
`resultado: "aprovado"` e `evidencias` de execução. O coletor verifica a
identificação e copia o documento; a análise dos casos funcionais é separada
da medição de desempenho.

## Ambiente

O protocolo utiliza Windows, Git e PowerShell, Maven 3.9.11, JDK 1.8.0_201 no
monólito, JDK 21 no serviço e no analisador, JBoss EAP 7.4 e CK
0.7.1-SNAPSHOT. O SHA-256 esperado do JAR CK é
`2F32FA347EF739E97152741828675A9939AEAFE61BEE3F196EE5AF264E612162`.
O JAR não é versionado: informe seu caminho em `-CkJar` ou disponibilize-o em
`metricas/ck.jar`, o caminho padrão do script.

Resolva previamente as dependências Maven e disponibilize as imagens e o
ambiente Docker usados pelos testes do serviço, pois os builds usam `-o`
(offline). Execute na mesma máquina, sem builds ou ensaios concorrentes. Os
caminhos de Java, Maven e JBoss podem ser informados pelos parâmetros do script.

Para a inicialização, configure o banco descartável de ensaio e as integrações
do JBoss e do serviço. Recrie a base entre estados, sem acesso concorrente, e
registre a massa inicial e os efeitos dos agendadores. Os scripts de apoio estão
em [validacao/jobs/](../validacao/jobs/). As rotinas conservam suas configurações
de agendamento. Configure credenciais localmente, sem versionar segredos.

## Campanha principal

Cada condição tem dez repetições de build e de inicialização:

| Estado | Escopo | Build com testes | Inicialização | CBO |
| --- | --- | --- | --- | --- |
| Anterior | monólito | 10 | 10 | Uma análise |
| Posterior | monólito remanescente | 10 | 10 | Uma análise |
| Posterior | serviço | 10 | 10 | Uma análise |
| Posterior | conjunto | 10 | 10 | Reunião das duas distribuições |

Build: `mvn -B -o clean package`, incluindo os testes existentes e o
empacotamento. No conjunto, o coletor mede o tempo de parede do monólito seguido
do serviço, com a orquestração. As suítes são distintas: os logs publicados
registram 33 testes no anterior, 32 no legado posterior com três ignorados
(29 executados) e 165 no serviço. A diferença de tempo inclui essa diferença de
validação.

Inicialização: um processo novo por aplicação em cada repetição, com os
artefatos já construídos. O JBoss recebe uma base temporária e o WAR antes do
cronômetro. No conjunto, o serviço é iniciado antes do JBoss, sem aguardar uma
resposta de saúde entre as partidas. O tempo termina quando os dois endpoints
retornam HTTP 200 na mesma rodada: `/monolito-biblioteca/health` e
`/q/health/live`. O intervalo solicitado é de 100 ms, com timeout de 500 ms por
consulta e limite padrão de 120 segundos. A medida representa disponibilidade
HTTP; a validação funcional é documentada separadamente.

As portas padrão são 18080 (monólito), 18081 (serviço) e 19990 (gerenciamento
JBoss). Portas ocupadas impedem a coleta. Ao final de cada repetição, o coletor
encerra as árvores de processos que iniciou; desligamento e preparação da base
ficam fora do tempo medido. Bases temporárias e registros de saída permitem
inspecionar a execução.

CBO: uma execução do CK em `src/main/java` de cada projeto, sem testes, usando
`java -jar <CK> <fontes> false 0 true`. A distribuição por tipo está em
`cbo/class.csv`. A média do conjunto posterior deve ser calculada reunindo as
linhas dos dois projetos, ou ponderando pelo número de tipos. O conjunto não
recebe uma quarta análise CK.

### Executar uma nova campanha

Execute a partir da raiz do repositório que contém os coletores. Substitua os
caminhos, a conexão e a evidência pelos do ensaio, e escolha um identificador
novo. O exemplo usa um worktree do estado anterior:

```powershell
$parametros = @{
    Estado = 'pre-migracao'
    ColetaId = 'nova-campanha-pre-01'
    Repositorio = 'C:\caminho\worktree-pre'
    ResultadosRaiz = 'C:\caminho\resultados-novos'
    CkJar = 'C:\desenvolvimento\ck\target\ck-0.7.1-SNAPSHOT-jar-with-dependencies.jar'
    ServicoDbUrl = 'jdbc:postgresql://localhost:5432/biblioteca'
    ServicoDbUsuario = 'postgres'
    ServicoDbSenha = $env:TCC_SERVICO_DB_SENHA
}
.\medicoes\campanha-v2.ps1 @parametros -Etapa registrar -EvidenciaFuncional '.\validacao\validacao-funcional-pre-aa55edd.json'
.\medicoes\campanha-v2.ps1 @parametros -Etapa build
.\medicoes\campanha-v2.ps1 @parametros -Etapa inicializacao
.\medicoes\campanha-v2.ps1 @parametros -Etapa cbo
.\medicoes\campanha-v2.ps1 @parametros -Etapa conferir
```

No posterior, altere `Estado`, `ColetaId`, `Repositorio` e a evidência funcional
para os desse estado. Mantenha a mesma revisão do coletor e os mesmos parâmetros
entre as etapas. Para ensaiar o instrumento antes da campanha, use outro
identificador e adicione `-Piloto` em todas as etapas: será feita uma repetição,
identificada no manifesto e fora da análise final. Os scripts recusam
sobrescrita; preserve uma execução incompleta e use outro identificador após
investigar uma falha.

## Build suplementar sem testes

O comando é `mvn -B -o clean package -Dmaven.test.skip=true`. Essa opção não
compila nem executa as fontes de teste. Os scripts registram dez observações,
manifesto, comando, ambiente, revisão e árvore do projeto, hashes e saídas
individuais. Essas séries são analisadas separadamente do build com testes.

```powershell
.\medicoes\medir-build-monolito-sem-testes.ps1 -Estado pre-migracao -ColetaId nova-sem-testes-pre-01 -Repositorio 'C:\caminho\worktree-pre' -ResultadosRaiz 'C:\caminho\resultados-novos'
.\medicoes\medir-build-monolito-sem-testes.ps1 -Estado pos-migracao -ColetaId nova-sem-testes-pos-01 -Repositorio 'C:\caminho\worktree-pos' -ResultadosRaiz 'C:\caminho\resultados-novos'
.\medicoes\medir-build-servico-sem-testes.ps1 -ColetaId nova-sem-testes-servico-01 -Repositorio 'C:\caminho\worktree-pos' -ResultadosRaiz 'C:\caminho\resultados-novos'
```

Não foi medido um build conjunto sem testes. A soma das médias isoladas, se
apresentada, deve ser identificada como estimativa.

## Interpretação e histórico

Os CSV são a base numérica; logs e manifestos documentam execução e contexto.
Relate observações, média, mediana, dispersão e diferenças absolutas e relativas.
Dez repetições locais descrevem a variabilidade deste ambiente. A modernização
muda arquitetura, código, runtime, framework e testes simultaneamente; o estudo
não isola uma causa única. CBO cobre dependências estáticas entre tipos, enquanto
os contratos HTTP e as dependências operacionais são discutidos no texto.

O protocolo V1, seus quatro scripts, os pilotos, as coletas preliminares e a
recoleta de setembro foram retirados da árvore atual por não integrarem os
resultados finais. Permanecem recuperáveis no commit
[`58a67c39281c54424b8ca5524d05135f58ed235a`](https://github.com/Rhuan000/tcc-legado/tree/58a67c39281c54424b8ca5524d05135f58ed235a).
As decisões do desenvolvimento continuam em
[docs/tcc/DIARIO-DE-BORDO.md](../docs/tcc/DIARIO-DE-BORDO.md).
