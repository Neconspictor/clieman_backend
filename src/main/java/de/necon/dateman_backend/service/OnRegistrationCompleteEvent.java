package de.necon.dateman_backend.service;

import de.necon.dateman_backend.unit.User;
import org.springframework.context.ApplicationEvent;

public class OnRegistrationCompleteEvent extends ApplicationEvent {

    private User user;

    /**
     * Create a new {@code OnRegistrationCompleteEvent}.
     *
     * @param user the user on which the event initially occurred or with
     *               which the event is associated (never {@code null})
     */
    public OnRegistrationCompleteEvent(User user) {
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
