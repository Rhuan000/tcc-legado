# Diário de bordo da migração de empréstimos

Registro iniciado em 24/09/2026. Datas de commits seguem o fuso registrado no Git, UTC-03:00. Entradas anteriores à criação deste arquivo são reconstruções retrospectivas a partir das evidências identificadas, não anotações contemporâneas aos acontecimentos.

## Finalidade e regras

Documentar decisões e obstáculos da extração incremental da gestão de empréstimos, relacionando-os às implementações, aos testes e às medições de CBO, build e inicialização. O diário apoia a análise qualitativa; não substitui dados brutos nem demonstra causalidade por si só.

Cada entrada distingue fato observado, decisão e interpretação. Atividades planejadas não são resultados. Registros sem commit devem ser identificados. Não corrigir CSVs históricos para fazê-los coincidir com uma narrativa posterior.

## Histórico verificável

### 19/09/2026 Correção do processamento de multas no legado

- **Registro:** retrospectivo, escrito em 24/09/2026.
- **Evidência:** commit `2b8bd8b`, “legado: corrigindo comportamento do Job para multa.”
- **Fato:** o histórico contém uma correção anterior à coleta da linha de base.
- **Consequência:** a referência experimental deve ser o estado efetivamente medido, não o primeiro monólito executável.
- **Limite:** esta entrada não constitui validação funcional independente das mudanças do commit.

### 20/09/2026 Caracterização do cálculo de multa

- **Registro:** retrospectivo, escrito em 24/09/2026.
- **Evidência:** commit `8681c13`, “test: define contrato e cenários do cálculo de multas”.
- **Decisão:** definir cenários com entradas e saídas fixas derivados da leitura do legado.
- **Escopo:** dias úteis, feriados, tarifas por tipo, virada de ano e comportamentos particulares do código.
- **Limite:** os testes especificam expectativas; não comparam duas aplicações em execução.

### 21/09/2026 Coleta da linha de base

- **Registro:** retrospectivo, escrito em 24/09/2026.
- **Evidências:** CSVs em `metricas/pre-migracao/build/`, `inicializacao/` e `cbo/`, com metadados e logs.
- **Fato:** dez observações bem-sucedidas de build, dez de inicialização e uma análise de CBO com 40 classes.
- **Conferência em 24/09:** build com média de 10,705 s e mediana de 10,090 s; inicialização com média de 5,026 s e mediana de 4,873 s; CBO médio de 4,625. Estatísticas recalculadas dos CSVs, sem nova execução dos coletores.
- **Ambiente registrado:** JDK 1.8.0_201 para o monólito, Maven 3.9.11, JBoss 7.4.0.GA; CK 0.7.1-SNAPSHOT com Java 21.0.8.
- **Pendência:** o metadado registra árvore modificada no POM e no filtro de autenticação. A identificação da configuração medida exige considerar essas alterações além do commit-base.

### 22/09/2026 Versionamento e delimitação arquitetural

- **Registro:** retrospectivo, escrito em 24/09/2026.
- **Evidências:** `3b1ff8d` libera o health check; `c6e2389` registra a linha de base e o protocolo; `5c88e4f` reorganiza o projeto como `emprestimo-service`; `c8ed12f` define o roteiro.
- **Decisão:** gestão de empréstimos como recorte, com multa, prazo e calendário como primeira etapa.
- **Justificativa:** estudar uma capacidade com ciclo de vida e responsabilidade pelos dados identificáveis. Dependências com livros e usuários exigem contratos explícitos.
- **Restrição:** a extensão do recorte não deve ser escolhida para forçar redução de CBO ou dos tempos.
- **Estado final planejado:** propriedade lógica dos empréstimos no serviço; livros, usuários, autenticação e interface no monólito.
- **Observação:** hashes do plano PDF de apoio diferem dos encontrados no histórico atual. Usar os identificadores acima ao referenciar este repositório, distinguindo as versões documentais.

### 24/09/2026 Implementação do núcleo de multa

