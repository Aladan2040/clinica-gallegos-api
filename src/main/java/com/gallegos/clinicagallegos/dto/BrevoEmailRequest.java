package com.gallegos.clinicagallegos.dto;

import java.util.List;

public class BrevoEmailRequest {

    private Sender sender;
    private List<To> to;
    private String subject;
    private String htmlContent;

    public BrevoEmailRequest(String senderName, String senderEmail, String toEmail, String subject, String htmlContent) {
        this.sender = new Sender(senderName, senderEmail);
        this.to = List.of(new To(toEmail));
        this.subject = subject;
        this.htmlContent = htmlContent;
    }

    // Getters y Setters necesarios para la serialización JSON
    public Sender getSender() { return sender; }
    public void setSender(Sender sender) { this.sender = sender; }
    public List<To> getTo() { return to; }
    public void setTo(List<To> to) { this.to = to; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getHtmlContent() { return htmlContent; }
    public void setHtmlContent(String htmlContent) { this.htmlContent = htmlContent; }

    public static class Sender {
        private String name;
        private String email;

        public Sender(String name, String email) {
            this.name = name;
            this.email = email;
        }
        // Getters
        public String getName() { return name; }
        public String getEmail() { return email; }
    }

    public static class To {
        private String email;

        public To(String email) {
            this.email = email;
        }
        // Getters
        public String getEmail() { return email; }
    }

}
