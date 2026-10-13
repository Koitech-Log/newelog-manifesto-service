package br.com.newelog.validation;


import java.util.Locale;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * Valida o arquivo recebido em POST /veiculos/upload antes de qualquer leitura:
 * não pode estar vazio, precisa ter extensão .csv e não pode passar do limite.
 *
 * O tipo é conferido pela extensão (como em ManifestoUploadValidator) e não pelo
 * Content-Type, porque navegadores e sistemas operacionais enviam CSV com tipos
 * diferentes (text/csv, application/vnd.ms-excel, application/octet-stream).
 */
@Component
public class VeiculoUploadValidator {

    private static final String EXTENSAO_ESPERADA = ".csv";
    public static final long TAMANHO_MAXIMO_BYTES = 2L * 1024 * 1024;

    public void validar(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new ManifestoUploadException("O arquivo não pode estar vazio.");
        }

        String nomeArquivo = arquivo.getOriginalFilename();
        if (nomeArquivo == null || !nomeArquivo.toLowerCase(Locale.ROOT).endsWith(EXTENSAO_ESPERADA)) {
            throw new ManifestoUploadException(
                    "Formato inválido: o arquivo precisa ser um " + EXTENSAO_ESPERADA + "."
            );
        }

        if (arquivo.getSize() > TAMANHO_MAXIMO_BYTES) {
            throw new ManifestoUploadException(
                    "Arquivo maior que o limite de " + (TAMANHO_MAXIMO_BYTES / (1024 * 1024)) + " MB."
            );
        }
    }
}