- **Evidências:** commit `c6324d7`, `MultaService.java`, `MultaServiceTest.java`, execução de `mvnw.cmd -B test` registrada na sessão e relatórios locais.
- **Resultado observado:** 40 testes de multa e um HTTP aprovados, sem falhas ou testes ignorados após a implementação.
- **Decisão:** preservar tarifas e contagem do legado; fornecer datas e feriados explicitamente ao núcleo.
- **Decisão de teste:** substituir reflexão temporária por chamada direta ao contrato implementado.
- **Limite:** o resource HTTP somente confirma disponibilidade. A multa não foi integrada funcionalmente ao monólito.
- **Ocorrência técnica:** a execução inicial encontrou acesso negado em artefatos Maven existentes; a execução com acesso autorizado permitiu verificar RED e GREEN. Essa ocorrência não integra as medições de desempenho.

### 24/09/2026 Caracterização do prazo

- **Evidências:** `PrazoServiceTest.java`, atualização de `ROTEIRO-TCC.md` e relatórios locais.
- **Situação de versão na inspeção:** teste novo e alteração do roteiro ainda sem commit.
- **Contrato proposto:** `LocalDate PrazoService.calcularDataPrevista(LocalDate dataEmprestimo, String tipoUsuario, List<LocalDate> feriados)`.
- **Resultado observado:** 67 testes executados; 41 existentes passaram e 26 novos falharam pela ausência de `PrazoService`. Nenhum teste ignorado.
- **Decisão:** sete dias úteis para aluno e tipos não reconhecidos, quatorze para professor e dez para bolsista; excluir o dia de início.
- **Calendário:** manter feriados do ano de início para prazo e do ano de vencimento para multa. Não introduzir silenciosamente correção da virada de ano.
- **Próxima atividade técnica:** implementar prazo e, posteriormente, integrar calendário e contratos HTTP.

### 24/09/2026 Alinhamento do texto acadêmico

- **Evidências:** DOCX `TCC_II_RHUAN_ELIAS_REESTRUTURADO.docx` em Downloads, roteiro, protocolo e código.
- **Problema:** o texto descreve extração apenas de multa, alterna três e dez repetições e mistura CKJM, SonarQube e CK. Promete isolamento causal maior que o desenho permite.
- **Decisão:** manter a pergunta e preparar textos revisados para objetivos, recorte, metodologia, limitações e desenvolvimento.
- **Produto:** `docs/tcc/REVISAO-TCC-PARA-CHATGPT.md` contém textos substitutos e instruções de aplicação. O DOCX original não foi alterado.
- **Limite:** referências bibliográficas e citações diretas não foram conferidas nas obras originais; a revisão identifica essa pendência.

### 24/09/2026 Pendências identificadas na revisão

**Ferramenta de build.** O Wrapper do microsserviço aponta para Maven 3.9.16, enquanto os CSVs do monólito registram 3.9.11. É necessário definir e registrar a ferramenta usada nas próximas comparações. Nenhuma versão foi modificada nesta revisão.

**Consumidor indireto.** `LivroDestaqueDAO.buscarTopLivrosMes` faz `JOIN` com `emprestimo`. O corte final precisa adaptar essa leitura, além dos consumidores de `EmprestimoDAO`. O código de produção permanece inalterado.

**Coletores.** Os scripts apontam para o monólito; `Estado` altera a saída, sem selecionar automaticamente outra aplicação. Medições do serviço e do conjunto exigem adaptação, inclusive dos hashes.

**Inicialização.** `HealthCheckServlet` retorna HTTP 200 sem consultar banco ou BrasilAPI. O tempo representa disponibilidade HTTP, não verificação integral das operações. A espera de 100 ms não é limite absoluto de erro: as próprias requisições consomem tempo.

**Hash da linha de base.** O agregado registrado é `f996a5c525e18b834e88d90d2799e2e76ea6baaa97c80062de0bee7ca6f82d16`; o recalculado em 24/09 pelo procedimento de `registrar-estado.ps1` é `d33c5de7eeb81b426c2d60b8fe897ce5c462a2bae8a546816b417e27a9439e6b`, ambos para 69 arquivos. A causa não foi determinada. Diferenças de bytes, inclusive finais de linha, precisam ser investigadas antes de inferir alteração funcional. CSVs preservados. Reconciliação ou recoleta documentada pendente.

