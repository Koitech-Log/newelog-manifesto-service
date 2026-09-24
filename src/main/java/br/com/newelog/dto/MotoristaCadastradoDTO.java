package br.com.newelog.dto;

/**
 * Espelha (parcialmente) MotoristaDetalheDTO do motoristas-service — usado só
 * para ler o resultado de POST /api/motoristas/cadastrar-de-manifesto e saber
 * se o motorista era novo (cadastroValidado=false) ou já existia.
 */
public class MotoristaCadastradoDTO {

    private String nome;
    private String placaVeiculo;
    private String status;
    private boolean cadastroValidado;

    public MotoristaCadastradoDTO() {
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

    public boolean isCadastroValidado() {
        return cadastroValidado;
    }

    public void setCadastroValidado(boolean cadastroValidado) {
        this.cadastroValidado = cadastroValidado;
    }
}
