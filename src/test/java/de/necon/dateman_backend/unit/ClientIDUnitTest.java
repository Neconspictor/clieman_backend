package de.necon.dateman_backend.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.necon.dateman_backend.model.ID;
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

import static de.necon.dateman_backend.config.ServiceErrorMessages.INVALID_ID;
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

        ID id = new ID();

        id.setUser(new User("test@email.com", "password", "test", true));
        id.setId("an id");

        var violations = validator.validate(id);
        assertTrue(violations.size() == 0);
    }

    @Test
    public void id_NullNotAllowed() {

        ID id = new ID();

        id.setUser(new User("test@email.com", "password", "test", true));
        id.setId(null);

        var violations = validator.validate(id);
        assertTrue(violations.size() == 1);
        var constraint = violations.iterator().next();
        assertTrue(constraint.getMessageTemplate().equals(INVALID_ID));
    }

    @Test
    public void id_EmptyNotAllowed() {

        ID id = new ID();

        id.setUser(new User("test@email.com", "password", "test", true));
        id.setId("");

        var violations = validator.validate(id);
        assertTrue(violations.size() == 1);
        var constraint = violations.iterator().next();
        assertTrue(constraint.getMessageTemplate().equals(INVALID_ID));
    }

    @Test
    public void id_OnlySpacesNotAllowed() {

        ID id = new ID();

        id.setUser(new User("test@email.com", "password", "test", true));
        id.setId("   ");

        var violations = validator.validate(id);
        assertTrue(violations.size() == 1);
        var constraint = violations.iterator().next();
        assertTrue(constraint.getMessageTemplate().equals(INVALID_ID));
    }

    @Test
    public void user_NullAllowed() {

        ID id = new ID();

        id.setUser(null);
        id.setId("id");

        var violations = validator.validate(id);
        assertTrue(violations.size() == 0);
    }

    @Test
    public void userIsIgnoredByJSON() throws IOException {

        var user = createUser("test@email.com", "test");
        ID id = new ID("client id", user);

        StringWriter writer = new StringWriter();
        mapper.writeValue(writer, id);
        var deserializedID = mapper.readValue(writer.toString(), ID.class);

        assertEquals(null, deserializedID.getUser());
    }

    private static User createUser(String email, String username) {
        return new User(email, "password", username, true);
    }
}