### 26/09/2026 Implementação do prazo e do calendário

- **Data de registro:** 26/09/2026.
- **Tipo:** contemporâneo.
- **Estado do experimento:** extração das políticas de empréstimo.
- **Evidências:** commits `845fcfc`, “feat(emprestimo-service): implementa calculo de prazo”, e `3fbb12c`, “feat(emprestimo-service): integra calendario de feriados”.
- **Fato observado:** `PrazoService` foi implementado e os 26 cenários passaram a chamar diretamente seu contrato. `FeriadoClient` e `CalendarioService` foram adicionados com cache por ano e timeouts declarados de três segundos.
- **Decisão:** manter datas e feriados explícitos no núcleo; concentrar consulta externa e cache em uma camada separada.
- **Justificativa:** permitir testes determinísticos das regras e substituir o par global mutável de ano e lista do legado por associações explícitas por ano.
- **Validação e resultado:** `mvnw.cmd -B clean test` executou 71 testes, sem falhas, erros ou testes ignorados. Quatro testes isolados verificam cache por ano, nova tentativa depois de falha e remoção de duplicatas.
- **Limitações e pendências:** os testes do calendário substituem o cliente por uma implementação local e não validam ainda o contrato HTTP ou a desserialização da BrasilAPI. O monólito ainda não chama o serviço; não há comparação executada entre as duas aplicações.

### 26/09/2026 Validação do contrato HTTP da BrasilAPI

- **Data de registro:** 26/09/2026.
- **Tipo:** contemporâneo.
- **Estado do experimento:** extração das políticas de empréstimo.
- **Problema:** os testes existentes validavam cache e calendário com uma implementação Java local do cliente, mas não exercitavam a montagem da requisição HTTP nem a desserialização JSON. A inspeção da documentação oficial também mostrou que a rota correta é `/feriados/v1/{ano}`, enquanto o cliente continha o prefixo indevido `/api`.
- **Decisão:** corrigir a rota e testar o cliente REST real do Quarkus contra um servidor WireMock iniciado em porta dinâmica. O teste não consulta a internet e não altera o monólito.
- **Justificativa:** separar a caracterização das regras de negócio da validação do contrato da dependência externa. O servidor simulado torna o teste determinístico e permite controlar respostas de sucesso, lista vazia e erro HTTP.
- **Referências técnicas:** documentação oficial da BrasilAPI (`https://brasilapi.com.br/docs`) e guia oficial do REST Client do Quarkus (`https://quarkus.io/guides/rest-client`).
- **Validação e resultado:** `mvnw.cmd -B test` executou 74 testes, sem falhas, erros ou testes ignorados. Três testes HTTP verificam a rota exata, a conversão dos campos `date`, `name` e `type`, a lista vazia e a propagação de status 500.
- **Limitações:** o teste verifica o contrato documentado por meio de respostas simuladas; ele não demonstra disponibilidade da BrasilAPI real nem substitui monitoramento da integração em execução.

### 26/09/2026 Consolidação dos dois pontos de coleta quantitativa

- **Data de registro:** 26/09/2026.
- **Tipo:** contemporâneo.
- **Estado do experimento:** revisão do protocolo antes da coleta final.
- **Evidências:** PDF revisado para a banca, `ROTEIRO-TCC.md`, `medicoes/README.md`, validadores dos scripts de coleta e este diário.
- **Problema:** o texto revisado adotava duas campanhas quantitativas, mas o roteiro, o protocolo e os parâmetros dos scripts ainda aceitavam uma campanha intermediária.
- **Decisão:** comparar quantitativamente somente pré-migração e pós-migração. O estado intermediário permanece como marco arquitetural documentado por commits, contratos, testes e fluxos validados.
- **Justificativa:** concentrar a comparação nos extremos do recorte funcional e evitar uma terceira campanha que não integra o desenho metodológico apresentado. A consequência é não quantificar os custos transitórios da coexistência.
- **Validação:** buscas textuais confirmaram que as menções remanescentes ao estado intermediário o tratam como marco arquitetural ou registram explicitamente a ausência de campanha. Os quatro scripts passaram pela análise sintática do PowerShell sem erros e deixaram de aceitar `intermediario` em `ValidateSet`.
- **Pendências:** adaptar os coletores do estado final para medir separadamente monólito residual, microsserviço e conjunto.

