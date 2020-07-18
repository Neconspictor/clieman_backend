package de.necon.dateman_backend.factory;

import de.necon.dateman_backend.model.User;
import de.necon.dateman_backend.model.VerificationToken;

public final class ModelFactory {

    public static final String ANOTHER_VALID_EMAIL = "test2@email.com";


    public static VerificationToken createToken() {
        return new VerificationToken("token", createValidUser());
    }

    public static User createValidUser() {
        return new User("test@email.com", "password", "username", true);
    }

    public static User createSecondValidUser() {
        return new User(ANOTHER_VALID_EMAIL, "password", "username2", true);
    }
}
