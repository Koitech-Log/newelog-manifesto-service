package br.com.newelog.dto;

/** "campo" pode ser null quando o erro é da linha inteira (ex.: menos colunas que o cabeçalho). */
public record ErroLinhaCsvDTO(int linha, String campo, String motivo) {}