### 26/09/2026 Preparação para recoleta rastreável da linha de base

- **Data de registro:** 26/09/2026.
- **Tipo:** contemporâneo.
- **Estado do experimento:** preparação; nenhuma nova observação experimental produzida.
- **Problema:** a campanha histórica registra commit com árvore modificada e um hash físico que não pôde ser reproduzido. A alteração funcional do `AuthFilter` foi posteriormente versionada, mas a identidade byte a byte do POM medido não foi recuperada.
- **Decisão:** preservar integralmente a campanha histórica e preparar uma recoleta em worktree limpo de um snapshot pré-migração, com outro identificador.
- **Implementação preparada:** os coletores passaram a exigir `ColetaId`, recusam diretórios de saída existentes e alterações no POM ou em `src`, aceitam um repositório medido diferente daquele que contém os scripts e registram o hash do próprio coletor. O registro do estado acrescenta a árvore Git do monólito, um hash estável dos blobs versionados, `core.autocrlf` e identificadores da máquina ao hash físico já existente. Os medidores somente executam depois do registro da campanha no mesmo estado, identificador e commit. A inicialização registra o SHA-256 do WAR, e o CBO recusa por padrão um JAR do CK cujo hash seja diferente do fixado no protocolo.
- **Validação:** os quatro scripts passaram pela análise sintática do PowerShell. Testes isolados em diretório temporário confirmaram os campos de identidade do código e do ambiente, a recusa de sobrescrita sem alteração do CSV existente, a exigência do registro prévio da campanha e a rejeição do CK com hash inesperado antes da criação de resultados. Um ensaio completo em cópia Git temporária e limpa executou uma repetição de build, uma inicialização com HTTP 200 e uma análise CK que retornou 40 tipos e os cinco arquivos esperados. O WAR foi identificado por SHA-256. A cópia e os resultados temporários foram removidos; essas execuções são validação dos instrumentos e não integram as amostras do experimento.
- **Pendências:** revisar as alterações, selecionar e marcar o snapshot pré-migração, executar a campanha somente após autorização e atualizar o texto com os novos resultados.

### 26/09/2026 Recoleta rastreável da linha de base

- **Data de registro:** 26/09/2026.
- **Tipo:** contemporâneo.
- **Estado do experimento:** campanha quantitativa pré-migração.
- **Evidências:** diretório `metricas/pre-migracao/recoleta-baseline-20260926/`, código medido no commit `c6e2389` e coletores no commit `d0f91d8`.
- **Procedimento:** o snapshot foi aberto em worktree destacado e limpo. O registro precedeu as medições e fixou commit, árvore Git do monólito, hashes físico e lógico, ambiente e versão do coletor. Em sequência, sem cargas do experimento em paralelo, foram executados dez builds, dez inicializações e uma análise CK.
- **Resultado de build:** dez sucessos; média de 9,489546 s, mediana de 9,094534 s, desvio-padrão amostral de 0,988810 s, mínimo de 9,057714 s e máximo de 12,151952 s.
- **Resultado de inicialização:** dez respostas HTTP 200; média de 5,110341 s, mediana de 4,951688 s, desvio-padrão amostral de 0,582123 s, mínimo de 4,588122 s e máximo de 6,352641 s. Todas as repetições usaram o mesmo WAR, identificado por SHA-256.
- **Resultado de CBO:** 40 tipos, soma 185, média 4,625, mediana 4, desvio-padrão amostral aproximado de 3,628, mínimo 0 e máximo 13. Classe, tipo e CBO coincidem com os 40 registros da campanha histórica.
- **Auditoria:** 10/10 linhas de build e 10/10 linhas de inicialização foram aprovadas; os commits e hashes internos são únicos e coerentes; há 20 logs de build, 40 logs de inicialização e os cinco arquivos esperados do CK. Não foram encontradas mensagens `[ERROR]` nos logs Maven.
- **Decisão:** adotar esta campanha identificada e reproduzível como linha de base para a comparação final. Preservar a campanha histórica como registro anterior, sem reclassificar nem sobrescrever seus arquivos.
- **Limite:** a prontidão de inicialização continua definida como primeira resposta HTTP 200 do endpoint `/health`; ela não demonstra disponibilidade funcional do banco ou de integrações externas. Diferenças temporais entre campanhas não são efeito da migração, pois ambas representam o estado pré-migração.
- **Pendências:** revisar e versionar os novos registros, criar a referência Git da linha de base somente após aprovação e atualizar o texto acadêmico com a distinção entre campanha histórica e recoleta.

