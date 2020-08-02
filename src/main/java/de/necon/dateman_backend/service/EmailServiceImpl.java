package de.necon.dateman_backend.service;

import de.necon.dateman_backend.model.VerificationToken;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class EmailServiceImpl implements EmailService {

    private static String FROM = "noreply@dateman.com";
    private static final String EMAIL_ADDRESS_PROPERTY = "spring.mail.username";
    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    private Environment env;

    @PostConstruct
    public void init() {
        //send a test email for testing that everything is setup
        logger.info("testing email sending...");
        sendSimpleMessage(env.getProperty(EMAIL_ADDRESS_PROPERTY), "init test", "");
    }

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