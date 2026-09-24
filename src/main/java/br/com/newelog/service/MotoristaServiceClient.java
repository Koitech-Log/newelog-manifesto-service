package br.com.newelog.service;

import br.com.newelog.dto.ManifestoMapeadoDTO;
import br.com.newelog.dto.MotoristaCadastradoDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class MotoristaServiceClient {

    private final RestTemplate restTemplate;

    @Value("${motoristas-service.url}")
    private String motoristasServiceUrl;

    public MotoristaServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean validar(ManifestoMapeadoDTO manifesto) {
        // Prefixo /api: o endpoint real é /api/motoristas/validar-manifesto
        // (ver @RequestMapping("/api/motoristas") em MotoristaController).
        String url = motoristasServiceUrl + "/api/motoristas/validar-manifesto";

        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("manifestoID", manifesto.getmanifestoID());
        corpo.put("data", manifesto.getdata());
        corpo.put("nomeMotorista", manifesto.getnomeMotorista());
        corpo.put("cpfMotorista", manifesto.getcpfMotorista());
        corpo.put("nomeAgregado", manifesto.getnomeAgregado());
        corpo.put("cpfCnpjAgregado", manifesto.getcpfCnpjAgregado());
        corpo.put("placaVeiculo", manifesto.getplacaVeiculo());
        corpo.put("status", manifesto.getstatus());
        corpo.put("valorFrete", manifesto.getvalorFrete());
        corpo.put("totalDespesas", manifesto.gettotalDespesas());
        corpo.put("saldoAPagar", manifesto.getsaldoAPagar());

        try {
            restTemplate.postForEntity(url, corpo, Void.class);
            return true;
        } catch (HttpClientErrorException.BadRequest e) {
            System.out.println("Manifesto " + manifesto.getmanifestoID() + " rejeitado: " + e.getResponseBodyAsString());
            return false;
        }
    }

    /**
     * Efetiva o cadastro/atualização do motorista — chamado só depois que
     * validar() retorna true. Usa /cadastrar-de-manifesto (upsert por CPF),
     * não o POST /api/motoristas genérico, que não faz upsert e não resolve
     * a placa em um veículo existente. Retorna null se a chamada falhar.
     */
    public MotoristaCadastradoDTO cadastrar(ManifestoMapeadoDTO manifesto) {
        String url = motoristasServiceUrl + "/api/motoristas/cadastrar-de-manifesto";

        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("codigoExterno", manifesto.getmanifestoID());
        corpo.put("data", manifesto.getdata());
        corpo.put("nomeMotorista", manifesto.getnomeMotorista());
        corpo.put("cpfMotorista", manifesto.getcpfMotorista());
        corpo.put("placaVeiculo", manifesto.getplacaVeiculo());
        corpo.put("status", manifesto.getstatus());
        corpo.put("valorFrete", manifesto.getvalorFrete());

        try {
            return restTemplate.postForObject(url, corpo, MotoristaCadastradoDTO.class);
        } catch (HttpClientErrorException e) {
            System.out.println("Falha ao cadastrar motorista do manifesto " + manifesto.getmanifestoID()
                    + ": " + e.getResponseBodyAsString());
            return null;
        }
    }
}