### 28/09/2026 Primeira integração do monólito pela ACL

- **Data de registro:** 28/09/2026.
- **Tipo:** contemporâneo.
- **Estado do experimento:** extração das políticas de empréstimo.
- **Fato observado:** o monólito calculava o prazo e consultava feriados diretamente dentro de `EmprestimoService`; o microsserviço já expunha o contrato funcional de prazo.
- **Decisão:** criar uma Anti-Corruption Layer de produção no monólito para traduzir `Date` e o tipo do usuário para JSON, consumir `POST /prazos/calcular` e converter `dataPrevista` novamente para `Date`. O cálculo local de prazo foi removido, enquanto multa, persistência, criação e devolução permanecem no monólito nesta fatia.
- **Configuração:** o serviço usa a porta local 8081. A URL pode ser sobrescrita pela propriedade de sistema `emprestimo.service.url` ou pela variável `EMPRESTIMO_SERVICE_URL`. Conexão, espera por conexão e leitura têm limite de três segundos; erro HTTP, indisponibilidade ou resposta inválida interrompem a operação, sem fallback silencioso.
- **Validação e resultado:** o monólito gerou o WAR com sucesso usando o JDK 8 definido no protocolo; não foram adicionadas classes de teste ao legado. A suíte do microsserviço executou 78 testes, sem falhas, erros ou testes ignorados.
- **Decisão de medição:** os estados intermediários permanecem documentados por código, commits e evidências funcionais, sem campanha quantitativa própria. A comparação principal continuará entre a linha de base pré-migração e o estado final pós-migração.
- **Limitações e pendências:** a validação ponta a ponta com as duas aplicações em execução ainda deve ser registrada. A ACL de multa e a substituição do cálculo local correspondente permanecem para a próxima fatia.

### 28/09/2026 Conclusão da ACL das políticas de empréstimo

- **Data de registro:** 28/09/2026.
- **Tipo:** contemporâneo.
- **Estado do experimento:** extração das políticas de empréstimo.
- **Decisão:** ampliar a mesma ACL para `POST /multas/calcular` e substituir o cálculo local tanto na devolução quanto na atualização periódica. A comunicação HTTP comum permaneceu centralizada, sem criar DTOs compartilhados, interfaces sem segunda implementação ou classes de teste no monólito.
- **Remoções:** `EmprestimoService` deixou de conhecer calendário, dias úteis, cache de feriados e cliente da BrasilAPI. O `FeriadoClient` legado foi removido; `CacheGlobal` foi preservado porque ainda atende livros, mas perdeu os membros exclusivos de feriados.
- **Validação e resultado:** o WAR foi gerado com sucesso pelo JDK 8 do protocolo, compilando 40 classes de produção. Os 78 testes do microsserviço passaram sem falhas, erros ou testes ignorados. Uma verificação temporária, fora da árvore de fontes, executou a ACL contra um servidor HTTP local e confirmou os JSONs e as respostas de prazo (`2026-01-12`) e multa (`4.0`).
- **Acoplamento:** a dependência remota não foi eliminada; foi concentrada em uma fronteira explícita. A redução ou o aumento quantitativo de CBO não é inferido desta alteração e será verificado somente na campanha pós-migração.
- **Limitações e pendências:** o smoke test valida a tradução HTTP da ACL, mas não substitui uma execução ponta a ponta pelo Struts, EJB, banco e microsserviço. Essa execução ainda é necessária antes de encerrar formalmente o Estado 2.

