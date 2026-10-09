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
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExtratorManifestoCsvService {

    private static final Logger log = LoggerFactory.getLogger(ExtratorManifestoCsvService.class);

    private static final int COLUNAS_MINIMAS = 60;
    private static final DateTimeFormatter FORMATO_ENTRADA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Uma linha malformada não derruba o arquivo: ela é descartada, registrada
     * no log (sem dados pessoais) e contada em {@code linhasIgnoradas}, para
     * entrar no total de rejeitados da resposta. Linhas totalmente em branco
     * são ignoradas sem contar.
     */
    public ResultadoExtracaoDTO extrairEMapear(InputStream csvInputStream) throws Exception {
        List<ManifestoMapeadoDTO> listaMapeada = new ArrayList<>();
        int ignoradas = 0;

        try (
                InputStreamReader reader = new InputStreamReader(csvInputStream, StandardCharsets.ISO_8859_1);
                CSVReader csvReader = new CSVReaderBuilder(reader)
                        .withCSVParser(new CSVParserBuilder().withSeparator(';').build())
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
                    log.warn("Linha {} do CSV ignorada: {}", csvReader.getLinesRead(), e.getClass().getSimpleName());
                }
            }
        }

        return new ResultadoExtracaoDTO(listaMapeada, ignoradas);
    }

    private ManifestoMapeadoDTO mapear(String[] linha) {
        if (linha.length < COLUNAS_MINIMAS) {
            throw new IllegalArgumentException("linha com menos de " + COLUNAS_MINIMAS + " colunas");
        }

        ManifestoMapeadoDTO dto = new ManifestoMapeadoDTO();

        dto.setmanifestoID(linha[0]);
        dto.setdata(LocalDate.parse(linha[2].trim(), FORMATO_ENTRADA).toString());
        dto.setnomeMotoristas(linha[3]);
        dto.setcpfMotorista(limparDocumento(linha[4]));
        dto.setnomeAgregado(linha[15]);
        dto.setcpfCnpjAgregado(limparDocumento(linha[16]));
        dto.setplacaVeiculo(linha[19]);
        dto.setstatus(linha[59]);

        dto.setvalorFrete(converterValor(linha[38]));
        dto.settotalDespesas(converterValor(linha[51]));
        dto.setsaldoAPagar(converterValor(linha[56]));

        return dto;
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

    /** Vazio vale 0.0; valor preenchido mas ilegível rejeita a linha (NumberFormatException). */
    private Double converterValor(String valor) {
        if (valor == null || valor.isBlank()) {
            return 0.0;
        }

        return Double.parseDouble(valor.trim().replace(".", "").replace(",", "."));
    }
}
