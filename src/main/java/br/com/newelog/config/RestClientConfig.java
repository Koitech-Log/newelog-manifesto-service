package br.com.newelog.config;

import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class RestClientConfig {

    /**
     * O motoristas-service também exige JWT: repassa o Authorization da requisição
     * atual (a do usuário que enviou o CSV). Funciona porque o processamento do
     * upload roda na própria thread da requisição.
     *
     * Timeouts evitam que um motoristas-service travado prenda a requisição
     * (e a thread) indefinidamente.
     */
    @Bean
    public RestTemplate restTemplate(
            @Value("${motoristas-service.timeout.connect-ms:5000}") long conexaoMs,
            @Value("${motoristas-service.timeout.read-ms:30000}") long leituraMs) {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofMillis(conexaoMs))
                .build();
        JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory(httpClient);
        fabrica.setReadTimeout(Duration.ofMillis(leituraMs));

        RestTemplate restTemplate = new RestTemplate(fabrica);
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