### 28/09/2026 Revisão crítica e validação integrada das políticas

- **Tipo:** contemporâneo; alterações ainda sem commit, sobre `374e7e8`.
- **Correções:** conversão de `java.sql.Date` por `toLocalDate()`, pois `toInstant()` falha nesse tipo; rejeição de multa que não seja número JSON finito e não negativo.
- **Validação interna:** build limpo do WAR no JDK 8 e 78 testes do serviço aprovados. Em ambiente isolado, foram conferidos criação para os três tipos de usuário, devolução, multas, estoque e atualização manual pelo EJB usado pelo job. Nos cenários de indisponibilidade do serviço, criação e devolução não foram persistidas. Nenhuma classe de teste foi adicionada ao monólito; os auxiliares temporários não integram o repositório.
- **Limites:** calendário sintético; sem comparação executada contra a baseline. Disparo automático do Quartz, timeout de leitura e falhas intermediárias de escrita ainda não validados.
- **Métricas:** identificada subcontagem de dependências de records no CK; ressalva e condição para a coleta final registradas em `medicoes/README.md`. Mantidas duas campanhas quantitativas.

### 28/09/2026 Primeira fatia das consultas de empréstimos

- **Implementação:** modelo de leitura, repositório JDBC com pool e API para listagem, busca por ID e atrasados. Consultas derivadas do DAO legado; multa SQL nula continua representada como zero. Falhas SQL passam a resultar em erro HTTP, em vez de lista vazia ou ausência aparente.
- **Validação:** `mvnw.cmd -B package` no JDK 21 concluiu com 87 testes aprovados, incluindo nove novos testes HTTP com PostgreSQL 14 isolado. Cobertura de ordenação, datas, multa, devolvidos, vencimento hoje, futuros, ausência, ID inválido, lista vazia e falha SQL. O esquema reduzido de testes não valida as chaves estrangeiras entre domínios.
- **Limites:** leituras e escritas do monólito permanecem locais; redirecionamento pela ACL e consulta de destaques ainda pendentes. Nenhuma campanha quantitativa executada. A extensão JDBC acrescenta readiness do banco, cujo escopo deverá ser considerado na definição do endpoint da coleta final.

### 29/09/2026 Compatibilidade dos modelos com o CK

- **Decisão:** substituir os seis records do microsserviço por classes imutáveis com getters e igualdade por valor, preservando contratos JSON e validações. Escolha motivada pela limitação do instrumento, sem expectativa de reduzir CBO.
- **Verificação:** `mvnw.cmd -B package` concluiu com 87 testes aprovados. O CK com hash fixado reconheceu as seis classes e seus campos; uma amostra de controle com uma dependência obteve CBO 1. Resultados temporários em `target`, sem nova campanha quantitativa ou alteração da baseline.
- **Banco:** o `CREATE TABLE` pertence exclusivamente ao preparo do PostgreSQL isolado dos testes. O script não está no JAR de produção; a aplicação consulta a tabela existente.

### 29/09/2026 Consultas do monólito pela ACL

- **Implementação:** listagem, busca por ID e atrasados redirecionados pela ACL, incluindo leituras da devolução e do job de multas. Removidas as três consultas de `EmprestimoDAO`; escritas preservadas. O EJB continua como fachada para Struts. A tela de detalhes passa a encaminhar falhas de consulta à página de erro.
- **Validação interna:** WAR gerado com JDK 8. Verificação temporária da ACL com 43 checagens de tradução, rotas, contratos inválidos, erros HTTP, indisponibilidade e regressão dos POSTs de prazo e multa. Integração da ACL compilada com Quarkus e PostgreSQL isolado confirmou listagem, ordenação, multa, datas de devolução, ausência e filtro de atrasados. Auxiliares mantidos apenas em `target`; nenhuma suíte adicionada ao legado.
- **Limites:** não foram exercitados nesta rodada o fluxo completo das telas em JBoss nem o agendamento automático. A consulta de destaques ainda acessa empréstimos por `JOIN`; o Estado 3 permanece em andamento. Sem nova campanha quantitativa.

