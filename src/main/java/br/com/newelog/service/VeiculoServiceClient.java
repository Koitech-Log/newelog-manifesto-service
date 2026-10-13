package br.com.newelog.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import br.com.newelog.dto.ResultadoCadastroVeiculoDTO;
import br.com.newelog.dto.VeiculoCsvDTO;

/**
 * Chama POST /api/veiculos/cadastrar-de-csv no motoristas-service. O endpoint
 * exige JWT com perfil GESTOR, então o cabeçalho Authorization recebido do
 * front é repassado como veio — é o motoristas-service que decide se o usuário
 * pode importar. Falhas viram ResponseStatusException para o ApiExceptionHandler
 * devolver o status certo ao front.
 */
@Service
public class VeiculoServiceClient {

    private final RestTemplate restTemplate;

    @Value("${motoristas-service.url}")
    private String motoristasServiceUrl;

    public VeiculoServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResultadoCadastroVeiculoDTO cadastrar(List<VeiculoCsvDTO> veiculos, String authorization) {
        String url = motoristasServiceUrl + "/api/veiculos/cadastrar-de-csv";

        // Só os campos que o motoristas-service grava (Veiculo tem placa, marca e tipo).
        List<Map<String, Object>> itens = veiculos.stream().map(v -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("linha", v.linha());
            item.put("placa", v.placa());
            item.put("marca", v.marca());
            item.put("tipo", v.tipo());
            return item;
        }).toList();

        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("veiculos", itens);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (authorization != null && !authorization.isBlank()) {
            headers.set(HttpHeaders.AUTHORIZATION, authorization);
        }

        try {
            ResultadoCadastroVeiculoDTO resultado = restTemplate.postForObject(
                    url, new HttpEntity<>(corpo, headers), ResultadoCadastroVeiculoDTO.class);
            if (resultado == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "O motoristas-service respondeu sem dados ao cadastrar os veículos.");
            }
            return resultado;
        } catch (HttpClientErrorException e) {
            int status = e.getStatusCode().value();
            if (status == 401) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Sessão ausente ou expirada. Faça login novamente.");
            }
            if (status == 403) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Apenas gestores podem importar veículos.");
            }
            if (status == 400) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "O motoristas-service rejeitou os dados enviados.");
            }
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Falha ao cadastrar os veículos no motoristas-service (HTTP " + status + ").");
        } catch (HttpServerErrorException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "O motoristas-service falhou ao cadastrar os veículos (HTTP "
                            + e.getStatusCode().value() + ").");
        } catch (ResourceAccessException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Não foi possível conectar ao motoristas-service.");
        }
    }
}
