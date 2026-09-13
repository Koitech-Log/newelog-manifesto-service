package br.com.newelog.controller;

import br.com.newelog.dto.ManifestoUploadResponseDTO;
import br.com.newelog.service.ManifestoUploadService;
import br.com.newelog.validation.ManifestoUploadValidator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/manifestos")
public class ManifestoUploadController {

    private final ManifestoUploadValidator validator;
    private final ManifestoUploadService service;

    public ManifestoUploadController(ManifestoUploadValidator validator, ManifestoUploadService service) {
        this.validator = validator;
        this.service = service;
    }

    @PostMapping("/upload")
    public ResponseEntity<ManifestoUploadResponseDTO> upload(@RequestParam("file") MultipartFile file) {
        validator.validar(file);
        ManifestoUploadResponseDTO response = service.processar(file);
        return ResponseEntity.ok(response);
    }
}