package com.br.zetta.fire.service;

import com.br.zetta.fire.exceptions.custom.ExternalServiceException;
import com.br.zetta.fire.exceptions.general.GlobalExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class GeocodingService {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Value("${locationiq.api.key}")
    private String apiKey;

    // Objeto do Spring usado para fazer requisições HTTP (chamar URLs externas)
    private final RestTemplate restTemplate;

    public GeocodingService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(60000); // 20 segundos de espera
        factory.setReadTimeout(60000);    // 20 segundos de leitura
        this.restTemplate = new RestTemplate(factory);
    }

    public BigDecimal[] getCoordinates(String street, String city, String state) {
        String queryCompleta = street + ", " + city + ", " + state + ", Brazil";
        BigDecimal[] coords = executeRequest(queryCompleta);

        if(coords[0].equals(BigDecimal.ZERO)) {
            String queryCidade = city + ", " + state + ", Brazil";
            coords = executeRequest(queryCidade);
        }

        return coords;
    }

    private BigDecimal[] executeRequest(String query) {


        try{
            String urlApi = "https://us1.locationiq.com/v1/search?key=" + apiKey +
                    "&q=" + URLEncoder.encode(query, StandardCharsets.UTF_8) +
                    "&format=json&limit=1";

            ResponseEntity<List<Map<String,Object>>> response = restTemplate.exchange(
                    urlApi,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );

            if(response.getBody() != null && !response.getBody().isEmpty()) {
                Map<String, Object> result = response.getBody().get(0);
                return new BigDecimal[] {
                        new BigDecimal(result.get("lat").toString()),
                        new BigDecimal(result.get("lon").toString())
                };
            }
        } catch (HttpClientErrorException.Unauthorized e) {
            logger.error("Chave da API LocationIQ inválida ou expirada.");
            throw new ExternalServiceException("Falha na autenticação com o serviço de geolocalização.");
        } catch (HttpClientErrorException.TooManyRequests e) {
            logger.warn("Limite de requisições da LocationIQ atingido.");
            throw new ExternalServiceException("Limite de busca excedido. Tente novamente mais tarde.");
        } catch (Exception e) {
            logger.error("Erro ao conectar com LocationIQ: {}", e.getMessage());
            throw new ExternalServiceException("Não foi possível obter as coordenadas no momento.");
        }
        return new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO};
    }

}
