package br.com.newelog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import br.com.newelog.dto.ManifestoMapeadoDTO;

/** Coluna Destino do CSV (índice 23) até o corpo enviado ao motoristas-service. */
class DestinoManifestoCsvTest {

    private final ExtratorManifestoCsvService extrator = new ExtratorManifestoCsvService();

    private static String linha(String destino) {
        String[] c = new String[60];
        Arrays.fill(c, "");
        c[0] = "M-1";
        c[2] = "15/04/2026";
        c[3] = "João da Silva";
        c[4] = "123.456.789-00";
        c[19] = "ABC1D23";
        c[23] = destino;
        c[38] = "100,00";
        c[59] = "EM VIAGEM";
        return String.join(";", c);
    }

    private ManifestoMapeadoDTO extrair(String destino) throws Exception {
        String csv = "cabecalho\n" + linha(destino) + "\n";
        // O arquivo real é ISO-8859-1: "ã" é 1 byte (0xE3), não UTF-8
        var resultado = extrator.extrairEMapear(
                new ByteArrayInputStream(csv.getBytes(StandardCharsets.ISO_8859_1)));
        assertThat(resultado.linhasIgnoradas()).isZero();
        return resultado.manifestos().get(0);
    }

    @Test
    void leDestinoComAcentoDoArquivoIso88591() throws Exception {
        assertThat(extrair("São Paulo").getdestino()).isEqualTo("São Paulo");
    }

    @Test
    void removeEspacosDasPontasEMantemListaDeCidades() throws Exception {
        assertThat(extrair("  São Paulo, Cotia  ").getdestino()).isEqualTo("São Paulo, Cotia");
    }

    @Test
    void destinoVazioOuSoEspacosViraNullSemRejeitarALinha() throws Exception {
        assertThat(extrair("").getdestino()).isNull();
        assertThat(extrair("   ").getdestino()).isNull();
    }

    @Test
    void destinoCom120CaracteresNaoEResumido() {
        String exato = "A".repeat(120);
        assertThat(ExtratorManifestoCsvService.resumirDestino(exato)).isEqualTo(exato);
    }

    @Test
    void destinoLongoCortaNaUltimaVirgulaQueCabeETerminaComReticencias() throws Exception {
        // Linha real do CSV abr-jun (158 caracteres)
        String real = "Americana, Sumaré, Campo Limpo Paulista, Limeira, Pirassununga, Campinas, Capivari, "
                + "Hortolândia, Indaiatuba, Piracicaba, Rio das Pedras, Santa Bárbara D'Oeste";

        String resumido = extrair(real).getdestino();

        assertThat(resumido)
                .isEqualTo("Americana, Sumaré, Campo Limpo Paulista, Limeira, Pirassununga, Campinas, Capivari, "
                        + "Hortolândia, Indaiatuba, Piracicaba…")
                .hasSizeLessThanOrEqualTo(120);
        assertThat(real).startsWith(resumido.substring(0, resumido.length() - 1));
    }

    @Test
    void destinoLongoSemVirgulaCortaEm119MaisReticencias() {
        String resumido = ExtratorManifestoCsvService.resumirDestino("X".repeat(200));

        assertThat(resumido).hasSize(120).isEqualTo("X".repeat(119) + "…");
    }

    @Test
    void enviaDestinoNaValidacaoENoCadastro() {
        var template = new RestTemplate();
        var server = MockRestServiceServer.bindTo(template).build();
        var client = new MotoristaServiceClient(template, "http://motoristas.test");
        var m = new ManifestoMapeadoDTO();
        m.setmanifestoID("M-1");
        m.setvalorFrete(100.0);
        m.setdestino("São Paulo");

        server.expect(requestTo("http://motoristas.test/api/motoristas/validar-manifesto"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.destino").value("São Paulo"))
                .andRespond(withSuccess());
        server.expect(requestTo("http://motoristas.test/api/motoristas/cadastrar-de-manifesto"))
                .andExpect(jsonPath("$.destino").value("São Paulo"))
                .andRespond(withSuccess("{\"cadastroValidado\":true}", MediaType.APPLICATION_JSON));

        assertThat(client.validar(m)).isTrue();
        assertThat(client.cadastrar(m)).isNotNull();
        server.verify();
    }

    @Test
    void semDestinoEnviaNull() {
        var template = new RestTemplate();
        var server = MockRestServiceServer.bindTo(template).build();
        var client = new MotoristaServiceClient(template, "http://motoristas.test");
        var m = new ManifestoMapeadoDTO();
        m.setmanifestoID("M-1");
        m.setvalorFrete(100.0);

        server.expect(requestTo("http://motoristas.test/api/motoristas/cadastrar-de-manifesto"))
                .andExpect(jsonPath("$.destino").value(Matchers.nullValue()))
                .andRespond(withSuccess("{\"cadastroValidado\":true}", MediaType.APPLICATION_JSON));

        assertThat(client.cadastrar(m)).isNotNull();
        server.verify();
    }
}
