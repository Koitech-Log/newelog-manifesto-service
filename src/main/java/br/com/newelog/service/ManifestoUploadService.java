package br.com.newelog.service;

import br.com.newelog.dto.ManifestoMapeadoDTO;
import br.com.newelog.dto.ManifestoUploadResponseDTO;
import br.com.newelog.dto.MotoristaCadastradoDTO;
import br.com.newelog.dto.MotoristaNovoDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
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

        int cadastrados = 0;
        int rejeitados = 0;
        List<MotoristaNovoDTO> novos = new ArrayList<>();
        // Evita listar o mesmo motorista várias vezes quando o manifesto tem
        // mais de uma linha (viagem) para ele — cadastroValidado continua
        // false em todas as chamadas até uma validação manual futura.
        java.util.Set<String> cpfsJaListados = new java.util.HashSet<>();

        for (ManifestoMapeadoDTO manifesto : manifestos) {
            boolean valido = motoristaServiceClient.validar(manifesto);
            if (!valido) {
                rejeitados++;
                continue;
            }

            MotoristaCadastradoDTO cadastrado = motoristaServiceClient.cadastrar(manifesto);
            if (cadastrado == null) {
                rejeitados++;
                continue;
            }

            cadastrados++;
            // cadastroValidado=false sinaliza que o motorista ainda não passou
            // por validação manual — inclui tanto quem acabou de ser criado
            // agora quanto quem já estava pendente de manifestos anteriores
            // (ver ManifestoCadastroService no motoristas-service). É o
            // critério usado para exibir só os motoristas novos/pendentes na
            // tela de importação.
            if (!cadastrado.isCadastroValidado() && cpfsJaListados.add(manifesto.getcpfMotorista())) {
                novos.add(new MotoristaNovoDTO(
                        cadastrado.getNome(),
                        cadastrado.getPlacaVeiculo(),
                        cadastrado.getStatus()
                ));
            }
        }

        String mensagem = novos.isEmpty()
                ? String.format("Processado: %d manifestos (%d cadastrados/atualizados, %d rejeitados)",
                        manifestos.size(), cadastrados, rejeitados)
                : String.format("Processado: %d manifestos (%d cadastrados/atualizados, %d novos, %d rejeitados)",
                        manifestos.size(), cadastrados, novos.size(), rejeitados);

        return new ManifestoUploadResponseDTO(mensagem, nomeArquivo, manifestos.size(), cadastrados, rejeitados, novos);
    }
}