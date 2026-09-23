package br.com.newelog.service;

import br.com.newelog.dto.ManifestoMapeadoDTO;
import br.com.newelog.dto.ManifestoUploadResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class ManifestoUploadService {

    private final ExtratorManifestoCsvService extrator;
    private final MotoristaServiceClient motoristaServiceClient;

    public ManifestoUploadService(ExtratorManifestoCsvService extrator,
                                   MotoristaServiceClient motoristaServiceClient) {
        this.extrator = extrator;
        this.motoristaServiceClient = motoristaServiceClient;
    }

    public ManifestoUploadResponseDTO processar(MultipartFile arquivo) {
        String nomeArquivo = arquivo.getOriginalFilename();
        List<ManifestoMapeadoDTO> manifestos;

        try {
            manifestos = extrator.extrairEMapear(arquivo.getInputStream());
        } catch (Exception e) {
            throw new RuntimeException("Erro ao ler o CSV: " + e.getMessage(), e);
        }

        int validados = 0;
        int rejeitados = 0;

        for (ManifestoMapeadoDTO manifesto : manifestos) {
            boolean ok = motoristaServiceClient.validar(manifesto);
            if (ok) validados++; else rejeitados++;
        }

        String mensagem = String.format(
                "Processado: %d manifestos (%d válidos, %d rejeitados)",
                manifestos.size(), validados, rejeitados
        );
        return new ManifestoUploadResponseDTO(mensagem, nomeArquivo);
    }
}