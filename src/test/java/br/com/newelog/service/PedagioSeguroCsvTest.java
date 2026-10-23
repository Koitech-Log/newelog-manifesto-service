package br.com.newelog.service;

import static org.assertj.core.api.Assertions.assertThat;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import br.com.newelog.dto.ResultadoExtracaoDTO;

class PedagioSeguroCsvTest {
    private final ExtratorManifestoCsvService extrator = new ExtratorManifestoCsvService();

    private String linha(String pedagio) {
        String[] c = new String[61];
        Arrays.fill(c, "");
        c[0] = "18790";
        c[2] = "01/07/2026";
        c[3] = "Motorista Teste";
        c[4] = "52998224725";
        c[19] = "ABC1D23";
        c[38] = "2.129,70";
        c[40] = pedagio;
        c[51] = "0,00";
        c[56] = "0,00";
        c[59] = "Finalizado";
        return String.join(";", c);
    }

    private ResultadoExtracaoDTO extrair(String... linhas) throws Exception {
        String csv = "cabecalho\n" + String.join("\n", linhas) + "\n";
        return extrator.extrairEMapear(new ByteArrayInputStream(csv.getBytes(StandardCharsets.ISO_8859_1)));
    }

    @ParameterizedTest
    @CsvSource(value = {"0,00|0.00", "0,01|0.01", "165,87|165.87", "1.234,56|1234.56",
        "1234|1234.00", "1.234|1234.00", "12,3|12.30", "9.999.999.999,99|9999999999.99",
        " 165,87 |165.87"}, delimiter = '|', ignoreLeadingAndTrailingWhitespace = false)
    void converteMoedaExatamente(String entrada, String esperado) throws Exception {
        var resultado = extrair(linha(entrada));
        assertThat(resultado.linhasIgnoradas()).isZero();
        assertThat(resultado.manifestos()).hasSize(1);
        var m = resultado.manifestos().get(0);
        assertThat(m.getvalorPedagio()).isEqualByComparingTo(new BigDecimal(esperado));
        assertThat(m.getvalorFrete()).isEqualTo(2129.70);
        assertThat(m.gettotalDespesas()).isZero();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t"})
    void vazioNaoViraZero(String entrada) throws Exception {
        var resultado = extrair(linha(entrada));
        assertThat(resultado.linhasIgnoradas()).isZero();
        assertThat(resultado.manifestos().get(0).getvalorPedagio()).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "-1,00", "NaN", "Infinity", "1e3", "12.34", "1,234",
        "10.000.000.000,00", "1,00,00", "R$ 10,00", "1 000,00"})
    void invalidoRejeitaSomenteALinha(String entrada) throws Exception {
        var resultado = extrair(linha(entrada), linha("165,87"));
        assertThat(resultado.linhasIgnoradas()).isEqualTo(1);
        assertThat(resultado.manifestos()).hasSize(1);
        assertThat(resultado.manifestos().get(0).getvalorPedagio()).isEqualByComparingTo("165.87");
    }

    @Test
    void campoExcessivamenteLongoRejeitaSomenteALinha() throws Exception {
        var resultado = extrair(linha("1".repeat(100000)), linha("165,87"));
        assertThat(resultado.linhasIgnoradas()).isEqualTo(1);
        assertThat(resultado.manifestos()).hasSize(1);
        assertThat(resultado.manifestos().get(0).getvalorPedagio()).isEqualByComparingTo("165.87");
    }

    @Test
    void confereOsDoisCsvReaisEMeses() throws Exception {
        String dir = System.getProperty("pedagio.csv.dir");
        Assumptions.assumeTrue(dir != null, "Informe -Dpedagio.csv.dir para os CSVs reais");
        Map<String, BigDecimal> somas = new TreeMap<>();
        int quantidade = 0;
        for (String nome : new String[]{"manifestos_jan-mar.csv", "manifestos_jul-ago.csv"}) {
            try (var input = Files.newInputStream(Path.of(dir, nome))) {
                var resultado = extrator.extrairEMapear(input);
                assertThat(resultado.linhasIgnoradas()).isZero();
                quantidade += resultado.manifestos().size();
                for (var m : resultado.manifestos()) {
                    assertThat(m.getvalorPedagio()).isNotNull();
                    somas.merge(m.getdata().substring(0, 7), m.getvalorPedagio(), BigDecimal::add);
                }
            }
        }
        assertThat(quantidade).isEqualTo(3194);
        assertThat(somas).containsExactlyInAnyOrderEntriesOf(Map.of(
            "2026-04", new BigDecimal("94059.31"),
            "2026-05", new BigDecimal("66565.34"),
            "2026-06", new BigDecimal("51770.25"),
            "2026-07", new BigDecimal("41327.54"),
            "2026-08", new BigDecimal("48587.98")));
        System.out.println("CSV_REAIS_VERIFICADOS: " + quantidade + " linhas, pedagios=" + somas);
    }
}
