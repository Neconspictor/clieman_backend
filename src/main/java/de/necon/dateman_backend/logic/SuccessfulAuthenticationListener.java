package de.necon.dateman_backend.logic;

import de.necon.dateman_backend.events.SuccessfulAuthenticationEvent;
import de.necon.dateman_backend.model.User;
import de.necon.dateman_backend.service.JWTTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class SuccessfulAuthenticationListener implements
        ApplicationListener<SuccessfulAuthenticationEvent> {

    @Autowired
    private JWTTokenService tokenService;

    @Override
    public void onApplicationEvent(SuccessfulAuthenticationEvent event) {
        User user = event.getUser();

        String token = tokenService.createToken(user);
        var tokenHeader = tokenService.createTokenHeader(token);
        event.getResponse().addHeader(tokenHeader.getValue0(), tokenHeader.getValue1());
    }
}