# Correções F01–F05 — 14/10/2026

Revisão aplicada às branches `experimento/pre-migracao-revisada` e
`experimento/migracao-revisada`. A coleta funcional anterior permanece histórica.

| Falha | Confirmação por inspeção | Correção |
| --- | --- | --- |
| F01 | No pré, o lookup usa um nome JNDI de um serviço CDI. Na migração esse lookup já havia sido substituído pela ACL. | No pré, consultar empréstimo ativo pelo DAO injetado, sem dependência circular de serviços. Na migração, preservar a ACL. Manter bloqueios por estoque e empréstimo ativo; falha de consulta impede excluir. Atualizar contador somente após exclusão efetiva. |
| F02 | Nas duas versões, salvar/atualizar capturavam e descartavam SQLException; a Action anunciava sucesso. | Propagar erros, traduzir SQLState 23505 em mensagem de ISBN duplicado e verificar linhas afetadas. Erros retornam ao formulário; sucesso somente após persistência. ISBN vazio representa ausência (SQL NULL). |
| F03 | Nas duas versões, setInt desempacotava ano nulo. Formulário e esquema permitem ausência. | Manter ano opcional, escrever SQL NULL e preservar null na leitura, no cadastro e na edição. |
| F04 | Nas duas versões faltava configurar a codificação antes do AuthFilter, que já lê parâmetros. DAO usa setString sem conversões manuais. | Filtro UTF-8 antes da autenticação/Struts, para entrada e saída; formulários de livros e destaques explicitam UTF-8. |
| F05 | Nas duas versões, exceção de quantidade inválida escapava do cadastro; edição não aplicava a mesma validação. | Exceção de negócio reconhecida pelo EJB e tratada pela Action; validação compartilhada entre cadastro e edição. Formulário reapresenta campos com escape HTML, sem stack trace. |

## Verificação executada

Java 8 (`jdk1.8.0_201`) e Maven 3.9.11. **21 testes, zero falhas, zero erros,
zero testes ignorados por execução**, nas duas branches, tanto em H2 2.2.224
quanto no PostgreSQL local. Os dois WARs foram gerados com `mvn package`.

Os testes cobrem cadastro/edição com ISBN repetido, edição com mesmo ISBN ou
ISBN anteriormente nulo, ISBN opcional, ano vazio/preenchido, título obrigatório
e números inválidos enviados diretamente à Action, quantidade negativa no
cadastro/edição, exclusão sem estoque, com estoque, empréstimo ativo/devolvido,
consulta de empréstimos indisponível e falha de persistência sem falso sucesso.
Conferem dados persistidos e atributos retornados ao formulário.

Para acentos, verificam a ordem dos filtros e a decodificação de corpo UTF-8
antes da leitura dos parâmetros, além de cadastrar/editar e consultar pela lista
e detalhe do DAO de destaques os textos `ação`, `validação`, `João` e `Promoção`.
São entradas novas; nenhum texto corrompido da coleta foi reaproveitado.

No PostgreSQL, cada teste cria um schema aleatório `regressao_<uuid>` e o remove
ao terminar. As tabelas de aplicação e os registros da coleta não são alterados.
O schema de teste contém somente as estruturas necessárias aos cenários.

## Reprodução

Na pasta `monolito-biblioteca` de cada branch, com JDK 8 selecionado:

```powershell
mvn test
mvn package '-Dtest.jdbc.url=jdbc:postgresql://localhost:5432/biblioteca'
```

A segunda execução usa PostgreSQL; `test.jdbc.user` e `test.jdbc.password`
permitem escolher as credenciais. O padrão local é o mesmo dos DAOs. O usuário
de teste precisa poder criar/remover seus próprios schemas. Sem `test.jdbc.url`,
a suíte usa somente H2 em memória.

Relatórios: `target/surefire-reports`. Artefato gerado:
`target/monolito-biblioteca.war`. Para registrar o estado publicado, usar
`git rev-parse HEAD` e `Get-FileHash target/monolito-biblioteca.war -Algorithm SHA256`
no momento da publicação.

## Limites e próxima coleta

As Actions e EJBs são executados diretamente nos testes, sem container EJB ou
navegador. Na migração, a fronteira da ACL de empréstimos é substituída por um
dublê nos testes de exclusão; não se trata de teste HTTP do microsserviço. O
teste de decodificação simula a leitura do corpo pelo servlet. A correção F04
fecha a lacuna identificada no código, mas o caminho completo navegador,
servidor publicado e banco ainda requer a nova coleta.

Não houve publicação dos WARs nem nova aprovação da matriz funcional pós.
Permanecem pendentes os cenários de multas, execução real do job, calendário,
repetição de devolução e exclusão de usuário com empréstimo ativo descritos na
coleta original. A data dos testes de destaque/devolução é fixa em 14/10/2026;
os testes aqui não recalculam multas nem comprovam equivalência temporal.
