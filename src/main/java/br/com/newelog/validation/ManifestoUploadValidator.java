package br.com.newelog.validation;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ManifestoUploadValidator {

    private static final String EXTENSAO_ESPERADA = ".csv";

    public void validar(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new ManifestoUploadException("Arquivo nao pode estar vazio");
        }

        String nomeArquivo = arquivo.getOriginalFilename();

        if (nomeArquivo == null
                || !nomeArquivo.toLowerCase().endsWith(EXTENSAO_ESPERADA)) {
            throw new ManifestoUploadException(
                    "Formato invalido, o arquivo precisar ser um: " + EXTENSAO_ESPERADA
            );
        }
    }
}