### 29/09/2026 Retirada do acesso a empréstimos na consulta de destaques

- **Fronteira:** o serviço fornece apenas IDs dos livros e contagens mensais. Dados de livros, seleção de cinco destaques e sua atualização permanecem no monólito. Removido o `JOIN` de `LivroDestaqueDAO`; auditoria das fontes Java não encontrou leituras SQL remanescentes de empréstimos no legado.
- **Comportamento:** mês definido pelo banco e contagem incluindo devolvidos, como antes. Empates agora usam ID crescente, antes sem ordem definida. O job consulta a API e carrega os livros antes de apagar destaques; falhas nessa fase preservam os registros existentes.
- **Validação:** microsserviço empacotado com 93 testes aprovados (seis novos) e WAR compilado com JDK 8. Execução manual do job com API real e PostgreSQL isolado reproduziu o ranking esperado do SQL legado nos dados do ensaio e criou os cinco destaques com dados locais. Verificação temporária totalizou 22 checagens, incluindo preservação dos registros sob erro HTTP, JSON inválido, indisponibilidade e livro ausente.
- **Limites:** ensaio manual, sem disparo automático do Quartz; substituição dos destaques continua sem transação única, sujeita a falhas parciais nas escritas. Nenhuma nova campanha quantitativa; auxiliares somente em `target`.

### 29/09/2026 Comandos e processamento de empréstimos no serviço

- **Situação:** protótipo não commitado, posteriormente substituído pela coordenação unidirecional descrita abaixo. As evidências desta entrada referem-se àquele desenho, não à versão revisada.
- **Implementação:** criação e devolução no microsserviço; Struts redirecionado pela ACL; referências de usuários e estoque fornecidos por API interna autenticada do legado. Retirados DAO, service, EJB/interface e job antigos de empréstimos. Livros, usuários e destaques não foram migrados.
- **Consistência:** registro durável dos comandos, movimentação atômica e deduplicada do estoque no legado e retomada periódica no serviço. Repetições de devolução passam a retornar sucesso sem nova movimentação, uma mudança deliberada em relação ao erro anterior. Há consistência eventual entre duas transações locais, não atomicidade distribuída.
- **Dados:** script de corte preserva empréstimos e remove as duas cascatas entre domínios. Banco físico e credenciais permanecem compartilhados; propriedade lógica é uma restrição das aplicações, não isolamento por permissões. Script aplicado somente ao banco isolado do ensaio.
- **Validação:** primeira execução dos dez testes de comandos, antes da implementação HTTP, teve nove falhas esperadas. Após implementação e ampliação dos cenários, 107 testes do serviço passaram. Build limpo do legado compilou 37 classes, sem testes internos. No JBoss/Struts com PostgreSQL isolado, criação e devolução repetidas preservaram um único efeito no estoque. Uma falha SQL após confirmação do estoque foi recuperada automaticamente pelo agendamento sem novo desconto. Calendário sintético no ensaio; nenhuma coleta quantitativa final realizada.
- **Agendamento e corte:** no WAR sem os componentes antigos, criação e devolução pelo Struts passaram novamente. Cron de multas acelerado apenas no ensaio atualizou um atraso para R$ 10,00 e preservou R$ 99,00 de um empréstimo já devolvido. Auditoria textual não encontrou SQL de empréstimos no legado nem SQL de livros/usuários no serviço.
- **Limitações:** indisponibilidade persistente pode manter operações pendentes; não há prazo garantido de recuperação. Exclusão de referências e edição concorrente de estoque continuam exigindo cuidado operacional. Não foi criada pasta versionada de validação nem suíte no legado.

### 29/09/2026 Coletores dos escopos finais

