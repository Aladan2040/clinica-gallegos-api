package com.gallegos.clinicagallegos.service;

import com.gallegos.clinicagallegos.dto.BrevoEmailRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class EmailService {

    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${brevo.api.url}")
    private String apiUrl;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    private final RestTemplate restTemplate;

    public EmailService() {
        this.restTemplate = new RestTemplate();
    }

    @Async
    public void enviarCorreo(String to, String subject, String body) {
        try {
            // 1. Configurar Headers (API Key)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiKey);
            headers.set("accept", "application/json");

            // 2. Convertir cuerpo a HTML básico
            String htmlBody = "<p>" + body.replace("\n", "<br>") + "</p>";

            // 3. Crear el objeto de solicitud
            BrevoEmailRequest request = new BrevoEmailRequest(
                    "Clinica Gallegos",
                    senderEmail,
                    to,
                    subject,
                    htmlBody
            );

            // 4. Enviar la petición POST
            HttpEntity<BrevoEmailRequest> entity = new HttpEntity<>(request, headers);
            restTemplate.postForObject(apiUrl, entity, String.class);

            System.out.println("✅ [Brevo API] Correo enviado a: " + to);

        } catch (Exception e) {
            System.err.println("❌ [Brevo API] Error enviando correo: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
