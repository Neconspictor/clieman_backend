package de.necon.dateman_backend.service;

import de.necon.dateman_backend.unit.VerificationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailServiceImpl {

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    Environment env;

    private static String FROM = "noreply@dateman.com";

    public void sendSimpleMessage(String to, String subject, String text) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(FROM);
        String email = preprocessEmail(to);
        message.setTo(email);
        message.setSubject(subject);
        message.setText(text);
        emailSender.send(message);
    }

    public void sendVerificationMessage(VerificationToken token) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(FROM);
        String email = preprocessEmail(token.getUser().getEmail());
        message.setTo(email);
        message.setSubject("Verification token");

        String text = "Your verification token is: " + token.getToken();
        message.setText(text);

        emailSender.send(message);
    }

    private String preprocessEmail(String email) {
        // if we have test environment we use a test email instead
        String testEmail = env.getProperty("dateman.test.email");
        if (testEmail != null) {
            return testEmail;
        }

        return email;
    }
}