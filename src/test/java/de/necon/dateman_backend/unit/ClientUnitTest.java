package de.necon.dateman_backend.unit;

import de.necon.dateman_backend.model.Client;
import de.necon.dateman_backend.model.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import static de.necon.dateman_backend.config.ServiceErrorMessages.CLIENT_INVLAID_ID;
import static de.necon.dateman_backend.config.ServiceErrorMessages.NO_USER;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class ClientUnitTest {

    private static Validator validator;

    @BeforeAll
    public static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }


    @Test
    public void valid() {

        var user = new User("test@email.com", "password", "test", true);

        Client client = new Client(null,
                null,
                null,
                null,
                "clientID",
                null,
                null,
                user);

        var violations = validator.validate(client);
        assertTrue(violations.size() == 0);
    }

    @Test
    public void validationForUserWorks() {

        Client client = new Client(null,
                null,
                null,
                null,
                "clientID",
                null,
                null,
                null);

        var violations = validator.validate(client);
        assertTrue(violations.size() == 1);
        var constraint = violations.iterator().next();
        assertTrue(constraint.getMessageTemplate().equals(NO_USER));
    }


    @Test
    public void validationForIDWorks() {

        var user = new User("test@email.com", "password", "test", true);
        Client client = new Client(null,
                null,
                null,
                null,
                null,
                null,
                null,
                user);

        var violations = validator.validate(client);
        assertTrue(violations.size() == 1);
        var constraint = violations.iterator().next();
        assertTrue(constraint.getMessageTemplate().equals(CLIENT_INVLAID_ID));
    }
}