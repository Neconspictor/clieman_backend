package de.necon.clieman_backend.events;

import de.necon.clieman_backend.model.User;
import org.springframework.context.ApplicationEvent;

public class OnSendVerificationCodeEvent extends ApplicationEvent {

    private User user;

    /**
     * Create a new {@code OnRegistrationCompleteEvent}.
     *
     * @param user the user on which the event initially occurred or with
     *               which the event is associated (never {@code null})
     */
    public OnSendVerificationCodeEvent(User user) {
        super(user);
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
