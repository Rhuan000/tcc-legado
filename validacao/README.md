# Contas do ambiente de ensaio

Recrie o banco a partir de `init-db.sql` desta revisao antes de testar.
Todos os 34 usuarios iniciais possuem credenciais. A senha de ensaio e
`Desenvolvedores` (D maiusculo), armazenada no formato SHA-256 esperado pelo
login legado. Essa credencial publica e o hash legado servem apenas ao ambiente
descartavel; nao constituem uma solucao de autenticacao para producao.

| Matricula | Tipo | Perfil |
| --- | --- | --- |
| 2024001 | BIBLIOTECARIO | ADMIN |
| 2024002 | PROFESSOR | CONSULTA |
| 2024003 | ALUNO | CONSULTA |
| 2024004 | PROFESSOR | CONSULTA |
| 2024005 a 2024034 | ALUNO, PROFESSOR ou BOLSISTA | CONSULTA |

As antigas matriculas mat001 a mat030 correspondem a 2024005 a 2024034,
respectivamente. Os IDs foram preservados. Ha somente um bibliotecario inicial
e uma credencial de perfil ADMIN. ADMIN e CONSULTA nao sao tipos de usuario.
Os dez antigos ADMIN da massa de leitores
passaram a BOLSISTA; essa mudanca influencia prazos/multas e deve ser aplicada
igualmente ao pre e ao pos antes das novas evidencias.

O cadastro de novos leitores exige matricula unica com sete digitos e senha
entre 8 e 128 caracteres. Usuario e credencial sao gravados na mesma transacao,
sempre com perfil CONSULTA. A senha de ensaio nao e atribuida automaticamente
a novos cadastros. Editar nome, email ou tipo preserva senha e perfil.

## Vinculo e regras de emprestimo

| Tipo | Prazo em dias uteis | Multa por dia util de atraso |
| --- | --- | --- |
| ALUNO | 7 | R$ 2,00 |
| BIBLIOTECARIO | 7 | R$ 2,00 |
| PROFESSOR | 14 | R$ 0,50 |
| BOLSISTA | 10 | R$ 1,00 |

Bibliotecarios podem receber emprestimos, inclusive a conta 2024001. Essa e a
politica do cenario experimental, aplicada igualmente no pre e no pos. O perfil
ADMIN autoriza operacoes no portal e nao concede prazo maior nem isencao de multa.
Um novo cadastro de BIBLIOTECARIO recebe CONSULTA, como os demais cadastros;
selecionar um vinculo nao concede permissoes administrativas.

## Validacao funcional

1. Recriar banco, aplicar init-db e, somente no pos, a migracao de emprestimos.
2. Entrar como 2024001 e cadastrar aluno, professor, bolsista e bibliotecario com senhas proprias.
3. Confirmar login de cada cadastro com perfil CONSULTA.
4. Rejeitar matricula repetida, fora do formato e senha ausente/curta.
5. Editar nome/tipo de um leitor e confirmar que senha e perfil foram preservados.
6. Emprestar e devolver um livro para 2024001, verificando prazo, multa e estoque.
7. Confirmar os mesmos comportamentos no pre e no pos.

O init-db cria credenciais automaticamente para a massa. A tela de cadastro
cria credenciais para novos usuarios. Usuarios e senhas antigos do banco local
nao mudam ate sua recriacao; esta tarefa nao executa reset no banco do autor.

## Pacotes publicados para auditoria

Os dois pacotes originais abaixo contêm a rastreabilidade e as evidências dos
41 casos de cada estado. Foram publicados sem alterar os bytes dos ZIPs:

| Estado | Pacote | SHA-256 |
| --- | --- | --- |
| Anterior | [TCC-Legado.zip](pacotes/TCC-Legado.zip) | `6B5B414617DA84BEE6C6A77E2ABB90312CAF6A106BC7F1A4313D6A78D145212E` |
| Posterior | [TCC-Pos-Migracao.zip](pacotes/TCC-Pos-Migracao.zip) | `36E7914160CCB9213BA3EEF09642EAEA049381F7059F1ED6DA0C9EE232475214` |

Os hashes dos pacotes conferem com os JSON originais
[validacao-funcional-pre-aa55edd.json](validacao-funcional-pre-aa55edd.json) e
[validacao-funcional-pos-45ff20b.json](validacao-funcional-pos-45ff20b.json).
Esses JSON continuam com seus caminhos históricos de Downloads; o mapeamento
para os arquivos publicados está na tabela acima. Os mesmos documentos foram
copiados pelo coletor para as campanhas finais em `metricas/`.

Após extrair os pacotes, consulte:

- `TCC-Legado/evidencias/rastreabilidade.json` e
  `TCC-Pos-Migracao/evidencias/rastreabilidade.json`: identificação dos casos,
  resultados e arquivos de evidência.
- `TCC-Legado/evidencias/07-Rotinas/LIM-02-virada-ano.txt`:
  complementação controlada da consulta de feriados no estado anterior.
- `TCC-Pos-Migracao/evidencias/07-Rotinas/JOB-02-comando-e-metodologia.md`:
  procedimento e limites da execução controlada da rotina posterior.
- Os arquivos `LEIA-ME.txt` dos pacotes: orientação de navegação.

## Relação com a recoleta de 23/10/2026

As evidências manuais são anteriores e vinculam-se aos commits
`aa55edd88c412725c84c10bff92e88a503e39fcb` (pré) e
`45ff20b3d68ff484143a7b28e35b7c1d93fc361b` (pós). Os 41 casos de cada
versão **não foram reexecutados na recoleta**. Os testes automatizados foram
reexecutados pelos builds medidos: 33 no anterior, 32 no monólito posterior
com três ignorados e 165 no serviço, sem falhas ou erros.

O aceite funcional conserva os limites documentados nos JSON e nos pacotes:
quantidade negativa verificada pelo formulário, devolução repetida com estoque
preservado e diferença de mensagem no pós, e rotina diária verificada por
execução controlada, sem observar o disparo automático à meia-noite.
Uma resposta HTTP 200 dos endpoints usados na métrica de inicialização
não substitui esses casos funcionais.

O índice dos resultados atuais está em [metricas/README.md](../metricas/README.md);
os comandos para repetir o ensaio estão em
[medicoes/README.md](../medicoes/README.md).
