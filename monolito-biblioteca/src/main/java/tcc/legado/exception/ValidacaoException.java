package tcc.legado.exception;

import javax.ejb.ApplicationException;

/** Erro de negócio que pode ser apresentado ao usuário, inclusive via EJB. */
@ApplicationException(rollback = true)
public class ValidacaoException extends RuntimeException {
    public ValidacaoException(String mensagem) {
        super(mensagem);
    }

    public ValidacaoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
