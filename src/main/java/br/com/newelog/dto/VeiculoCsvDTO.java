package br.com.newelog.dto;

public record VeiculoCsvDTO(
        int linha,
        String placa,
        String marca,
        String modelo,
        String cor,
        String tipo,
        String agregado,
        String agregadoDocumento) {}