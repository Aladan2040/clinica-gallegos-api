package com.gallegos.clinicagallegos.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void enviarCorreo(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        // IMPORTANTE: En Brevo, el "From" DEBE ser el correo que validaste al crear la cuenta
        message.setFrom("faridlazo1921@gmail.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        try {
            mailSender.send(message);
            System.out.println("✅ Correo enviado a: " + to);
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
        }
    }
}
