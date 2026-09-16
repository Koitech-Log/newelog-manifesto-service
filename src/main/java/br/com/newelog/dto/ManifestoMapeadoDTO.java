package br.com.newelog.dto

public class ManifestoMapeadoDTO{
    private String manifestoID;
    private String data;
    private String nomeMotorista;
    private String cpfMotorista;
    private String nomeAgregado;
    private String cpfCnpjAgregado;
    private String placaVeiculo;
    private String status;
    private Double valorFrete;
    private Double totalDespesas;
    private Double saldoAPagar;

    public ManifestoMapeadoDTO(){
    }

    public ManifestoMapeadoDTO(String manifestoID, String data, String nomeMotorista,
                                String cpfMotorista, String nomeAgregado,
                                String cpfCnpjAgregado, String placaVeiculo,
                                String status, Double valorFrete, Double totalDespesas, Double saldoAPagar){

                                    this.manifestoID = manifestoID;
                                    this.data = data;
                                    this.nomeMotorista = nomeMotorista;
                                    this.cpfMotorista = cpfMotorista;
                                    this.nomeAgregado = nomeAgregado;
                                    this.cpfCnpjAgregado = cpfCnpjAgregado;
                                    this.placaVeiculo = placaVeiculo;
                                    this.status = status;
                                    this.valorFrete = valorFrete;
                                    this.totalDespesas = totalDespesas;
                                    this.saldoAPagar = saldoAPagar;
                                }

                                //getters e setters
                                public String getManifestoId() { return manifestoId; }
                                public void setManifestoId(String manifestoId) { this.manifestoId = manifestoId; }

                                public String getdata() { return data; }
                                public void setdata(String data) { this.data = data; }

                                public String getnomeMotorista() { return nomeMotorista; }
                                public void setnomeMotoristas(String nomeMotorista) { this.nomeMotorista = nomeMotorista; }

                                public String getcpfMotorista() { return cpfMotorista; }
                                public void setcpfMotorista(String cpfMotorista) { this.cpfMotorista = cpfMotorista; }

                                public String getnomeAgregado() { return nomeAgregado; }
                                public void setnomeAgregado(String nomeAgregado) { this.nomeAgregado = nomeAgregado; }

                                public String getcpfCnpjAgregado() { return cpfCnpjAgregado; }
                                public void setcpfCnpjAgregado(String cpfCnpjAgregado) { this.cpfCnpjAgregado = cpfCnpjAgregado; }

                                public String getplacaVeiculo() { return placaVeiculo; }
                                public void setplacaVeiculo(String placaVeiculo) { this.placaVeiculo = placaVeiculo; }

                                public String getstatus() { return status; }
                                public void setstatus(String status) { this.status = status; }

                                public Double getvalorFrete() { return valorFrete; }
                                public void setvalorFrete(Double valorFrete) { this.valorFrete = valorFrete; }

                                public Double gettotalDespesas() { return totalDespesas; }
                                public void settotalDespesas(Double totalDespesas) { this.totalDespesas = totalDespesas; }

                                public Double getsaldoAPagar() { return saldoAPagar; }
                                public void setsaldoAPagar(Double saldoAPagar) { this.saldoAPagar = saldoAPagar; }
}