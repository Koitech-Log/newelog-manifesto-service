package br.com.newelog.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import br.com.newelog.dto.ErroLinhaCsvDTO;
import br.com.newelog.dto.ResultadoCadastroVeiculoDTO;
import br.com.newelog.dto.ResultadoExtracaoCsvDTO;
import br.com.newelog.dto.VeiculoCsvDTO;
import br.com.newelog.dto.VeiculoUploadResponseDTO;
import br.com.newelog.validation.ManifestoUploadException;

/**
 * Orquestra o upload de veículos: lê o CSV com ExtratorVeiculoCsvService, envia
 * as linhas válidas ao motoristas-service em uma única chamada e junta os erros
 * de leitura e de cadastro em uma resposta só. Linhas com erro não impedem o
 * cadastro das demais.
 */
@Service
public class VeiculoUploadService {

    private final ExtratorVeiculoCsvService extrator;
    private final VeiculoServiceClient veiculoServiceClient;

    public VeiculoUploadService(ExtratorVeiculoCsvService extrator,
                                VeiculoServiceClient veiculoServiceClient) {
        this.extrator = extrator;
        this.veiculoServiceClient = veiculoServiceClient;
    }

    public VeiculoUploadResponseDTO processar(MultipartFile arquivo, String authorization) {
        ResultadoExtracaoCsvDTO extraido = extrair(arquivo);

        List<VeiculoCsvDTO> veiculos = extraido.veiculos();
        List<ErroLinhaCsvDTO> erros = new ArrayList<>(extraido.erros());
        int totalLinhas = veiculos.size() + erros.size();

        if (totalLinhas == 0) {
            throw new ManifestoUploadException("O arquivo não contém linhas de veículos.");
        }

        int cadastrados = 0;
        int atualizados = 0;

        if (!veiculos.isEmpty()) {
            ResultadoCadastroVeiculoDTO resultado = veiculoServiceClient.cadastrar(veiculos, authorization);
            cadastrados = resultado.cadastrados();
            atualizados = resultado.atualizados();
            if (resultado.erros() != null) {
                erros.addAll(resultado.erros());
            }
        }

        erros.sort(Comparator.comparingInt(ErroLinhaCsvDTO::linha));
        int rejeitados = erros.size();

        String mensagem = String.format(
                "Processado: %d linhas (%d cadastrados, %d atualizados, %d rejeitados)",
                totalLinhas, cadastrados, atualizados, rejeitados);

        return new VeiculoUploadResponseDTO(
                mensagem, arquivo.getOriginalFilename(), totalLinhas,
                cadastrados, atualizados, rejeitados, erros);
    }

    private ResultadoExtracaoCsvDTO extrair(MultipartFile arquivo) {
        try (InputStream entrada = arquivo.getInputStream()) {
            return extrator.extrair(entrada);
        } catch (IllegalArgumentException e) {
            // CSV sem cabeçalho ou sem uma das colunas obrigatórias: erro do arquivo (400).
            throw new ManifestoUploadException(e.getMessage());
        } catch (IOException e) {
            throw new ManifestoUploadException("Não foi possível ler o CSV: " + e.getMessage());
        }
    }
}
