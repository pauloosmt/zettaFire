package com.br.zetta.fire.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class GeocodingService {

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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO};
    }

}
