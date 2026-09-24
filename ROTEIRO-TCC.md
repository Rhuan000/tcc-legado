# Roteiro de execução do TCC

## Objetivo do estudo

Avaliar os efeitos da extração incremental da gestão de empréstimos de um
monólito Java EE para um microsserviço Quarkus, utilizando o padrão Strangler
Fig. Serão observados CBO, tempo de build e tempo de inicialização, sem assumir
previamente que todas as métricas melhorarão.

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
3. Migrar cálculo de prazo, calendário, cliente de feriados e cache.
4. Expor contratos para cálculo de prazo e multa.
5. Criar no monólito uma Anti-Corruption Layer que converta seus modelos em
   requisições HTTP.
6. Manter persistência, criação e devolução no monólito nesta etapa.
7. Prever timeout, indisponibilidade e respostas inválidas do serviço.

Critério de saída: o monólito usa o microsserviço para prazo e multa, com testes
de contrato e comportamento preservado.

Esse é o estado intermediário do experimento. Após estabilizá-lo, executar as
dez medições de build e inicialização e uma coleta de CBO.

## Estado 3 — migrar consultas e persistência

1. Implementar entidade e repositório de empréstimos no novo serviço.
2. Migrar busca por identificador, listagem e consulta de atrasados.
3. Fazer o `EmprestimoAction` consultar a API em vez do EJB.
4. Impedir novos acessos do monólito à tabela de empréstimos.
5. Manter o banco na mesma instância, atribuindo propriedade lógica exclusiva
   da tabela ao microsserviço.

Critério de saída: leituras e persistência de empréstimos pertencem ao serviço;
o monólito atua como cliente e adaptador da interface legada.

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
