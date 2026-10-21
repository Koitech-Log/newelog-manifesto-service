package br.com.newelog.service;

import org.springframework.http.HttpStatus;

/**
 * Falha ao falar com o motoristas-service que invalida o processamento inteiro
 * (token inválido, perfil sem permissão, serviço indisponível) — diferente de
 * uma linha rejeitada por regra de negócio, que só entra na contagem de rejeitados.
 */
public class MotoristasServiceException extends RuntimeException {

    private final HttpStatus status;

    public MotoristasServiceException(HttpStatus status, String mensagem) {
        super(mensagem);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
