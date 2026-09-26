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

## Próximas evidências necessárias

| Pendência | Evidência esperada |
| --- | --- |
| Reconciliar linha de base | Arquivos medidos identificados e divergência explicada, ou nova coleta documentada |
| Padronizar ou justificar Maven | Versão efetiva nos coletores de cada aplicação |
| Implementar prazo | Testes aprovados mantendo os cenários |
| Integrar feriados | Cliente e cache testados, preservando anos de consulta |
| Expor políticas e criar ACL | Contratos e testes de integração |
| Comparar comportamentos | Cenários equivalentes executados nas duas versões |
| Preparar coletores | Monólito, serviço e conjunto medidos com critérios explícitos |
| Migrar dados e consumidores | Auditoria sem acesso direto remanescente, incluindo destaques |
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
