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

    @Async // 💡 ESTO ES CLAVE: El método se ejecuta en un hilo separado
    public void enviarCorreo(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        // Asegúrate de que este remitente coincida con tu usuario de autenticación o sea válido
        message.setFrom("Clinica Dental Gallegos <noreply@clinica.com>");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        try {
            mailSender.send(message);
            System.out.println("Correo enviado con éxito a: " + to);
        } catch (Exception e) {
            // El error se loguea aquí, pero NO rompe la petición de agendar cita
            System.err.println("FALLO ENVÍO CORREO a " + to + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
