package br.com.newelog.dto;

import java.util.List;

/**
 * Resposta de POST /api/veiculos/cadastrar-de-csv no motoristas-service.
 * Os erros usam o mesmo formato (linha + motivo) dos erros de leitura do CSV.
 */
public record ResultadoCadastroVeiculoDTO(
        int cadastrados,
        int atualizados,
        List<ErroLinhaCsvDTO> erros
) {
}
