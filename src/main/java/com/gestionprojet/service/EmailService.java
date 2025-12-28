package com.gestionprojet.service;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailService {

    // SMTP configuration - should be moved to a config file eventually
    private final String host = "smtp.gmail.com";
    private final String port = "587";
    private final String username = "votreemail@gmail.com"; // User should fill this
    private final String password = "votrepassword"; // User should fill this

    public void sendConfirmationEmail(String toAddress, String username) {
        System.out.println("📧 Tentative d'envoi d'email de confirmation à: " + toAddress);

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EmailService.this.username, EmailService.this.password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(this.username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toAddress));
            message.setSubject("Confirmation d'inscription - ProjectHub");

            String htmlContent = "<h1>Bienvenue sur ProjectHub !</h1>"
                    + "<p>Bonjour <strong>" + username + "</strong>,</p>"
                    + "<p>Nous sommes ravis de vous compter parmi nous. Votre compte a été créé avec succès.</p>"
                    + "<p>Vous pouvez maintenant vous connecter et commencer à gérer vos projets.</p>"
                    + "<br>"
                    + "<p>Cordialement,<br>L'équipe ProjectHub</p>";

            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);

            System.out.println("✅ Email de confirmation envoyé avec succès à " + toAddress);

        } catch (MessagingException e) {
            System.err.println("❌ Erreur lors de l'envoi de l'email: " + e.getMessage());
            // We don't throw exception here to not block the registration process
            // but in a production app we might want to handle it differently
        }
    }
}
