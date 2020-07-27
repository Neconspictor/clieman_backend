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
import java.util.Date;

import static de.necon.dateman_backend.config.ServiceErrorMessages.INVALID_ID;
import static de.necon.dateman_backend.config.ServiceErrorMessages.NO_USER;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ClientUnitTest {

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

        var user = createUser("test@email.com", "test");

        Client client = createClient("client id", user);

        var violations = validator.validate(client);
        assertTrue(violations.size() == 0);
    }

    @Test
    public void validationForUserWorks() {

        Client client = createClient("client id", null);

        var violations = validator.validate(client);
        assertTrue(violations.size() == 1);
        var constraint = violations.iterator().next();
        assertTrue(constraint.getMessageTemplate().equals(NO_USER));
    }


    @Test
    public void validationForIDWorks() {

        var user = createUser("test@email.com", "test");
        Client client = createClient(null, user);

        var violations = validator.validate(client);
        assertTrue(violations.size() == 1);
        var constraint = violations.iterator().next();
        assertTrue(constraint.getMessageTemplate().equals(INVALID_ID));
    }

    @Test
    public void userIsIgnoredByJSON() throws IOException {

        var user = createUser("test@email.com", "test");
        var client = createClient("client id", user);

        assertEquals(user, client.getId().getUser());

        var deserializedClient = deserialize(serialize(client));

        assertEquals(null, deserializedClient.getId().getUser());
    }


    /**
     * Ensures that the id field is serialized using the following scheme:
     * "id" : "id of the client"
     * @throws IOException
     */
    @Test
    public void serialization_valid() throws IOException {

        var user = createUser("test@email.com", "test");
        var client = createClient("cool id of the client", user);
        client.setBirthday(new Date(100));
        client.setEmail("email");
        client.setAddress("address");
        client.setForename("forename of client");
        client.setName("family name of client");

        var serialized = serialize(client);

        String expected = "{\"id\":\"" + client.getId().getId() + "\""
                + ",\"address\":\"" + client.getAddress() + "\""
                + ",\"birthday\":" + client.getBirthday().getTime()
                + ",\"email\":\"" + client.getEmail() + "\""
                + ",\"forename\":\"" + client.getForename() + "\""
                + ",\"name\":\"" + client.getName() + "\""
                 + "}";

        assertEquals(expected, serialized);
    }

    @Test
    public void id_isCustomDeserialized() throws IOException {

        var user = createUser("test@email.com", "test");
        var client = createClient("cool id of the client", user);

        var deserialized = deserialize(serialize(client));
        //Note: user field gets lost!
        deserialized.getId().setUser(user);
        assertEquals(deserialized, client);

    }

    private String serialize(Client client) throws IOException {
        StringWriter writer = new StringWriter();
        mapper.writeValue(writer, client);
        return writer.toString();
    }

    private Client deserialize(String serialized) throws IOException {
        return mapper.readValue(serialized, Client.class);
    }

    private static Client createClient(String id, User user) {
        return new Client(null,
                null,
                null,
                null,
                id,
                null,
                null,
                user);
    }

    private static User createUser(String email, String username) {
        return new User(email, "password", username, true);
    }
}