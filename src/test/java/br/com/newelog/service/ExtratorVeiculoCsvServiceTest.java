package br.com.newelog.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import br.com.newelog.dto.ResultadoExtracaoCsvDTO;
import br.com.newelog.dto.VeiculoCsvDTO;

class ExtratorVeiculoCsvServiceTest {

    private static final String CABECALHO = "Placa;Marca;Modelo;Cor;Tipo Veículo;Agregado\n";

    private final ExtratorVeiculoCsvService servico = new ExtratorVeiculoCsvService();

    private InputStream csv(String conteudo, Charset charset) {
        return new ByteArrayInputStream(conteudo.getBytes(charset));
    }

    private InputStream csv(String conteudo) {
        return csv(conteudo, StandardCharsets.UTF_8);
    }

    // ---------- com o arquivo de exemplo (veiculos_teste_pequeno.csv) ----------

    private ResultadoExtracaoCsvDTO lerArquivoPequeno() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/veiculos_teste_pequeno.csv")) {
            assertNotNull(in, "veiculos_teste_pequeno.csv precisa estar em src/test/resources");
            return servico.extrair(in);
        }
    }

    @Test
    void leTodosOsVeiculosDoArquivoDeExemplo() throws Exception {
        ResultadoExtracaoCsvDTO r = lerArquivoPequeno();

        assertEquals(5, r.veiculos().size());
        assertTrue(r.erros().isEmpty());

        VeiculoCsvDTO primeiro = r.veiculos().get(0);
        assertEquals("MFS3836", primeiro.placa());
        assertEquals("Mercedes Benz", primeiro.marca());
        assertEquals("914", primeiro.modelo());
        assertEquals("branca", primeiro.cor());
        assertEquals("3/4", primeiro.tipo());
    }

    @Test
    void separaDocumentoDoNomeDoAgregado() throws Exception {
        VeiculoCsvDTO v = lerArquivoPequeno().veiculos().stream()
                .filter(x -> x.placa().equals("EIY9A14")).findFirst().orElseThrow();

        assertEquals("ROBERTO FELTRIN", v.agregado());
        assertEquals("04350172840", v.agregadoDocumento());
    }

    @Test
    void agregadoVazioViraNull() throws Exception {
        VeiculoCsvDTO v = lerArquivoPequeno().veiculos().stream()
                .filter(x -> x.placa().equals("FHQ0510")).findFirst().orElseThrow();

        assertNull(v.agregado());
        assertNull(v.agregadoDocumento());
    }

    // ---------- espaços, acentos, linhas vazias ----------

    @Test
    void removeEspacosDasPontasEDuplicadosEPoePlacaEmMaiuscula() throws Exception {
        var r = servico.extrair(csv(CABECALHO + " abc1d23 ; Volvo ;  VM   260 ;PRATA;Truck ou pesado;  JOSE   HILTON \n"));

        VeiculoCsvDTO v = r.veiculos().get(0);
        assertEquals("ABC1D23", v.placa());
        assertEquals("Volvo", v.marca());
        assertEquals("VM 260", v.modelo());
        assertEquals("JOSE HILTON", v.agregado());
    }

    @Test
    void preservaAcentosQuandoArquivoEstaEmWindows1252() throws Exception {
        var r = servico.extrair(csv(CABECALHO + "ABC1D23;Volvo;FH;PRATA;Cavalo mecânico;JOSE\n",
                Charset.forName("windows-1252")));

        assertEquals("Cavalo mecânico", r.veiculos().get(0).tipo());
    }

    @Test
    void preservaAcentosQuandoArquivoEstaEmUtf8() throws Exception {
        var r = servico.extrair(csv(CABECALHO + "ABC1D23;Volvo;FH;PRATA;Cavalo mecânico;JOSE\n"));

        assertEquals("Cavalo mecânico", r.veiculos().get(0).tipo());
    }

    @Test
    void ignoraLinhasVaziasEMantemONumeroRealDaLinha() throws Exception {
        var r = servico.extrair(csv(CABECALHO + "\n;;;;;\nABC1D23;Volvo;FH;PRATA;Truck ou pesado;JOSE\n"));

        assertEquals(1, r.veiculos().size());
        assertEquals(4, r.veiculos().get(0).linha());
        assertTrue(r.erros().isEmpty());
    }

    @Test
    void lePelaOrdemDoCabecalhoEIgnoraColunasExtras() throws Exception {
        String csv = "Cor;Combustível;Agregado;Tipo Veículo;Modelo;Marca;Placa\n"
                + "PRATA;Diesel S10;JOSE;Truck ou pesado;VM260;Volvo;ABC1D23\n";
        VeiculoCsvDTO v = servico.extrair(csv(csv)).veiculos().get(0);

        assertEquals("ABC1D23", v.placa());
        assertEquals("Volvo", v.marca());
        assertEquals("Truck ou pesado", v.tipo());
    }

    // ---------- erros ----------

    @Test
    void linhaComMenosColunasViraErroENaoExcecao() throws Exception {
        var r = servico.extrair(csv(CABECALHO + "ABC1D23;Volvo\n"));

        assertTrue(r.veiculos().isEmpty());
        assertEquals(1, r.erros().size());
        assertEquals(2, r.erros().get(0).linha());
    }

    @Test
    void cabecalhoSemColunaObrigatoriaLancaExcecao() {
        assertThrows(IllegalArgumentException.class,
                () -> servico.extrair(csv("Placa;Marca\nABC1D23;Volvo\n")));
    }

    @Test
    void arquivoVazioLancaExcecao() {
        assertThrows(IllegalArgumentException.class, () -> servico.extrair(csv("")));
    }
}