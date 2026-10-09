package br.com.newelog.controller;

import br.com.newelog.service.MotoristasServiceException;
import br.com.newelog.validation.ManifestoUploadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Converte exceções de domínio em respostas HTTP consistentes, no mesmo
 * formato de corpo de erro usado no motoristas-service (timestamp + mensagem).
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(ManifestoUploadException.class)
    public ResponseEntity<Map<String, Object>> handleUploadInvalido(ManifestoUploadException ex) {
        return ResponseEntity.badRequest().body(corpoDeErro(ex.getMessage()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleArquivoGrande(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(corpoDeErro("Arquivo maior do que o permitido."));
    }

    @ExceptionHandler(MotoristasServiceException.class)
    public ResponseEntity<Map<String, Object>> handleMotoristasService(MotoristasServiceException ex) {
        return ResponseEntity.status(ex.getStatus()).body(corpoDeErro(ex.getMessage()));
    }

    /** Detalhes ficam só no log: a mensagem da exceção pode expor URLs e dados internos. */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleErroInesperado(RuntimeException ex) {
        log.error("Erro inesperado ao processar o manifesto", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(corpoDeErro("Erro inesperado ao processar o manifesto."));
    }

    private Map<String, Object> corpoDeErro(String mensagem) {
        Map<String, Object> corpo = new HashMap<>();
        corpo.put("timestamp", Instant.now().toString());
        corpo.put("mensagem", mensagem);
        return corpo;
    }
}
