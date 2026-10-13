package br.com.newelog.dto;

import java.util.List;

/**
 * Resposta de POST /veiculos/upload. "erros" junta as linhas que não puderam ser
 * lidas do CSV e as rejeitadas no cadastro, ordenadas pelo número da linha.
 */
public record VeiculoUploadResponseDTO(
        String mensagem,
        String nomeArquivo,
        int totalLinhas,
        int cadastrados,
        int atualizados,
        int rejeitados,
        List<ErroLinhaCsvDTO> erros
) {
}
