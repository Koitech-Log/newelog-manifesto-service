package br.com.newelog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import br.com.newelog.dto.ManifestoMapeadoDTO;

/** Coluna Manifesto do CSV enviada como numeroManifesto (identifica a viagem). */
class NumeroManifestoClientTest {

    private static final String BASE = "http://motoristas.test";

    private static ManifestoMapeadoDTO manifesto(String numero) {
        var m = new ManifestoMapeadoDTO();
        m.setmanifestoID(numero);
        m.setvalorFrete(277.0);
        return m;
    }

    @Test
    void enviaONumeroDaColunaManifestoNaValidacaoENoCadastro() throws Exception {
        String[] c = new String[60];
        Arrays.fill(c, "");
        c[0] = "17156";
        c[2] = "10/04/2026";
        c[4] = "529.982.247-25";
        c[38] = "277,00";
        String csv = "cabecalho\n" + String.join(";", c) + "\n";
        ManifestoMapeadoDTO lido = new ExtratorManifestoCsvService()
                .extrairEMapear(new ByteArrayInputStream(csv.getBytes(StandardCharsets.ISO_8859_1)))
                .manifestos().get(0);

        var template = new RestTemplate();
        var server = MockRestServiceServer.bindTo(template).build();
        server.expect(requestTo(BASE + "/api/motoristas/validar-manifesto"))
                .andExpect(jsonPath("$.numeroManifesto").value("17156"))
                .andRespond(withSuccess());
        server.expect(requestTo(BASE + "/api/motoristas/cadastrar-de-manifesto"))
                .andExpect(jsonPath("$.numeroManifesto").value("17156"))
                .andRespond(withSuccess("{\"cadastroValidado\":true}", MediaType.APPLICATION_JSON));

        var client = new MotoristaServiceClient(template, BASE);
        assertThat(client.validar(lido)).isTrue();
        assertThat(client.cadastrar(lido)).isNotNull();
        server.verify();
    }

    @Test
    void removeEspacosDoNumero() {
        var template = new RestTemplate();
        var server = MockRestServiceServer.bindTo(template).build();
        server.expect(requestTo(BASE + "/api/motoristas/cadastrar-de-manifesto"))
                .andExpect(jsonPath("$.numeroManifesto").value("17156"))
                .andRespond(withSuccess("{\"cadastroValidado\":true}", MediaType.APPLICATION_JSON));

        new MotoristaServiceClient(template, BASE).cadastrar(manifesto("  17156 "));
        server.verify();
    }

    @Test
    void numeroVazioVaiComoNull() {
        var template = new RestTemplate();
        var server = MockRestServiceServer.bindTo(template).build();
        server.expect(requestTo(BASE + "/api/motoristas/cadastrar-de-manifesto"))
                .andExpect(jsonPath("$.numeroManifesto").value(Matchers.nullValue()))
                .andRespond(withSuccess("{\"cadastroValidado\":true}", MediaType.APPLICATION_JSON));

        new MotoristaServiceClient(template, BASE).cadastrar(manifesto("  "));
        server.verify();
    }
}
