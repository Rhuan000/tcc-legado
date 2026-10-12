# Revisao da linha experimental

A branch `experimento/pre-migracao-revisada` parte de `c6e2389` e corrige
o formulario e a navegacao de devolucao do legado, sem alterar sua arquitetura.
O formulario enviava `idEmprestimo`, enquanto a Action esperava `id`.
O botao da listagem chamava a operacao de devolucao em vez de abrir o formulario.
A selecao recebida da listagem ou do detalhe passa a ser preservada.

## Dados e rastreabilidade

Os resultados herdados em `metricas/pre-migracao/` sao preliminares e estao
excluidos da nova analise. Nao representam o codigo corrigido. A tag original
`experimento/pre-migracao` preserva a coleta anterior. Nao misturar amostras
antigas com novas nem reutilizar a planilha antiga como resultado revisado.

Antes de coletar, validar funcionalmente o codigo corrigido, registrar seu
commit e garantir uma arvore limpa no escopo medido. Arquivar a pasta antiga
fora do diretorio de saida dos coletores; executar o protocolo completo do
README e gerar novamente os artefatos derivados. Registrar os resultados em
um commit posterior ao codigo medido.

## Validacao funcional antes de cada coleta

1. Criar um emprestimo e verificar usuario, livro, prazo e decremento do estoque.
2. Abrir Registrar Devolucao pela listagem e selecionar um emprestimo ativo.
3. Abrir Devolver em uma linha e pelo detalhe; conferir o ID preselecionado.
4. Confirmar a devolucao e verificar data real, multa e reposicao do estoque.
5. Conferir que o emprestimo devolvido nao aparece entre os ativos.
6. Validar multas com e sem atraso, tipos de usuario e calendario de feriados.

Compilar o WAR nao substitui esses cenarios em JBoss com banco configurado.
Nao declarar equivalencia funcional ou coleta concluida sem executa-los.

## Continuidade da migracao

Preservar a main original. Reaplicar os commits de migracao em uma branch
derivada do legado corrigido, sem importar os resultados antigos como novas
medicoes. O reparo de interface equivale a `cc01568` e nao precisa ser duplicado.
O reparo `c854586` e do cliente de feriados moderno e deve permanecer na linha
moderna. Cada marco intermediario e final exige validacao funcional e coleta
proprias, com commit, ambiente e protocolo registrados.

## Texto para o TCC apos concluir a revisao

As coletas preliminares foram excluidas da analise apos a identificacao de
falhas funcionais no fluxo de devolucao. A implementacao pre-migracao foi
corrigida preservando sua arquitetura. Os estados experimentais foram
validados e submetidos a novas medicoes sob o protocolo definido. Os registros
anteriores foram preservados para rastreabilidade.
