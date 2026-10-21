package br.com.newelog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import br.com.newelog.dto.ManifestoMapeadoDTO;

class MotoristaServiceClientTest {

    private static final String BASE = "http://motoristas.test";
    private static final String VALIDAR = BASE + "/api/motoristas/validar-manifesto";
    private static final String CADASTRAR = BASE + "/api/motoristas/cadastrar-de-manifesto";

    private MockRestServiceServer server;
    private MotoristaServiceClient client;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        client = new MotoristaServiceClient(restTemplate, BASE);
    }

    private static ManifestoMapeadoDTO manifesto() {
        ManifestoMapeadoDTO m = new ManifestoMapeadoDTO();
        m.setmanifestoID("M-1");
        m.setdata("2026-08-15");
        m.setnomeMotoristas("João da Silva");
        m.setcpfMotorista("12345678900");
        m.setplacaVeiculo("ABC1D23");
        m.setvalorFrete(100.0);
        m.settotalDespesas(10.0);
        return m;
    }

    @Test
    void validarAceitaQuandoRespondeOk() {
        server.expect(requestTo(VALIDAR)).andExpect(method(HttpMethod.POST)).andRespond(withSuccess());

        assertThat(client.validar(manifesto())).isTrue();
        server.verify();
    }

    @Test
    void validarRejeitaQuando400() {
        server.expect(requestTo(VALIDAR)).andRespond(
                withBadRequest().contentType(MediaType.APPLICATION_JSON).body("{\"mensagem\":\"invalido\"}"));

        assertThat(client.validar(manifesto())).isFalse();
    }

    @Test
    void validarPropaga401() {
        server.expect(requestTo(VALIDAR)).andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        assertThatThrownBy(() -> client.validar(manifesto()))
                .isInstanceOf(MotoristasServiceException.class)
                .extracting("status").isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void validarPropaga403() {
        server.expect(requestTo(VALIDAR)).andRespond(withStatus(HttpStatus.FORBIDDEN));

        assertThatThrownBy(() -> client.validar(manifesto()))
                .isInstanceOf(MotoristasServiceException.class)
                .extracting("status").isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void erro5xxViraBadGatewayEMensagemNaoVazaDetalhes() {
        server.expect(requestTo(VALIDAR)).andRespond(
                withStatus(HttpStatus.INTERNAL_SERVER_ERROR).body("stacktrace interno"));

        assertThatThrownBy(() -> client.validar(manifesto()))
                .isInstanceOf(MotoristasServiceException.class)
                .hasMessageNotContaining("stacktrace")
                .extracting("status").isEqualTo(HttpStatus.BAD_GATEWAY);
    }

    @Test
    void cadastrarDevolveNullQuando409() {
        server.expect(requestTo(CADASTRAR)).andRespond(withStatus(HttpStatus.CONFLICT));

        assertThat(client.cadastrar(manifesto())).isNull();
    }

    @Test
    void cadastrarPropaga401() {
        server.expect(requestTo(CADASTRAR)).andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        assertThatThrownBy(() -> client.cadastrar(manifesto()))
                .isInstanceOf(MotoristasServiceException.class);
    }
}
