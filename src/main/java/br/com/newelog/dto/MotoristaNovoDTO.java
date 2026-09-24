package br.com.newelog.dto;

/**
 * Versão enxuta de um motorista recém-cadastrado a partir do manifesto,
 * devolvida ao front-end na tela de importação.
 */
public class MotoristaNovoDTO {

    private String nome;
    private String placaVeiculo;
    private String status;

    public MotoristaNovoDTO() {
    }

    public MotoristaNovoDTO(String nome, String placaVeiculo, String status) {
        this.nome = nome;
        this.placaVeiculo = placaVeiculo;
        this.status = status;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getPlacaVeiculo() {
        return placaVeiculo;
    }

    public void setPlacaVeiculo(String placaVeiculo) {
        this.placaVeiculo = placaVeiculo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
