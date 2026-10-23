# Experimento de migração incremental da gestão de empréstimos

Repositório de apoio ao TCC sobre a extração da gestão de empréstimos de um
monólito Java para um microsserviço Quarkus, com Strangler Fig e
Anti-Corruption Layer. O sistema foi construído para representar características
de aplicações legadas no ambiente do estudo.

Os resultados adotados no TCC são os da **recoleta de 23/10/2026**: 110
observações de tempo e três análises estáticas CK. Para começar a auditoria,
consulte o [índice dos dados](metricas/README.md), o
[resumo estatístico](metricas/resumo-estatistico.csv) e o
[inventário de integridade](metricas/integridade-publicada.json).

## Navegação

| Caminho | Conteúdo |
| --- | --- |
| [metricas](metricas/README.md) | Campanhas finais, logs, manifestos e contexto da recoleta. |
| [medicoes](medicoes/README.md) | Três coletores exatos e procedimento para repetir a coleta manualmente. |
| [validacao](validacao/README.md) | Pacotes dos testes funcionais, rastreabilidade, massa e roteiros. |
| [monolito-biblioteca](monolito-biblioteca/README.md) | Interface, cadastros, estoque e integração no estado posterior. |
| [tcc-moderno/emprestimo-service](tcc-moderno/emprestimo-service/README.md) | API, regras, persistência e processamento de empréstimos. |
| [migracoes](migracoes/001-comandos-emprestimo.sql) | SQL da migração para o estado posterior. |
| [docs/tcc/DIARIO-DE-BORDO.md](docs/tcc/DIARIO-DE-BORDO.md) | Decisões e registros do desenvolvimento. |
| [ROTEIRO-TCC.md](ROTEIRO-TCC.md) | Percurso histórico da implementação. |

## Código efetivamente medido

| Estado | Commit |
| --- | --- |
| Legado anterior | `aa55edd88c412725c84c10bff92e88a503e39fcb` |
| Legado posterior e serviço | `45ff20b3d68ff484143a7b28e35b7c1d93fc361b` |

As séries suplementares sem testes utilizam esses mesmos commits. O serviço
pertence ao mesmo snapshot posterior do monólito. Commits posteriores publicam
dados e documentação; a revisão atual da `main` não substitui a identificação
do código medido.

## Execução e interpretação

O monólito utiliza Java 8 e JBoss EAP 7.4; o serviço e o CK utilizam Java 21.
Os registros identificam Maven 3.9.11 e PostgreSQL 14. O protocolo detalhado,
os hashes dos instrumentos e os comandos estão em [medicoes](medicoes/README.md).

O [passo a passo de coleta](medicoes/README.md) mostra como preparar duas cópias
dos commits, configurar o banco descartável e as integrações, executar as cinco
etapas da campanha principal e coletar os builds suplementares. As chamadas
utilizam diretamente os três scripts versionados.

As suítes automatizadas foram reexecutadas durante os builds. Os testes
funcionais manuais são evidências anteriores dos mesmos snapshots; seus casos
não foram reexecutados na recoleta. Inicialização significa disponibilidade HTTP
dos endpoints especificados, e o build com testes inclui suítes diferentes
entre os projetos. Esses limites fazem parte da interpretação dos resultados.

## Preservação e histórico

Os 307 arquivos brutos das campanhas finais foram copiados sem alterar bytes,
observações, caminhos locais ou hashes. Atributos Git preservam os bytes dos
coletores e dos registros. O inventário publicado cobre apenas o acervo
selecionado; pilotos, cópias de código, bundles, builds, caches e dumps locais
não integram a árvore de evidências.

As campanhas anteriores de 20/10 permanecem consultáveis no commit
[`0d83263d38860c5b271f10a3f668c7b78fba497a`](https://github.com/Rhuan000/tcc-legado/tree/0d83263d38860c5b271f10a3f668c7b78fba497a).
O acervo anterior à primeira limpeza permanece no commit
[`58a67c39281c54424b8ca5524d05135f58ed235a`](https://github.com/Rhuan000/tcc-legado/tree/58a67c39281c54424b8ca5524d05135f58ed235a).
