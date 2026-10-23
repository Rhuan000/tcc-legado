# Dados utilizados no TCC

Este diretório reúne as duas campanhas finais e as três séries suplementares
de 20/10/2026 utilizadas nas tabelas do trabalho: 110 observações de tempo e
três análises estáticas CK. Os CSV, os manifestos e os logs dessas campanhas
foram preservados como registros brutos.

## Índice das campanhas

| Estado | Diretório | Conteúdo |
| --- | --- | --- |
| Anterior | [final-v2-pre-20261020-02](pre-migracao/final-v2-pre-20261020-02/) | 10 builds com testes, 10 inicializações e CBO do monólito. |
| Posterior | [final-v2-pos-20261020-01](pos-migracao/final-v2-pos-20261020-01/) | 10 builds com testes e 10 inicializações de cada escopo: monólito, serviço e conjunto; CBO dos dois projetos. |
| Anterior | [suplementar-monolito-sem-testes-20261020-01](pre-migracao/suplementar-monolito-sem-testes-20261020-01/) | 10 builds do monólito sem compilação ou execução de testes. |
| Posterior | [suplementar-monolito-sem-testes-20261020-01](pos-migracao/suplementar-monolito-sem-testes-20261020-01/) | 10 builds do legado remanescente sem compilação ou execução de testes. |
| Posterior | [suplementar-servico-sem-testes-20261020-03](pos-migracao/suplementar-servico-sem-testes-20261020-03/) | 10 builds do serviço sem compilação ou execução de testes. |

São 40 observações de build com testes, 40 de inicialização e 30 de build sem
testes. As condições de build são apresentadas separadamente. A campanha
principal mede o tempo de parede do conjunto posterior; não há série medida
para o conjunto sem testes.

## Identificação do código

- Campanha principal anterior: `aa55edd88c412725c84c10bff92e88a503e39fcb`.
- Campanha principal posterior: `45ff20b3d68ff484143a7b28e35b7c1d93fc361b`.
- Suplementar anterior: o mesmo commit `aa55edd88c412725c84c10bff92e88a503e39fcb`.
- Suplementares posteriores: `b88d70f19dcd74b2092683c87301aadfbb58c153`.

Os dois projetos posteriores têm as mesmas árvores Git nos commits `45ff20b`
e `b88d70f`: `cbd8f5b37e2cd3f45a0f5b600d54b3548cb46901` no monólito e
`72435186792c11d3ddde2e7ad17f55858ee5aa1b` no serviço. Assim, a revisão do
repositório mudou entre as publicações, mas o código dos projetos medidos se
manteve.

## Onde conferir cada resultado

Nas duas campanhas principais:

- `manifesto.json`: estado, revisão, ambiente, parâmetros e identificadores da
  execução.
- `validacao-funcional.json`: vínculo com os pacotes e registros dos testes
  funcionais, incluindo seus limites.
- `<escopo>/build/medicoes.csv`: dez tempos de build em `duracao_ms`.
- `<escopo>/inicializacao/medicoes.csv`: dez tempos de inicialização em
  `duracao_ms`.
- `<escopo>/build/artefatos.json` e
  `<escopo>/inicializacao/artefatos.json`: identificação dos artefatos.
- `<projeto>/cbo/class.csv`: CBO por tipo; os demais CSV e `metadados.json`
  mantêm os resultados complementares e o contexto do CK.
- Arquivos `*.stdout.log` e `*.stderr.log`: saída de cada build, inicialização
  e análise estática, junto da medição correspondente.

Nas séries suplementares, cada pasta `<projeto>/build-sem-testes/` contém
`medicoes.csv`, `manifesto.json` e os logs individuais das dez execuções.

Os tempos em segundos são obtidos dividindo `duracao_ms` por 1.000. Para CBO,
use todas as linhas de `class.csv`: 42 tipos no anterior, 41 no monólito
posterior e 26 no serviço. A distribuição conjunta posterior reúne os 67 tipos;
a média é ponderada por essas quantidades, não a soma das médias dos projetos.

O build principal executa `mvn -B -o clean package`. Os logs registram suítes
distintas: 33 testes no anterior, 32 no monólito posterior com três ignorados
(29 executados) e 165 no serviço. O suplementar usa
`mvn -B -o clean package -Dmaven.test.skip=true`, sem compilar ou executar
testes. A medida de inicialização termina na disponibilidade HTTP dos endpoints
definidos no protocolo; os pacotes funcionais documentam outros comportamentos.

## Instrumentos e acervo histórico

O procedimento e os comandos para novas execuções estão em
[medicoes/README.md](../medicoes/README.md). Os scripts atuais não alteram os
registros das campanhas publicadas.

Pilotos e coletas preliminares, inclusive a recoleta de setembro e a planilha
derivada antiga, não integram as tabelas finais. Foram retirados da árvore atual
para manter este índice restrito aos dados utilizados. O conteúdo anterior,
incluindo o protocolo V1, permanece no commit
[`58a67c39281c54424b8ca5524d05135f58ed235a`](https://github.com/Rhuan000/tcc-legado/tree/58a67c39281c54424b8ca5524d05135f58ed235a).
Os registros do processo continuam no
[diário de bordo](../docs/tcc/DIARIO-DE-BORDO.md).
