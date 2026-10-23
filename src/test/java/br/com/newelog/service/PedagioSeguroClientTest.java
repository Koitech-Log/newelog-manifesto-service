package br.com.newelog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import java.math.BigDecimal;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import br.com.newelog.dto.ManifestoMapeadoDTO;
import static org.hamcrest.Matchers.containsString;

class PedagioSeguroClientTest {
    @ParameterizedTest
    @ValueSource(strings = {"0.00", "0.01", "165.87", "9999999999.99"})
    void transmitePedagioExatoSemUsarTotalDespesas(String pedagio) {
        var template = new RestTemplate();
        var server = MockRestServiceServer.bindTo(template).build();
        var client = new MotoristaServiceClient(template, "http://motoristas.test");
        var m = new ManifestoMapeadoDTO();
        m.setmanifestoID("18790");
        m.setvalorFrete(2129.70);
        m.setvalorPedagio(new BigDecimal(pedagio));
        m.settotalDespesas(0.0);
        server.expect(requestTo("http://motoristas.test/api/motoristas/cadastrar-de-manifesto"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(content().string(containsString("\"valorPedagio\":" + pedagio)))
            .andRespond(withSuccess("{\"cadastroValidado\":true}", MediaType.APPLICATION_JSON));
        assertThat(client.cadastrar(m)).isNotNull();
        server.verify();
    }
}
