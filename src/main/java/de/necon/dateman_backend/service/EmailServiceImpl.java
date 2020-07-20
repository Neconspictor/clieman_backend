package de.necon.dateman_backend.service;

import de.necon.dateman_backend.unit.VerificationToken;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    Environment env;

    private static String FROM = "noreply@dateman.com";

    @Override
    public void sendSimpleMessage(String to, String subject, String text) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(FROM);
        String email = preprocessEmail(to);
        message.setTo(email);
        message.setSubject(subject);
        message.setText(text);
        emailSender.send(message);
    }

    @Override
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
        Boolean noOverride = BooleanUtils.toBoolean(env.getProperty("dateman.test.no-override"));
        if (testEmail != null && !noOverride) {
            return testEmail;
        }

        return email;
    }
}