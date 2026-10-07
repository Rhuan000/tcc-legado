# emprestimo-service

Microsserviço criado para receber, de forma incremental, a capacidade de
gestão de empréstimos do monólito. A migração começa pelas regras de multa,
prazo e calendário e avança até consultas, criação, devolução e propriedade
lógica dos dados de empréstimo. O processamento periódico de multas pertence
ao serviço. A coordenação de criação/devolução com o estoque permanece no legado.

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
empréstimos. Para o job de multas, o serviço consulta o contexto atual do usuário
pela API interna do legado. Não consulta estoque. A consulta dos mais emprestados continua
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
periódica, o serviço consulta novamente o tipo atual do usuário no legado.

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

## Processamento periódico de multas e integração de usuários

`AtualizacaoMultasJob` identifica atrasados pelo repositório do serviço, consulta
`GET /integracao/usuarios/{id}/contexto` no legado e aplica as regras e a escrita
locais. O contrato retorna somente `idUsuario` e `tipoUsuario`. Não há cache do
tipo, cópia da tabela de usuários nem uso do tipo congelado na criação.

Configuração da nova integração (independente do token de comandos existente):

| Variável | Onde configurar | Finalidade |
| --- | --- | --- |
| `LEGADO_USUARIOS_URL` | Serviço | URL base com contexto, por exemplo `https://legado:8443/monolito-biblioteca` |
| `LEGADO_USUARIOS_INTEGRATION_TOKEN` | Ambas as aplicações | Mesmo segredo aleatório exclusivo, com pelo menos 32 caracteres, fora do Git |
| `LEGADO_USUARIOS_ALLOW_LOCAL_HTTP` | Ambas, somente em ensaio local | `true` permite HTTP de loopback; padrão `false` |
| `EMPRESTIMO_MULTAS_CRON` | Serviço | Padrão `0 0 0 * * ?`; `off` desativa; `0 */5 * * * ?` permite ensaio a cada cinco minutos |

A URL inclui o contexto de implantação do WAR; a rota acima é anexada a ela.
O segredo segue no header `X-Integration-Token`, nunca na URL. O cliente exige
HTTPS, valida o certificado normalmente, não segue redirecionamentos e limita
conexão/leitura a três segundos. HTTP local só é aceito com a exceção explícita
e URL de loopback (`localhost`, `127.0.0.1` ou `::1`). No legado, a exceção também
exige que a conexão venha de loopback. Não habilitar essa exceção atrás de proxy
acessível remotamente. Não desabilitar a verificação TLS; configurar certificado
confiável e restringir a rota à rede dos serviços no firewall/proxy.

Sem segredo válido (mínimo 32 caracteres), a integração falha fechada. O token
autentica o serviço para leitura do contexto, não operadores nem comandos de
empréstimo. Configurar/rotacionar os segredos nas duas aplicações de forma
coordenada. Não reutilizar `EMPRESTIMO_INTEGRATION_TOKEN` para essa leitura.

O agendador usa `America/Sao_Paulo`, configurável por `emprestimo.multas.time-zone`.
Alinhar o fuso da JVM do legado e do PostgreSQL com esse fuso: a seleção de
atrasados preserva `CURRENT_DATE` do banco. Há proteção contra sobreposição na
mesma instância; este experimento pressupõe **uma instância ativa do serviço**.
Não há coordenação de agendadores entre réplicas nem execução retroativa garantida
de disparos perdidos durante indisponibilidade.

Falha HTTP, timeout ou contexto inválido preserva a multa daquele empréstimo,
registra a falha sem corpo/credencial e permite processar os demais. O ciclo é
reportado como falho e a próxima execução volta a consultar o contexto e recalcula
o valor integral (não soma novamente). Usuário excluído ou cadastro inválido exige
correção operacional. Devoluções concluídas ou pendentes continuam protegidas
pela escrita condicional do repositório. Não há transação distribuída nem snapshot
atômico entre o cadastro remoto e a atualização da multa.

Para o corte, implantar a rota e configurar os segredos, retirar o job antigo do
legado e só então ativar o cron no serviço. O Quartz legado mantém recuperação de
fluxos e destaques, sem atualização de multas. Criação e devolução continuam
recebendo contexto do coordenador legado. O endpoint autenticado de atualização
individual de multa permanece compatível, mas não é mais usado pelo job legado.

Os testes novos ficam somente no microsserviço: cliente HTTP com legado simulado,
segurança do envio, regras de falha e processamento com PostgreSQL isolado,
incluindo disparo real do agendador. Eles não certificam o deployment da servlet
no JBoss; a validação integrada com o legado real permanece separada.

Referências: [REST Client](https://quarkus.io/guides/rest-client/) e
[Scheduler](https://quarkus.io/guides/scheduler-reference/).

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