- **Versionamento:** preparação mantida apenas na árvore de trabalho, fora da sequência de commits da aplicação, por decisão do autor. A campanha final aguarda revisão do histórico, testes manuais e eventuais ajustes.
- **Preparação:** coletor pós-migração com registro do snapshot, build e inicialização de monólito, serviço e conjunto; CK separado por aplicação e união identificada por tipo. Coletas antigas preservadas. Scripts da baseline recusam uso como coleta final.
- **Verificação interna:** em cópia temporária com histórico próprio, passaram registro, CK das duas aplicações, uma execução do build conjunto e uma inicialização de cada escopo. Confirmadas recusas de árvore suja e sobrescrita. Essas execuções são diagnósticas, não uma terceira campanha nem resultados finais do TCC.
- **Pendência:** revisão e commits antes das dez observações por escopo e da análise quantitativa. Nenhum commit foi criado no histórico principal nesta etapa.

### 29/09/2026 Revisão da direção das dependências

- **Decisão:** após revisão com o autor, manter no legado a coordenação entre empréstimos, usuários e estoque. Removidos o cliente de retorno, a API interna do legado e o agendador do Quarkus. Login e permissões continuam locais; o serviço autentica o sistema chamador por segredo de integração.
- **Fronteira:** o legado consulta os cadastros e envia IDs, tipo e datas; regras e persistência de empréstimos continuam no serviço. O Quartz local solicita atualização de multas usando o tipo atual do usuário. A consulta dos mais emprestados permanece inalterada e não depende de usuários.
- **Recuperação:** diário local do coordenador, reserva de estoque atômica com o registro da criação e retomada por Quartz. O serviço deduplica os comandos em transação local. Não há atomicidade distribuída, compensação automática ou garantia de tempo de recuperação.
- **Validação:** 107 testes do serviço aprovados; WAR com 38 classes de produção compilado, sem suíte interna. Ensaio temporário com Struts/JBoss e PostgreSQL isolado confirmou criação/devolução repetidas, retomada automática após falha de criação remota e após devolução remota com falha local, além do uso do tipo atualizado pelo job de multas. API recusou chamadas sem token ou com token incorreto. Teste adicional confirmou rejeição quando o serviço está sem segredo configurado, mantendo health público.
- **Compatibilidade:** preservada a regra padrão para tipos adicionais do cadastro legado, como ADMIN; os tipos não foram restritos artificialmente aos três descritos nos requisitos. Teste de regressão acrescentado.
- **Limites:** calendário sintético; nenhum commit ou coleta quantitativa final. O texto deve descrever extração de regras e dados de empréstimos, não extração integral da coordenação de todos os domínios. Script de banco revisado e aplicado apenas em novo banco de ensaio.

## Próximas evidências necessárias

| Pendência | Evidência esperada |
| --- | --- |
| Conferir instrumento e escopos finais | Classes convencionais reconhecidas; manter mesmas regras de análise nos dois snapshots |
| Aplicar Maven fixado às coletas finais | Maven 3.9.11 explícito e versão efetiva registrada nos três escopos |
| Completar cenários adversos | Timeout de leitura e indisponibilidade prolongada; recuperação e multas disparadas pelo Quartz legado já exercitadas em ensaio isolado |
| Comparar comportamentos | Cenários equivalentes executados nas duas versões |
| Executar campanha final após revisão e commits | Coletores com monólito, serviço e conjunto identificados; dez observações de tempos por escopo |
| Aplicar corte no ambiente de apresentação | Backup, script de migração e implantação coordenada; ensaio isolado já realizado |
| Atualizar o texto | Markdown aplicado ao DOCX, referências conferidas e revisão visual |

## Modelo para novas entradas

### AAAA-MM-DD Título do evento

- **Data de registro:**
- **Tipo:** contemporâneo ou retrospectivo.
- **Estado do experimento:**
- **Evidências:** commits, arquivos, comandos e relatórios.
- **Fato observado:**
- **Problema ou decisão:**
- **Justificativa:**
- **Validação e resultado:**
- **Consequência para métricas ou comportamento:**
- **Limitações e pendências:**
