package br.com.newelog.service;

import br.com.newelog.dto.ManifestoUploadResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ManifestoUploadService {

    public ManifestoUploadResponseDTO processar(MultipartFile arquivo) {
        
        String nomeArquivo = arquivo.getOriginalFilename();

        //só pra testar, dps implementar o resto (conversao pra json (task12))
        return new ManifestoUploadResponseDTO(
                "arquivo recebido. funcionou!!!!!!!",
                nomeArquivo
        );
    }
}