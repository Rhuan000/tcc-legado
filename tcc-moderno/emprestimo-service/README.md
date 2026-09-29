# emprestimo-service

Microsserviço criado para receber, de forma incremental, a capacidade de
gestão de empréstimos do monólito. A migração começa pelas regras de multa,
prazo e calendário e avança até consultas, criação, devolução e propriedade
lógica dos dados de empréstimo. A coordenação entre domínios e o disparo dos
processamentos periódicos permanecem no legado.

O banco PostgreSQL pode permanecer fisicamente compartilhado durante o
experimento. No estado final, somente este serviço deverá acessar diretamente
as tabelas de empréstimo; o monólito deverá usar a API HTTP.

## Consultas de empréstimos

- `GET /emprestimos`: lista ordenada por ID.
- `GET /emprestimos/{id}`: consulta individual; 404 para ID inexistente e 400 para ID não positivo.
- `GET /emprestimos/atrasados`: empréstimos sem devolução e com vencimento anterior a `CURRENT_DATE` do PostgreSQL.
- `GET /emprestimos/mais-emprestados-mes?limite=5`: IDs dos livros e total de empréstimos no mês do banco; limite de 1 a 100, padrão 5.

As respostas contêm IDs de livro e usuário, datas ISO (`yyyy-MM-dd`) e a multa
persistida. As consultas não recalculam multas. Multa nula é apresentada como
zero, preservando a leitura do legado. Falha no banco resulta em erro HTTP 500.
O acesso usa JDBC e pool de conexões, sem criação ou alteração automática de
tabelas. O monólito consome consultas e comandos pela ACL.
A contagem mensal inclui devolvidos, como no legado, e ordena por
total decrescente e ID crescente nos empates. Somente `idLivro` e
`totalEmprestimos` compõem essa resposta; dados de livros e gestão de destaques
permanecem no monólito.

Em desenvolvimento, a conexão aponta para `localhost:5432/biblioteca`, com
as credenciais locais do legado. Para executar o JAR, configurar
`EMPRESTIMO_DB_URL` (URL JDBC), `EMPRESTIMO_DB_USERNAME` e `EMPRESTIMO_DB_PASSWORD`.
Os endpoints são internos ao experimento e devem permanecer em rede restrita.

Os testes usam PostgreSQL 14 isolado via Dev Services e precisam de Docker
ativo (`./mvnw test`). O esquema reduzido de teste valida consultas e comandos;
a migração do esquema existente é um procedimento separado.
Configuração de referência: [datasources do Quarkus](https://quarkus.io/guides/datasource/).

A extensão JDBC acrescenta a verificação do banco ao health check de readiness.
Para medir disponibilidade HTTP sem exigir banco, usar `/q/health/live`.
Funcionamento integrado deve ser validado separadamente.

## Comandos recebidos do legado

- `POST /emprestimos`: `chave` (UUID), `idLivro`, `idUsuario`, `tipoUsuario` e `dataEmprestimo`.
- `POST /emprestimos/{id}/devolucao`: `tipoUsuario` e `dataReferencia`.
- `POST /emprestimos/{id}/multa`: `tipoUsuario` e `dataReferencia`; atualiza apenas empréstimo ativo e atrasado.
- Datas ISO e tipo não vazio. `PROFESSOR` e `BOLSISTA` têm regras específicas;
`ALUNO` e os demais tipos do cadastro seguem a regra padrão, como no legado.
Respostas de sucesso: 200.

O legado autentica o operador, verifica suas permissões, consulta seus cadastros
e envia os dados. O serviço valida o contrato, aplica as regras e persiste
empréstimos. Não há cliente HTTP, consulta de usuário/estoque ou agendamento
voltado ao legado dentro deste serviço. A consulta dos mais emprestados continua
usando somente a tabela de empréstimos.

Toda API de negócio exige `X-Integration-Token`, comparado com
`EMPRESTIMO_INTEGRATION_TOKEN`. Ausência/token incorreto: 401; segredo não
configurado no serviço: 503. Configurar o mesmo segredo nas duas aplicações,
fora do Git e do navegador. Isso autentica o sistema chamador, não o usuário
final. Usar rede restrita e HTTPS fora do ensaio local. Health checks permanecem
públicos para a medição de disponibilidade HTTP.

Criação repetida com a mesma chave e conteúdo retorna o mesmo empréstimo;
conteúdo diferente resulta em 409. Devolução repetida preserva a primeira
conclusão sem novo efeito. O serviço mantém um registro local de comandos
para retomar falhas SQL, mas quem repete a chamada é o coordenador no legado.
Os dados de cada comando ficam fixos durante sua recuperação. Na atualização
periódica, o legado consulta novamente o tipo atual do usuário.

Antes do primeiro deploy, parar ambas as aplicações, fazer backup e executar
uma vez `migracoes/001-comandos-emprestimo.sql` da raiz com
`psql -v ON_ERROR_STOP=1 -f ...`. O script preserva a tabela `emprestimo`,
remove suas duas chaves estrangeiras em cascata e cria:
- `emprestimo_operacao`: idempotência dos comandos, propriedade do serviço;
- `fluxo_emprestimo`: diário de coordenação e estoque, propriedade do legado.

A revisão anterior do script era um protótipo não commitado, aplicado apenas
ao banco isolado do ensaio anterior. Não aplicar esta revisão sobre aquele
esquema como se fosse uma migração incremental.

O banco físico e as credenciais permanecem compartilhados. A exclusividade
dos dados é lógica e auditada nas fontes, não imposta por permissões do banco.
A remoção das cascatas preserva históricos; referências ausentes exigem
correção operacional. Consulte o README do legado para recuperação de fluxos.

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/emprestimo-service-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/maven-tooling>.

## Related Guides

- REST Jackson ([guide](https://quarkus.io/guides/rest#json-serialisation)): Jackson serialization support for Quarkus REST. This extension is not compatible with the quarkus-resteasy extension, or any of the extensions that depend on it
- SmallRye Health ([guide](https://quarkus.io/guides/smallrye-health)): Monitor service health

## Provided Code

### REST

Easily start your REST Web Services

[Related guide section...](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)

### SmallRye Health

Monitor your application's health using SmallRye Health

[Related guide section...](https://quarkus.io/guides/smallrye-health)
