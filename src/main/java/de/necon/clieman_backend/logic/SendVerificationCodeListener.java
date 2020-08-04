package de.necon.clieman_backend.logic;

import de.necon.clieman_backend.events.OnSendVerificationCodeEvent;
import de.necon.clieman_backend.model.User;
import de.necon.clieman_backend.service.EmailService;
import de.necon.clieman_backend.service.UserService;
import de.necon.clieman_backend.service.VerificationCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class SendVerificationCodeListener implements
        ApplicationListener<OnSendVerificationCodeEvent> {

    @Autowired
    private UserService userService;

    @Autowired
    private MessageSource messages;

    @Autowired
    private EmailService emailService;

    @Autowired
    private VerificationCodeService codeService;

    @Override
    public void onApplicationEvent(OnSendVerificationCodeEvent event) {
        User user = event.getUser();

        String token = codeService.generateVerificationCode();


        userService.deleteExistingVerificationToken(user);
        var verificationToken = userService.createVerificationToken(user, token);

        verificationToken.getToken();
        emailService.sendVerificationMessage(verificationToken);
    }
}
