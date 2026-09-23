package br.com.newelog.service;

import br.com.newelog.dto.ManifestoMapeadoDTO;
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
}