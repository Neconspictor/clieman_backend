package de.necon.dateman_backend.events;

import de.necon.dateman_backend.model.User;
import org.springframework.context.ApplicationEvent;

import javax.servlet.http.HttpServletResponse;

public class SuccessfulAuthenticationEvent extends ApplicationEvent {

    private User user;
    private HttpServletResponse response;

    /**
     * Create a new {@code OnRegistrationCompleteEvent}.
     *
     * @param user the user on which the event initially occurred or with
     *               which the event is associated (never {@code null})
     * @param response
     */
    public SuccessfulAuthenticationEvent(User user, HttpServletResponse response) {
        super(user);
        this.user = user;
        this.response = response;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public void setResponse(HttpServletResponse response) {
        this.response = response;
    }
}
