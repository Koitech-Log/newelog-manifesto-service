package br.com.newelog.controller;

import br.com.newelog.validation.ManifestoUploadException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Converte exceções de domínio em respostas HTTP consistentes, no mesmo
 * formato de corpo de erro usado no motoristas-service (timestamp + mensagem).
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ManifestoUploadException.class)
    public ResponseEntity<Map<String, Object>> handleUploadInvalido(ManifestoUploadException ex) {
        return ResponseEntity.badRequest().body(corpoDeErro(ex.getMessage()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleArquivoGrande(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(corpoDeErro("Arquivo maior do que o permitido."));
    }

    /**
     * Mantém o status escolhido por quem lançou a exceção (ex.: 401/403 vindos
     * do motoristas-service no upload de veículos). Sem este handler, o
     * handler de RuntimeException abaixo transformaria tudo em 500.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleStatus(ResponseStatusException ex) {
        String mensagem = ex.getReason() != null ? ex.getReason() : "Erro ao processar a requisição.";
        return ResponseEntity.status(ex.getStatusCode()).body(corpoDeErro(mensagem));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleErroInesperado(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(corpoDeErro(ex.getMessage() != null ? ex.getMessage() : "Erro inesperado ao processar o manifesto."));
    }

    private Map<String, Object> corpoDeErro(String mensagem) {
        Map<String, Object> corpo = new HashMap<>();
        corpo.put("timestamp", Instant.now().toString());
        corpo.put("mensagem", mensagem);
        return corpo;
    }
}