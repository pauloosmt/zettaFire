package com.br.zetta.fire.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class PushNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(PushNotificationService.class);
    private static final String EXPO_PUSH_URL = "https://exp.host/--/api/v2/push/send";
    private final RestTemplate restTemplate;

    public PushNotificationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void sendPushNotification(String to, String title, String body, String sound, String channelId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", "application/json");
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("to", to);
        payload.put("title", title);
        payload.put("body", body);
        payload.put("sound", sound);
        payload.put("channelId", channelId);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            restTemplate.postForObject(EXPO_PUSH_URL, request, String.class);
            System.out.println(">>> TENTATIVA DE ENVIO PARA O TOKEN: " + to);
            logger.info("Push notification enviada para o token: {}", to);
        } catch (Exception e) {
            System.out.println(">>> ERRO NO ENVIO: " + e.getMessage());
            logger.error("Falha ao enviar push para {}: {}", to, e.getMessage());
        }
    }
}