# Roteiro de execução do TCC

## Objetivo do estudo

Avaliar os efeitos da extração incremental da gestão de empréstimos de um
monólito Java EE para um microsserviço Quarkus, utilizando o padrão Strangler
Fig. Serão observados CBO, tempo de build e tempo de inicialização, sem assumir
previamente que todas as métricas melhorarão.

Trata-se de um sistema demonstrativo com características legadas construídas
para o estudo. Os resultados descrevem a modernização nesse ambiente, incluindo
mudanças de framework e runtime; não isolam o efeito causal dos microsserviços.
CBO caracteriza dependências estáticas entre tipos, não todo o acoplamento
distribuído. A comparação não estabelece superioridade sobre um monólito modular.

O cálculo de multa é a primeira fatia da migração, e não o limite final do
microsserviço.

## Fronteira adotada

O `emprestimo-service` deverá assumir progressivamente:

- regras de prazo, dias úteis, feriados e multa;
- criação, consulta e devolução de empréstimos;
- persistência dos empréstimos;
- identificação e atualização periódica de multas em atraso;
- propriedade lógica das tabelas de empréstimo.

Livros, usuários, autenticação e interface Struts permanecem no monólito. O
banco poderá continuar na mesma instância PostgreSQL, mas, ao final, o monólito
não deverá acessar diretamente as tabelas de empréstimo.

## Estado 0 — congelar a linha de base

1. Revisar e versionar o endpoint de saúde usado para detectar prontidão.
2. Versionar os scripts de coleta, os CSVs brutos, os metadados e o protocolo.
3. Registrar commit, hash do código, JDK, Maven, JBoss, CK e características da
   máquina.
4. Manter a planilha apenas como resultado derivado; os CSVs são a fonte
   primária.
5. Criar uma tag como `experimento/pre-migracao` depois que o estado estiver
   limpo e reproduzível.
6. Atribuir um identificador único a cada campanha e recusar a reutilização de
   diretórios que já contenham resultados.
7. Executar eventual recoleta em worktree do snapshot pré-migração, mantendo os
   CSVs históricos separados e sem reclassificá-los como dados da nova rodada.

Critério de saída: dez medições válidas de build e inicialização, uma coleta de
CBO e nenhuma alteração de produção não documentada.

Commit sugerido: `chore: registra linha de base e protocolo de medição`.

## Estado 1 — redefinir o recorte arquitetural

1. Renomear `multa-service` para `emprestimo-service`.
2. Documentar a decisão de extrair empréstimos porque o domínio concentra as
   classes de maior CBO e representa uma capacidade central do sistema.
3. Definir contratos HTTP sem compartilhar classes Java entre as aplicações.
4. Definir claramente o que pertence ao monólito e ao microsserviço.
5. Manter os testes de multa existentes como testes de caracterização da
   primeira regra migrada.

Critério de saída: projeto renomeado, fronteira documentada e serviço ainda sem
alteração no comportamento do monólito.

Commit sugerido: `refactor: amplia recorte para o dominio de emprestimos`.

## Estado 2 — extrair políticas de empréstimo

1. Implementar o cálculo de multa até os testes existentes passarem.
   Concluído: `MultaService.calcularMulta` implementado e 40 testes de cálculo
   aprovados, além do teste de disponibilidade HTTP (41 no total). Validação:
   `mvnw.cmd -B test` em `tcc-moderno/emprestimo-service`. O endpoint HTTP ainda
   verifica apenas disponibilidade; a exposição do cálculo está no item 4.
