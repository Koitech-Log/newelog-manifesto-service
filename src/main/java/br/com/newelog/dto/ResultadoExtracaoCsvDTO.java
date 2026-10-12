package br.com.newelog.dto;

import java.util.List;

public record ResultadoExtracaoCsvDTO(List<VeiculoCsvDTO> veiculos, List<ErroLinhaCsvDTO> erros) {
    
}
