package br.com.newelog.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import br.com.newelog.dto.VeiculoUploadResponseDTO;
import br.com.newelog.service.VeiculoUploadService;
import br.com.newelog.validation.VeiculoUploadValidator;

@RestController
@RequestMapping("/veiculos")
public class VeiculoUploadController {

    private final VeiculoUploadValidator validator;
    private final VeiculoUploadService service;

    public VeiculoUploadController(
            VeiculoUploadValidator validator,
            VeiculoUploadService service
    ) {
        this.validator = validator;
        this.service = service;
    }

    /**
     * Recebe o CSV de veículos. O cabeçalho Authorization é repassado ao
     * motoristas-service, que restringe o cadastro ao perfil GESTOR.
     */
    @PostMapping("/upload")
    public ResponseEntity<VeiculoUploadResponseDTO> upload(
            @RequestParam("file") MultipartFile file,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization
    ) {
        validator.validar(file);
        VeiculoUploadResponseDTO response = service.processar(file, authorization);
        return ResponseEntity.ok(response);
    }
}