2. Criar testes de caracterização para prazo por tipo de usuário, dias úteis,
   fins de semana, feriados e virada de ano.
   Concluído na etapa RED: `PrazoServiceTest` contém 26 testes derivados da
   leitura do legado, com datas e resultados fixos. Contrato proposto:
   `LocalDate PrazoService.calcularDataPrevista(LocalDate dataEmprestimo,
   String tipoUsuario, List<LocalDate> feriados)`. Preserva-se a consulta de
   feriados somente do ano de início do empréstimo, sem buscar o próximo ano
   ao atravessar dezembro. Essa consulta será responsabilidade da integração;
   os testes do núcleo fornecem a lista explicitamente.
   Validação com `mvnw.cmd -B test`: 67 testes executados, 41 existentes
   aprovados e 26 novos falhando pela ausência de `PrazoService`, sem testes
   ignorados. O build permanece vermelho intencionalmente até a implementação
   do cálculo no item 3. Esses testes não são uma comparação executada entre
   as duas aplicações.
3. Migrar cálculo de prazo, calendário, cliente de feriados e cache.
   Cálculo de prazo concluído: `PrazoService` preserva os prazos por tipo de
   usuário e a contagem de dias úteis do legado, recebendo data e feriados de
   forma explícita. Os 26 testes de caracterização passaram a chamar o contrato
   diretamente. Validação com `mvnw.cmd -B clean test`: 67 testes executados,
   sem falhas, erros ou testes ignorados.
   Calendário, cliente e cache concluídos: `FeriadoClient` declara a integração
   com a BrasilAPI, com timeouts de conexão e leitura de três segundos, e
   `CalendarioService` mantém listas imutáveis por ano. Falhas não são
   armazenadas, permitindo nova tentativa na próxima chamada. Quatro testes
   cobrem reutilização do cache, separação por ano, nova tentativa após falha e
   remoção de duplicatas. O contrato HTTP da BrasilAPI também foi validado com
   WireMock: três testes exercitam a rota documentada `/feriados/v1/{ano}`, a
   desserialização de data e demais campos, uma lista vazia e a propagação de
   erro HTTP. Essa validação identificou e corrigiu o prefixo indevido `/api`
   que existia no cliente. Validação com `mvnw.cmd -B test`: 74 testes
   aprovados, sem falhas, erros ou testes ignorados. A exposição dos contratos
   HTTP próprios do microsserviço permanece no item 4.
4. Expor contratos para cálculo de prazo e multa.
   Concluído: `POST /prazos/calcular` e `POST /multas/calcular` recebem contratos
   JSON próprios, validam os campos obrigatórios, consultam o calendário no ano
   preservado do legado e delegam as regras aos serviços já caracterizados.
   Validação com `mvnw.cmd -B test`: 78 testes aprovados.
5. Criar no monólito uma Anti-Corruption Layer que converta seus modelos em
   requisições HTTP.
   Implementação concluída: a ACL converte `Date` e o tipo do usuário para os
   contratos de prazo e multa e traduz as respostas para os tipos esperados
   pelo legado. Criação, devolução e atualização periódica continuam sendo
   orquestradas pelo monólito, mas seus cálculos usam o microsserviço.
6. Manter persistência, criação e devolução no monólito nesta etapa.
7. Prever timeout, indisponibilidade e respostas inválidas do serviço.
   Concluído para as políticas: prazo e multa limitam conexão, espera por
   conexão e leitura a três segundos e convertem falhas HTTP ou respostas
   inválidas em erro da integração, sem fallback silencioso.

Critério de saída: o monólito usa o microsserviço para prazo e multa, com testes
de contrato e comportamento preservado.

Evidência em 28/09/2026: criação e devolução por Struts/EJB/banco/serviço e o
método de atualização usado pelo job foram exercitados em ambiente isolado.
Registro resumido: `docs/tcc/DIARIO-DE-BORDO.md`, entrada de validação integrada.
O cron automático e timeout de leitura ainda não foram exercitados. A evidência
não é uma comparação executada entre as duas versões nem equivalência irrestrita.

Esse é o estado intermediário da arquitetura. Após estabilizá-lo, registrar os
contratos, commits, testes e fluxos validados, sem realizar uma campanha
quantitativa. As métricas serão comparadas somente entre os estados
pré-migração e pós-migração.

## Estado 3 — migrar consultas e persistência

