
package br.com.newelog.service;

import br.com.newelog.dto.ManifestoMapeadoDTO;
import br.com.newelog.dto.ResultadoExtracaoDTO;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class ExtratorManifestoCsvService {

    private static final Logger log =
            LoggerFactory.getLogger(ExtratorManifestoCsvService.class);

    private static final int COLUNAS_MINIMAS = 60;

    private static final DateTimeFormatter FORMATO_ENTRADA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu", Locale.ROOT)
                    .withResolverStyle(ResolverStyle.STRICT);

    /**
     * Uma linha malformada não derruba o arquivo: ela é descartada,
     * registrada no log (sem dados pessoais) e contada em linhasIgnoradas.
     * Linhas totalmente em branco são ignoradas sem contar.
     */
    public ResultadoExtracaoDTO extrairEMapear(InputStream csvInputStream)
            throws Exception {

        List<ManifestoMapeadoDTO> listaMapeada = new ArrayList<>();
        int ignoradas = 0;

        try (
                InputStreamReader reader = new InputStreamReader(
                        csvInputStream,
                        StandardCharsets.ISO_8859_1
                );
                CSVReader csvReader = new CSVReaderBuilder(reader)
                        .withCSVParser(
                                new CSVParserBuilder()
                                        .withSeparator(';')
                                        .build()
                        )
                        .withSkipLines(1)
                        .build()
        ) {
            String[] linha;

            while ((linha = csvReader.readNext()) != null) {
                if (linhaEmBranco(linha)) {
                    continue;
                }

                try {
                    listaMapeada.add(mapear(linha));
                } catch (RuntimeException e) {
                    ignoradas++;
                    log.warn(
                            "Linha {} do CSV ignorada: {}",
                            csvReader.getLinesRead(),
                            e.getClass().getSimpleName()
                    );
                }
            }
        }

        return new ResultadoExtracaoDTO(listaMapeada, ignoradas);
    }

    private ManifestoMapeadoDTO mapear(String[] linha) {
        if (linha.length < COLUNAS_MINIMAS) {
            throw new IllegalArgumentException(
                    "linha com menos de " + COLUNAS_MINIMAS + " colunas"
            );
        }

        ManifestoMapeadoDTO dto = new ManifestoMapeadoDTO();

        dto.setmanifestoID(linha[0]);

        // STRICT rejeita datas inexistentes, como 31/02/2026.
        dto.setdata(
                LocalDate.parse(linha[2].trim(), FORMATO_ENTRADA).toString()
        );

        dto.setnomeMotoristas(linha[3]);
        dto.setcpfMotorista(limparDocumento(linha[4]));
        dto.setnomeAgregado(linha[15]);
        dto.setcpfCnpjAgregado(limparDocumento(linha[16]));
        dto.setplacaVeiculo(linha[19]);
        dto.setstatus(linha[59]);
        dto.setvalorFrete(converterValor(linha[38]));
        dto.setvalorPedagio(converterPedagio(linha[40]));
        dto.settotalDespesas(converterValor(linha[51]));
        dto.setsaldoAPagar(converterValor(linha[56]));

        return dto;
    }

    /**
     * Campo vazio significa valor ausente.
     * Valores preenchidos precisam seguir o formato numérico brasileiro.
     */
    private BigDecimal converterPedagio(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        String texto = valor.strip();

        if (texto.length() > 64) {
            throw new IllegalArgumentException(
                    "Pedágio excede o tamanho permitido"
            );
        }

        if (!texto.matches(
                "(?:\\d+|\\d{1,3}(?:\\.\\d{3})+)(?:,\\d{1,2})?"
        )) {
            throw new IllegalArgumentException(
                    "Pedágio em formato inválido"
            );
        }

        BigDecimal pedagio = new BigDecimal(
                texto.replace(".", "").replace(",", ".")
        );

        if (pedagio.compareTo(new BigDecimal("9999999999.99")) > 0) {
            throw new IllegalArgumentException(
                    "Pedágio excede o limite permitido"
            );
        }

        return pedagio.setScale(2);
    }

    private boolean linhaEmBranco(String[] linha) {
        for (String campo : linha) {
            if (campo != null && !campo.isBlank()) {
                return false;
            }
        }

        return true;
    }

    private String limparDocumento(String documento) {
        if (documento == null) {
            return null;
        }

        return documento.replaceAll("[^0-9]", "");
    }

    /**
     * Campo vazio vale 0.0.
     * Valor preenchido e ilegível rejeita a linha.
     */
    private Double converterValor(String valor) {
        if (valor == null || valor.isBlank()) {
            return 0.0;
        }

        String texto = valor.trim();

        if (!texto.matches(
                "(?:\\d+|\\d{1,3}(?:\\.\\d{3})+)(?:,\\d{1,2})?"
        )) {
            throw new IllegalArgumentException(
                    "Valor numérico em formato inválido"
            );
        }

        return Double.parseDouble(
                texto.replace(".", "").replace(",", ".")
        );
    }
}
