package com.br.zetta.fire.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
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

    private final RestTemplate restTemplate = new RestTemplate();

    public BigDecimal[] getCoordinates(String street, String number, String city, String state, String cep) {
        try {
            //Formatando o endereço que vai buscar as coords
            String address = String.format("%s, %s, %s, %s, %s, Brazil", cep, number, street,city, state);

            //url da api
            String urlApi = "https://us1.locationiq.com/v1/search?key=" + apiKey +
                    "&street=" + URLEncoder.encode(street + ", " + number, StandardCharsets.UTF_8) +
                    "&city=" + URLEncoder.encode(city, StandardCharsets.UTF_8) +
                    "&postalcode=" + URLEncoder.encode(cep, StandardCharsets.UTF_8) +
                    "&country=Brazil&format=json&limit=1";


            System.out.println("URL enviada: " + urlApi);

            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    urlApi,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>(){}
            );
            if (response.getBody() != null && !response.getBody().isEmpty()) {
                Map<String, Object> result = response.getBody().get(0);
                BigDecimal lati = new BigDecimal(result.get("lat").toString());
                BigDecimal lon = new BigDecimal(result.get("lon").toString());

                return new BigDecimal[]{lati, lon};
            }
        } catch (Exception e) {
            throw new RuntimeException();
        }
        return new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO}; //retorno padrao
    }
}