Implementados modelo de leitura, repositório JDBC e endpoints de listagem,
busca por ID e atrasados. As consultas de `EmprestimoService`, incluindo as
leituras usadas pela devolução e pelo job de multas, passam pela ACL.
A consulta de destaques permanece pendente; a propriedade exclusiva dos dados
depende também da migração das escritas no Estado 4.

1. Implementar entidade e repositório de empréstimos no novo serviço.
2. Migrar busca por identificador, listagem e consulta de atrasados.
3. Redirecionar as consultas de `EmprestimoAction` à API pela ACL. Nesta fatia,
   o EJB permanece como fachada, delegando ao serviço e à ACL gerenciada por CDI.
4. Redirecionar as leituras do monólito para a API, mantendo temporariamente as
   escritas legadas necessárias à criação e devolução até o Estado 4.
5. Manter o banco na mesma instância e preparar a propriedade lógica exclusiva
   da tabela pelo microsserviço após a migração dos comandos.

Critério de saída: consultas e seu acesso à persistência pertencem ao serviço;
o monólito atua como cliente para leituras, com as escritas remanescentes
explicitamente transitórias até o Estado 4.

## Estado 4 — migrar criação, devolução e job

1. Migrar criação e devolução de empréstimos.
2. Definir contratos para validar usuário, consultar livro e alterar estoque.
3. Definir comportamento de compensação quando uma operação distribuída falhar.
4. Migrar o processamento periódico de multas para o agendador do serviço.
5. Remover `EmprestimoService`, `EmprestimoDAO`, EJB e interface EJB quando não
   houver consumidores remanescentes.
6. Manter temporariamente o `EmprestimoAction` como fachada Struts que chama a
   API, caracterizando o Strangler Fig.

Critério de saída: toda a capacidade de empréstimos é atendida pelo novo
serviço, e o monólito não possui lógica nem acesso direto aos seus dados.

## Estado 5 — fechar o experimento

Compatibilidade: os records foram substituídos por classes convencionais,
reconhecidas pelo CK fixado. Validar instrumento e coletores dos três escopos antes da coleta.
Se mudar o CK, reanalisar os dois snapshots com o mesmo instrumento e preservar
os resultados anteriores. A escolha por compatibilidade deve ser declarada;
não adaptar o código de produção para obter valores menores de CBO.

1. Executar dez builds limpos do monólito residual.
2. Executar dez builds limpos do microsserviço.
3. Registrar também o tempo total sequencial dos dois builds.
4. Executar dez inicializações de cada aplicação.
5. Medir o tempo até o sistema completo estar pronto, verificando os dois
   endpoints de saúde.
6. Executar o CK uma vez em cada base de código com a mesma versão da ferramenta.
7. Comparar CBO do monólito, do microsserviço e do conjunto, sem somar médias de
   maneira indiscriminada.
8. Registrar falhas, valores atípicos e decisões de exclusão sem apagar dados
   brutos.
9. Criar uma tag como `experimento/pos-migracao`.

## Estrutura sugerida do texto

1. Introdução, problema e objetivos.
2. Fundamentação: monólitos, microsserviços, Strangler Fig, ACL e métricas.
3. Caracterização do sistema legado e justificativa do domínio escolhido.
4. Método experimental, ambiente, estados, repetições e ameaças à validade.
5. Execução incremental da migração.
6. Resultados de CBO, build e inicialização.
7. Discussão dos ganhos e custos, incluindo resultados sem melhora.
8. Limitações: banco fisicamente compartilhado, sistema acadêmico e tamanho da
   amostra.
9. Conclusão e trabalhos futuros, incluindo separação física do banco.

## Regras para preservar a validade

- Não alterar o legado depois de medir um estado sem repetir suas medições.
- Não escolher o tamanho da extração para forçar melhora nas métricas.
- Usar a mesma máquina, ferramentas e condições em todos os estados.
- Separar resultados do monólito, do serviço e do sistema completo.
- Não tratar banco fisicamente compartilhado como independência total.
- Criar um commit ou uma tag para cada estado efetivamente medido.
