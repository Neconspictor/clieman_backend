package de.necon.dateman_backend.service;

import de.necon.dateman_backend.model.User;
import org.apache.commons.text.RandomStringGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
public class RegistrationListener  implements
        ApplicationListener<OnRegistrationCompleteEvent> {

    @Autowired
    private UserService userService;

    @Autowired
    private MessageSource messages;

    @Autowired
    private EmailServiceImpl emailService;

    RandomStringGenerator generator = new RandomStringGenerator.Builder()
            .withinRange('0', '9').build();

    @Override
    public void onApplicationEvent(OnRegistrationCompleteEvent event) {
        User user = event.getUser();
        String token = generator.generate(6);
        var verificationToken = userService.createVerificationToken(user, token);

        verificationToken.getToken();
        emailService.sendVerificationMessage(verificationToken);

    }
}
