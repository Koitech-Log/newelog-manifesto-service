package br.com.newelog.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import br.com.newelog.dto.ManifestoMapeadoDTO;
import br.com.newelog.dto.ResultadoExtracaoDTO;

class ExtratorManifestoCsvServiceTest {

    private final ExtratorManifestoCsvService extrator = new ExtratorManifestoCsvService();

    /** Linha com 60 colunas, preenchendo só as usadas pelo extrator. */
    private static String linha(String data, String frete) {
        String[] c = new String[60];
        java.util.Arrays.fill(c, "");
        c[0] = "M-1";
        c[2] = data;
        c[3] = "João da Silva";
        c[4] = "123.456.789-00";
        c[19] = "ABC1D23";
        c[38] = frete;
        c[59] = "EM VIAGEM";
        return String.join(";", c);
    }

    private ResultadoExtracaoDTO extrair(String... linhas) throws Exception {
        String csv = "cabecalho\n" + String.join("\n", linhas) + "\n";
        return extrator.extrairEMapear(new ByteArrayInputStream(csv.getBytes(StandardCharsets.ISO_8859_1)));
    }

    @Test
    void mapeiaLinhaValida() throws Exception {
        ResultadoExtracaoDTO resultado = extrair(linha("15/08/2026", "1.234,56"));

        assertThat(resultado.linhasIgnoradas()).isZero();
        assertThat(resultado.manifestos()).hasSize(1);
        ManifestoMapeadoDTO m = resultado.manifestos().get(0);
        assertThat(m.getdata()).isEqualTo("2026-08-15");
        assertThat(m.getcpfMotorista()).isEqualTo("12345678900");
        assertThat(m.getvalorFrete()).isEqualTo(1234.56);
    }

    @Test
    void dataInvalidaIgnoraSoAquelaLinha() throws Exception {
        ResultadoExtracaoDTO resultado = extrair(linha("31/02/2026", "100,00"), linha("16/08/2026", "200,00"));

        assertThat(resultado.manifestos()).hasSize(1);
        assertThat(resultado.linhasIgnoradas()).isEqualTo(1);
    }

    @Test
    void valorIlegivelIgnoraALinhaEmVezDeViraZero() throws Exception {
        ResultadoExtracaoDTO resultado = extrair(linha("15/08/2026", "abc"));

        assertThat(resultado.manifestos()).isEmpty();
        assertThat(resultado.linhasIgnoradas()).isEqualTo(1);
    }

    @Test
    void linhaCurtaContaComoIgnoradaEEmBrancoNao() throws Exception {
        ResultadoExtracaoDTO resultado = extrair("so;tres;colunas", "", linha("15/08/2026", "50,00"));

        assertThat(resultado.manifestos()).hasSize(1);
        assertThat(resultado.linhasIgnoradas()).isEqualTo(1);
    }

    @Test
    void valorVazioVale0() throws Exception {
        ResultadoExtracaoDTO resultado = extrair(linha("15/08/2026", ""));

        assertThat(resultado.manifestos().get(0).getvalorFrete()).isZero();
    }
}
