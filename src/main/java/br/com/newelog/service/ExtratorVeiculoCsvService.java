package br.com.newelog.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

import br.com.newelog.dto.ErroLinhaCsvDTO;
import br.com.newelog.dto.ResultadoExtracaoCsvDTO;
import br.com.newelog.dto.VeiculoCsvDTO;

@Service
public class ExtratorVeiculoCsvService {

    private static final Charset WINDOWS_1252 = Charset.forName("windows-1252");
    private static final Pattern AGREGADO_COM_DOCUMENTO =
            Pattern.compile("^(.*\\S)\\s+([\\d./-]{11,18})$");

    private static final List<String> COLUNAS_OBRIGATORIAS =
            List.of("placa", "marca", "modelo", "cor", "tipo veiculo", "agregado");

    public ResultadoExtracaoCsvDTO extrair(InputStream entrada) throws IOException {
        String texto = decodificar(entrada.readAllBytes());
        char separador = texto.lines().findFirst().orElse("").contains(";") ? ';' : ',';

        List<VeiculoCsvDTO> veiculos = new ArrayList<>();
        List<ErroLinhaCsvDTO> erros = new ArrayList<>();

        try (CSVReader reader = new CSVReaderBuilder(new StringReader(texto))
                .withCSVParser(new CSVParserBuilder().withSeparator(separador).build())
                .build()) {

            String[] cabecalho = reader.readNext();
            if (cabecalho == null) {
                throw new IllegalArgumentException("Arquivo CSV vazio.");
            }

            Map<String, Integer> colunas = new HashMap<>();
            for (int i = 0; i < cabecalho.length; i++) {
                colunas.put(normalizarCabecalho(cabecalho[i]), i);
            }
            for (String obrigatoria : COLUNAS_OBRIGATORIAS) {
                if (!colunas.containsKey(obrigatoria)) {
                    throw new IllegalArgumentException("Coluna obrigatória ausente: " + obrigatoria);
                }
            }

            int numeroLinha = 1; 
            String[] campos;
            while ((campos = reader.readNext()) != null) {
                numeroLinha++;
                if (linhaVazia(campos)) continue;

                try {
                    String agregadoBruto = valor(campos, colunas.get("agregado"));
                    String agregado = agregadoBruto;
                    String documento = null;

                    if (agregadoBruto != null) {
                        Matcher m = AGREGADO_COM_DOCUMENTO.matcher(agregadoBruto);
                        if (m.matches()) {
                            String digitos = m.group(2).replaceAll("\\D", "");
                            if (digitos.length() == 11 || digitos.length() == 14) {
                                agregado = m.group(1);
                                documento = digitos;
                            }
                        }
                    }

                    String placa = valor(campos, colunas.get("placa"));
                    veiculos.add(new VeiculoCsvDTO(
                            numeroLinha,
                            placa == null ? null : placa.toUpperCase(),
                            valor(campos, colunas.get("marca")),
                            valor(campos, colunas.get("modelo")),
                            valor(campos, colunas.get("cor")),
                            valor(campos, colunas.get("tipo veiculo")),
                            agregado,
                            documento));
                } catch (IllegalArgumentException e) {
                    erros.add(new ErroLinhaCsvDTO(numeroLinha, e.getMessage()));
                }
            }
        } catch (CsvValidationException e) {
            throw new IOException("Erro ao ler o CSV: " + e.getMessage(), e);
        }

        return new ResultadoExtracaoCsvDTO(veiculos, erros);
    }

    private String valor(String[] campos, int indice) {
        if (indice >= campos.length) {
            throw new IllegalArgumentException("Linha com menos colunas que o cabeçalho.");
        }
        String limpo = campos[indice].replaceAll("[\\s\\u00A0]+", " ").trim();
        return limpo.isEmpty() ? null : limpo;
    }

    private boolean linhaVazia(String[] campos) {
        return Arrays.stream(campos).allMatch(c -> c == null || c.isBlank());
    }

    private String normalizarCabecalho(String cabecalho) {
        String limpo = cabecalho.replace("\uFEFF", "").replaceAll("[\\s\\u00A0]+", " ")
                .trim().toLowerCase();
        return Normalizer.normalize(limpo, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }

    private String decodificar(byte[] bytes) {
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes)).toString();
        } catch (CharacterCodingException e) {
            return new String(bytes, WINDOWS_1252);
        }
    }
}