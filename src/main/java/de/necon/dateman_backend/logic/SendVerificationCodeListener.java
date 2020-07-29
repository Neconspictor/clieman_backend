package de.necon.dateman_backend.logic;

import de.necon.dateman_backend.events.OnSendVerificationCodeEvent;
import de.necon.dateman_backend.exception.ServiceError;
import de.necon.dateman_backend.model.User;
import de.necon.dateman_backend.service.EmailService;
import de.necon.dateman_backend.service.UserService;
import de.necon.dateman_backend.service.VerificationCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
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
        var verificationToken = userService.createVerificationToken(user, token);

        verificationToken.getToken();
        emailService.sendVerificationMessage(verificationToken);
    }
}
