package de.necon.clieman_backend.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.necon.clieman_backend.util.ModelFactory;
import de.necon.clieman_backend.model.Client;
import de.necon.clieman_backend.repository.ClientRepository;
import de.necon.clieman_backend.repository.EventRepository;
import de.necon.clieman_backend.repository.UserRepository;
import de.necon.clieman_backend.util.Json;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import java.io.IOException;
import java.util.Date;

import static de.necon.clieman_backend.config.ServiceErrorMessages.INVALID_ID;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ClientUnitTest {

    private static Validator validator;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    Json json;

    @Autowired
    ModelFactory modelFactory;

    @TestConfiguration
    public static class Config {
        @Bean
        Json json(@Autowired ObjectMapper mapper) {
            return new Json(mapper);
        }

        @Bean
        ModelFactory modelFactory(@Autowired UserRepository userRepository,
                                  @Autowired ClientRepository clientRepository,
                                  @Autowired EventRepository eventRepository) {
            return new ModelFactory(userRepository, clientRepository, eventRepository);
        }
    }

    @BeforeAll
    public static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }


    @Test
    public void valid() {

        var user = modelFactory.createUser("test@email.com", true, false);
        Client client = modelFactory.createClient("client id", user, false);

        var violations = validator.validate(client);
        assertTrue(violations.size() == 0);
    }

    @Test
    public void valid_userCanBeNull() {

        Client client = modelFactory.createClient("client id", null, false);

        var violations = validator.validate(client);
        assertTrue(violations.size() == 0);
    }


    @Test
    public void validationForIDWorks() {

        var user = modelFactory.createUser("test@email.com", true, false);
        Client client = modelFactory.createClient(null, user, false);

        var violations = validator.validate(client);
        assertTrue(violations.size() == 1);
        var constraint = violations.iterator().next();
        assertTrue(constraint.getMessageTemplate().equals(INVALID_ID));
    }

    @Test
    public void userIsIgnoredByJSON() throws IOException {

        var user = modelFactory.createUser("test@email.com", true, false);
        var client = modelFactory.createClient("client id", user, false);

        assertEquals(user, client.getId().getUser());

        var deserializedClient = json.deserialize(json.serialize(client), Client.class);

        assertEquals(null, deserializedClient.getId().getUser());
    }


    /**
     * Ensures that the serialization works as expected.
     * @throws IOException if an io error occurs.
     */
    @Test
    public void serialization_valid() throws IOException {

        var user = modelFactory.createUser("test@email.com", true, false);
        var client = modelFactory.createClient("cool id of the client", user, false);
        client.setBirthday(new Date(100));
        client.setEmail("email");
        client.setAddress("address");
        client.setForename("forename of client");
        client.setName("family name of client");

        var serialized = json.serialize(client);

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

        var user = modelFactory.createUser("test@email.com", true, false);
        var client = modelFactory.createClient("cool id of the client", user, false);

        var deserialized = json.deserialize(json.serialize(client), Client.class);
        //Note: user field gets lost!
        deserialized.getId().setUser(user);
        assertEquals(deserialized, client);

    }
}