package de.necon.dateman_backend.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.necon.dateman_backend.model.Client;
import de.necon.dateman_backend.model.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import java.io.IOException;
import java.io.StringWriter;

import static de.necon.dateman_backend.config.ServiceErrorMessages.CLIENT_INVLAID_ID;
import static de.necon.dateman_backend.config.ServiceErrorMessages.NO_USER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class ClientIDUnitTest {

    private static Validator validator;

    @Autowired
    ObjectMapper mapper;

    @BeforeAll
    public static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }


    @Test
    public void valid() {

        Client.ID id = new Client.ID();

        id.setUser(new User("test@email.com", "password", "test", true));
        id.setId("an id");

        var violations = validator.validate(id);
        assertTrue(violations.size() == 0);
    }

    @Test
    public void id_NullNotAllowed() {

        Client.ID id = new Client.ID();

        id.setUser(new User("test@email.com", "password", "test", true));
        id.setId(null);

        var violations = validator.validate(id);
        assertTrue(violations.size() == 1);
        var constraint = violations.iterator().next();
        assertTrue(constraint.getMessageTemplate().equals(CLIENT_INVLAID_ID));
    }

    @Test
    public void id_EmptyNotAllowed() {

        Client.ID id = new Client.ID();

        id.setUser(new User("test@email.com", "password", "test", true));
        id.setId("");

        var violations = validator.validate(id);
        assertTrue(violations.size() == 1);
        var constraint = violations.iterator().next();
        assertTrue(constraint.getMessageTemplate().equals(CLIENT_INVLAID_ID));
    }

    @Test
    public void id_OnlySpacesNotAllowed() {

        Client.ID id = new Client.ID();

        id.setUser(new User("test@email.com", "password", "test", true));
        id.setId("   ");

        var violations = validator.validate(id);
        assertTrue(violations.size() == 1);
        var constraint = violations.iterator().next();
        assertTrue(constraint.getMessageTemplate().equals(CLIENT_INVLAID_ID));
    }

    @Test
    public void user_NullNotAllowed() {

        Client.ID id = new Client.ID();

        id.setUser(null);
        id.setId("id");

        var violations = validator.validate(id);
        assertTrue(violations.size() == 1);
        var constraint = violations.iterator().next();
        assertTrue(constraint.getMessageTemplate().equals(NO_USER));
    }

    @Test
    public void userIsIgnoredByJSON() throws IOException {

        var user = createUser("test@email.com", "test");
        Client.ID id = new Client.ID("client id", user);

        StringWriter writer = new StringWriter();
        mapper.writeValue(writer, id);
        var deserializedID = mapper.readValue(writer.toString(), Client.ID.class);

        assertEquals(null, deserializedID.getUser());
    }

    private static User createUser(String email, String username) {
        return new User(email, "password", username, true);
    }
}
