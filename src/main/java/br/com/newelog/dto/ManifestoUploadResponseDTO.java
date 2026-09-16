package br.com.newelog.dto;

public class ManifestoUploadResponseDTO {

    private String mensagem;
    private String nomeArquivo;

    public ManifestoUploadResponseDTO(String mensagem, String nomeArquivo) {
        this.mensagem = mensagem;
        this.nomeArquivo = nomeArquivo;
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
}