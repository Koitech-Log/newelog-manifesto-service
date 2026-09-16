package br.com.newelog.service;

import br.com.newelog.dto.ManifestoMapeadoDTO;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVParserBuilder;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExtratorManifestoCsvService{
    public List extrairEMapear(InputStream csvInputStream) throws Exception{
        List listaMapeada =  new ArrayList<>();

        try(var reader = new InputStreamReader(csvInputStream, StandardCharsets.ISO_8859_1);
            CSVReader csvReader = new CSVReaderBuilder(reader)
                    .withCSVParser(new CSVParserBuilder().withSeparator(',').build())
                    .withSkipLines(1)
                    .build()){
            String[] linha;
            while ((linha = csvReader.readNext()) != null){
                if(linha.length <60){
                    continue;
                }

                ManifestoMapeadoDTO dto = new ManifestoMapeadoDTO();

                dto.setmanifestoID(linha[0]);
                dto.setdata(linha[2]);
                dto.setnomeMotoristas(linha[3]);
                dto.setcpfMotorista(limparDocumento(linha[4]));
                dto.setnomeAgregado(linha[15]);
                dto.setcpfCnpjAgregado(limparDocumento(linha[16]));
                dto.setplacaVeiculo(linha[19]);
                dto.setstatus(linha[59]);

                dto.setvalorFrete(converterValor(linha[38]));
                dto.settotalDespesas(converterValor(linha[51]));
                dto.setsaldoAPagar(converterValor(linha[56]));

                listaMapeada.add(dto);
            }
        }

        return listaMapeada;
    }

    private String limparDocumento(String doc){
        if (doc == null) return null;
        return doc.replaceAll("[^0-9]", "");
    }

    private Double converterValor(String valorStr){
        if (valorStr == null || valorStr.isBlank()) return 0.0;
        try{
            return Double.parseDouble(valorStr.replace(".", "").replace(",", "."));
        } catch (NumberFormatException e){
            return 0.0;
        }
    }
}