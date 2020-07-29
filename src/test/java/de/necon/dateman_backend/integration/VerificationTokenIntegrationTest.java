package de.necon.dateman_backend.integration;

import com.icegreen.greenmail.store.FolderException;
import de.necon.dateman_backend.events.OnSendVerificationCodeEvent;
import de.necon.dateman_backend.extensions.TestSmtpServer;
import de.necon.dateman_backend.listeners.ResetDatabaseTestExecutionListener;
import de.necon.dateman_backend.logic.SendVerificationCodeListener;
import de.necon.dateman_backend.model.User;
import de.necon.dateman_backend.repository.UserRepository;
import de.necon.dateman_backend.repository.VerificationTokenRepository;
import de.necon.dateman_backend.service.UserService;
import de.necon.dateman_backend.service.VerificationCodeService;
import de.necon.dateman_backend.service.VerificationCodeServiceImpl;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.mail.MessagingException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@TestExecutionListeners(mergeMode =
        TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS,
        listeners = {ResetDatabaseTestExecutionListener.class}
)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class VerificationTokenIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(VerificationTokenIntegrationTest.class);

    @Autowired
    private SendVerificationCodeListener sendVerificationCodeListener;

    @Autowired
    UserRepository userRepository;

    @Autowired
    VerificationTokenRepository verificationTokenRepository;

    @Autowired
    UserService userService;

    private static TestSmtpServer testSmtpServer;
    private static int MAIL_PORT;

    @BeforeAll
    public static void staticSetup(@Autowired Environment env) {
        MAIL_PORT = Integer.parseInt(env.getProperty("spring.mail.port"));
        testSmtpServer = new TestSmtpServer(MAIL_PORT);
    }

    @AfterAll
    public static void staticShutdown(@Autowired Environment env) {
        testSmtpServer.stop();
    }

    @BeforeEach
    //@Override
    public void setup() throws FolderException {
        testSmtpServer.reset();
    }

    @Test
    public void emailSendForUser() throws MessagingException, IOException {

        String email = "test@email.com";
        var user = new User(email, "password", null, false);
        userRepository.saveAndFlush(user);
        sendVerificationCode(user);

        var messages = testSmtpServer.getMessages();
        assertTrue(messages.length == 1);

        var message = messages[0];
        var recipients = message.getAllRecipients();
        assertTrue(recipients.length == 1);
        assertEquals(email, recipients[0].toString());
        var content = (String)message.getContent();

        var optionalToken = verificationTokenRepository.findByUser(user);
        assertTrue(optionalToken.isPresent());

        var token = optionalToken.get();

        assertTrue(content.contains(token.getToken()));
    }

    /*@Test
    public void senginMultipleTimesIsAllowed() throws MessagingException, IOException {

        String email = "test@email.com";
        var user = new User(email, "password", null, false);
        userRepository.saveAndFlush(user);
        sendVerificationCode(user);
        sendVerificationCode(user);

        var optionalToken = verificationTokenRepository.findByUser(user);
        assertTrue(optionalToken.isPresent());
    }*/

    private void sendVerificationCode(User user) {
        OnSendVerificationCodeEvent event = new OnSendVerificationCodeEvent(user);
        sendVerificationCodeListener.onApplicationEvent(event);
    }
}