# Dados utilizados no TCC

Este diretório reúne os resultados finais da **recoleta de 23/10/2026**:
110 observações de tempo, três análises estáticas CK e os registros necessários
para conferir sua origem. São 40 observações de build com testes, 40 de
inicialização HTTP e 30 de build sem testes, com dez repetições em cada série.

Comece por [resumo-estatistico.csv](resumo-estatistico.csv), que contém resultados
derivados dos CSV brutos. Confira os bytes publicados pelo
[inventário de integridade](integridade-publicada.json). O procedimento para
uma nova execução está em [medicoes/README.md](../medicoes/README.md).

## Índice das campanhas

| Estado | Diretório | Conteúdo |
| --- | --- | --- |
| Anterior | [final-v2-pre-20261023-01](pre-migracao/final-v2-pre-20261023-01/) | 10 builds com testes, 10 inicializações e CBO do monólito; 53 arquivos. |
| Posterior | [final-v2-pos-20261023-01](pos-migracao/final-v2-pos-20261023-01/) | 10 builds e 10 inicializações por escopo: monólito, serviço e conjunto; CBO dos dois projetos; 188 arquivos. |
| Anterior | [sem-testes-pre-20261023-01](pre-migracao/sem-testes-pre-20261023-01/) | 10 builds do monólito sem compilação ou execução de testes; 22 arquivos. |
| Posterior | [sem-testes-pos-20261023-01](pos-migracao/sem-testes-pos-20261023-01/) | 10 builds sem testes do monólito e 10 do serviço; 44 arquivos. |
| Contexto | [contexto-20261023](contexto-20261023/) | Ambiente, comandos, preparação do banco, plano e status final. |

Os **307 arquivos brutos finais** foram preservados integralmente, incluindo
logs vazios. Os pilotos ficaram fora da seleção e das estatísticas. O plano e
os registros de contexto mencionam os pilotos porque documentam a sequência
real da execução; essas menções não representam observações usadas no TCC.

## Identificação do código e dos instrumentos

- Todas as séries anteriores: `aa55edd88c412725c84c10bff92e88a503e39fcb`.
- Todas as séries posteriores, inclusive as suplementares e o serviço:
  `45ff20b3d68ff484143a7b28e35b7c1d93fc361b`.

Os três arquivos publicados em [medicoes](../medicoes/) são os mesmos bytes
utilizados na recoleta:

| Instrumento | SHA-256 |
| --- | --- |
| `campanha-v2.ps1` | `1ED17E68874356015D0707818A20DBD0E1BADE07E3B3C358E1482ACD24EDA17B` |
| `medir-build-monolito-sem-testes.ps1` | `55C1BDDEB5CE5F67AF145EE55F5EFC029A15BD714DBEB601127637DEC92759B7` |
| `medir-build-servico-sem-testes.ps1` | `78D274A109BEC3AACAD2583CA22363EED1B02316AC1F4062F63BE39B144E9336` |

Esses valores conferem com os registrados nos CSV e manifestos. O
`.gitattributes` impede conversões de fim de linha nesses instrumentos e no
acervo de métricas; uma conversão de codificação ou de CRLF/LF mudaria o hash.

## Onde conferir cada resultado

Nas duas campanhas principais:

- `manifesto.json`: estado, revisão, hashes, ambiente e parâmetros.
- `validacao-funcional.json`: vínculo histórico com os pacotes funcionais.
- `<escopo>/build/medicoes.csv` e `<escopo>/inicializacao/medicoes.csv`:
  dez observações por série em `duracao_ms`.
- `<escopo>/build/artefatos.json` e
  `<escopo>/inicializacao/artefatos.json`: identificação dos artefatos.
- `<projeto>/cbo/class.csv`: CBO por tipo. Os demais CSV, `metadados.json`
  e logs preservam a saída completa do CK.
- `*.stdout.log` e `*.stderr.log`: saída individual dos builds,
  inicializações e análises estáticas.

Nas suplementares, cada `<projeto>/build-sem-testes/` reúne `medicoes.csv`,
`manifesto.json` e os logs das dez execuções.

Em [contexto-20261023](contexto-20261023/), `contexto.json` registra ferramentas,
commits, hashes e ambiente Docker; `preparacao-base.jsonl` identifica os SQLs
aplicados; `comandos.jsonl` e `plano-execucao.json` registram a sequência;
`status-final.json` registra sucesso e restauração do container original.

## Como recalcular e interpretar

Divida `duracao_ms` por 1.000 para obter segundos. Calcule média, mediana e
desvio padrão amostral sobre as dez observações de cada série, preservando todas
as repetições bem-sucedidas. A inicialização do monólito posterior inclui uma
observação de 9,523 segundos; ela foi mantida.

O build principal utiliza `mvn -B -o clean package`. Os logs registram
33 testes no anterior, 32 no monólito posterior com três ignorados
(29 executados) e 165 no serviço. Os tempos incluem essa diferença de suítes.
No conjunto posterior, o tempo de parede mede monólito seguido do serviço,
incluindo a orquestração. O suplementar utiliza
`mvn -B -o clean package -Dmaven.test.skip=true`, sem compilar nem executar
testes. Não existe série medida do conjunto sem testes: a soma das médias
isoladas deve ser identificada como estimativa.

A inicialização termina quando os endpoints do protocolo respondem HTTP 200.
Ela não substitui a validação funcional nem mede prontidão completa do banco.
Os casos manuais publicados em [validacao](../validacao/README.md) são anteriores;
os testes automatizados dos builds foram reexecutados em 23/10.

Para CBO, utilize todas as linhas de `class.csv`: 42 tipos no anterior,
41 no monólito posterior e 26 no serviço. O conjunto reúne os 67 tipos
posteriores: sua média é ponderada pelas quantidades, não a soma das médias.
São 199 dependências CBO somadas no anterior e 343 no conjunto posterior,
resultando em médias 4,738 e 5,119, respectivamente.

## Caminhos locais preservados

Os registros mantêm os caminhos da máquina onde foram produzidos. Não é
necessário recriar essas mesmas pastas para recalcular as estatísticas:

| Prefixo original | Significado para auditoria |
| --- | --- |
| `C:\desenvolvimento\resultados-tcc\recoleta-20261023-02\codigo\pre-migracao` | Fontes do commit anterior identificado acima. |
| `C:\desenvolvimento\resultados-tcc\recoleta-20261023-02\codigo\pos-migracao` | Fontes do commit posterior identificado acima. |
| `...\AppData\Local\Temp\tcc-campanha-*` | Base temporária de cada inicialização, fora dos dados versionados. |
| `C:\Users\Rhuan Elias\Downloads\TCC-*.zip` | Pacotes históricos agora disponíveis em [validacao/pacotes](../validacao/pacotes/), com os hashes originais. |
| Caminhos `target` em logs e `artefatos.json` | Artefatos identificados durante a execução; binários de build não são versionados. |

Os caminhos CK apontam para arquivos relativos aos snapshots correspondentes.
A mudança de localização para publicar os registros não altera seus bytes.
O inventário publicado cobre somente a seleção atual; o inventário da execução
completa, que também inclui pilotos, permanece no acervo original.

## Histórico

As campanhas de 20/10 foram substituídas na análise final e retiradas desta
árvore para manter uma única seleção de resultados. Permanecem disponíveis no
commit [`0d83263d38860c5b271f10a3f668c7b78fba497a`](https://github.com/Rhuan000/tcc-legado/tree/0d83263d38860c5b271f10a3f668c7b78fba497a).
Não foram combinadas observações das duas datas.
