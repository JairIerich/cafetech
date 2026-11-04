package com.cafetech.api.exception;

/**
 * Regra de negocio violada (duplicidade, estado invalido, dependencia bloqueante).
 * Mesmo padrao do projeto adopet do curso Boas Praticas Java da Alura.
 */
public class ValidacaoException extends RuntimeException {

    public ValidacaoException(String mensagem) {
        super(mensagem);
    }
}
