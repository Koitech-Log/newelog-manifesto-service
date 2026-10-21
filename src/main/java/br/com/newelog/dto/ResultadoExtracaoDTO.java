package br.com.newelog.dto;

import java.util.List;

/**
 * Resultado da leitura do CSV: registros mapeados e quantidade de linhas
 * descartadas por estarem malformadas (poucas colunas, data ou valor ilegível).
 */
public record ResultadoExtracaoDTO(List<ManifestoMapeadoDTO> manifestos, int linhasIgnoradas) {
}
