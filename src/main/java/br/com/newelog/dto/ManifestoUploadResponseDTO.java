package br.com.newelog.dto;

import java.util.List;

public class ManifestoUploadResponseDTO {

    private String mensagem;
    private String nomeArquivo;
    private int totalProcessados;
    private int totalCadastrados;
    private int totalRejeitados;
    private List<MotoristaNovoDTO> novos;

    public ManifestoUploadResponseDTO(String mensagem, String nomeArquivo) {
        this.mensagem = mensagem;
        this.nomeArquivo = nomeArquivo;
    }

    public ManifestoUploadResponseDTO(String mensagem, String nomeArquivo, int totalProcessados,
                                       int totalCadastrados, int totalRejeitados, List<MotoristaNovoDTO> novos) {
        this.mensagem = mensagem;
        this.nomeArquivo = nomeArquivo;
        this.totalProcessados = totalProcessados;
        this.totalCadastrados = totalCadastrados;
        this.totalRejeitados = totalRejeitados;
        this.novos = novos;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public String getNomeArquivo() {
        return nomeArquivo;
    }

    public void setNomeArquivo(String nomeArquivo) {
        this.nomeArquivo = nomeArquivo;
    }

    public int getTotalProcessados() {
        return totalProcessados;
    }

    public void setTotalProcessados(int totalProcessados) {
        this.totalProcessados = totalProcessados;
    }

    public int getTotalCadastrados() {
        return totalCadastrados;
    }

    public void setTotalCadastrados(int totalCadastrados) {
        this.totalCadastrados = totalCadastrados;
    }

    public int getTotalRejeitados() {
        return totalRejeitados;
    }

    public void setTotalRejeitados(int totalRejeitados) {
        this.totalRejeitados = totalRejeitados;
    }

    public List<MotoristaNovoDTO> getNovos() {
        return novos;
    }

    public void setNovos(List<MotoristaNovoDTO> novos) {
        this.novos = novos;
    }
}