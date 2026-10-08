package br.com.newelog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class RestClientConfig {

    /**
     * O motoristas-service também exige JWT: repassa o Authorization da requisição
     * atual (a do usuário que enviou o CSV). Funciona porque o processamento do
     * upload roda na própria thread da requisição.
     */
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add((request, body, execution) -> {
            if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes atributos) {
                String autorizacao = atributos.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
                if (autorizacao != null) {
                    request.getHeaders().set(HttpHeaders.AUTHORIZATION, autorizacao);
                }
            }
            return execution.execute(request, body);
        });
        return restTemplate;
    }
}
