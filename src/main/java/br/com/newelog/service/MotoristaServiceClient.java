package br.com.newelog.service;

import br.com.newelog.dto.ManifestoMapeadoDTO;
import br.com.newelog.dto.MotoristaCadastradoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class MotoristaServiceClient {

    private static final Logger log = LoggerFactory.getLogger(MotoristaServiceClient.class);

    private final RestTemplate restTemplate;
    private final String motoristasServiceUrl;

    public MotoristaServiceClient(RestTemplate restTemplate,
                                  @Value("${motoristas-service.url}") String motoristasServiceUrl) {
        this.restTemplate = restTemplate;
        this.motoristasServiceUrl = motoristasServiceUrl;
    }

    /**
     * @return true se a linha é válida; false se o motoristas-service a rejeitou (400).
     * @throws MotoristasServiceException para falhas que invalidam o processamento
     *         inteiro: 401/403 (token ou perfil), 5xx ou serviço fora do ar.
     */
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
            log.warn("Manifesto {} rejeitado: {}", manifesto.getmanifestoID(), e.getResponseBodyAsString());
            return false;
        } catch (RestClientResponseException e) {
            throw traduzir(e);
        } catch (ResourceAccessException e) {
            throw indisponivel(e);
        }
    }

    /**
     * Efetiva o cadastro/atualização do motorista — chamado só depois que
     * validar() retorna true. Usa /cadastrar-de-manifesto (upsert por CPF),
     * não o POST /api/motoristas genérico.
     *
     * @return o detalhe do motorista, ou null se a linha foi rejeitada (400/409).
     * @throws MotoristasServiceException nos mesmos casos de {@link #validar}.
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
        } catch (HttpClientErrorException.BadRequest | HttpClientErrorException.Conflict e) {
            log.warn("Falha ao cadastrar motorista do manifesto {}: {}",
                    manifesto.getmanifestoID(), e.getResponseBodyAsString());
            return null;
        } catch (RestClientResponseException e) {
            throw traduzir(e);
        } catch (ResourceAccessException e) {
            throw indisponivel(e);
        }
    }

    private MotoristasServiceException traduzir(RestClientResponseException e) {
        int codigo = e.getStatusCode().value();
        if (codigo == 401) {
            return new MotoristasServiceException(HttpStatus.UNAUTHORIZED, "Sessão inválida ou expirada.");
        }
        if (codigo == 403) {
            return new MotoristasServiceException(HttpStatus.FORBIDDEN,
                    "Seu perfil não tem permissão para importar manifestos.");
        }
        log.error("motoristas-service respondeu {}: {}", codigo, e.getResponseBodyAsString());
        return new MotoristasServiceException(HttpStatus.BAD_GATEWAY,
                "O serviço de motoristas retornou um erro. Tente novamente em instantes.");
    }

    private MotoristasServiceException indisponivel(ResourceAccessException e) {
        log.error("motoristas-service inacessível: {}", e.getMessage());
        return new MotoristasServiceException(HttpStatus.BAD_GATEWAY,
                "O serviço de motoristas está indisponível. Tente novamente em instantes.");
    }
}
