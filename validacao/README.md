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
