package de.necon.dateman_backend.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.necon.dateman_backend.model.Client;
import de.necon.dateman_backend.model.Event;
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
import java.util.HashSet;
import java.util.Set;

import static de.necon.dateman_backend.config.ServiceErrorMessages.NO_USER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class EventUnitTest {

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
        var event = createValidEvent();
        var violations = validator.validate(event);
        assertTrue(violations.size() == 0);
    }

    @Test
    public void id_valid() {
        var user = createUser("test@email.com", "test");

        Event event = createEvent("event id", user, null);

        var violations = validator.validate(event);
        assertTrue(violations.size() == 0);
    }

    @Test
    public void id_invalid() {
        var user = createUser("test@email.com", "test");

        Event event = createEvent("event id", null, null);

        var violations = validator.validate(event);
        assertTrue(violations.size() == 1);

        var constraint = violations.iterator().next();
        assertTrue(constraint.getMessageTemplate().equals(NO_USER));
    }

    @Test
    public void userIsIgnoredByJSON() throws IOException {

        var event = createValidEvent();
        var deserialized = serializeDeserialize(event);
        assertEquals(null, deserialized.getId().getUser());
    }


    @Test
    public void JSONConversionWorks() throws IOException {

        var event = createValidEvent();
        var user = event.getId().getUser();
        var deserialized = serializeDeserialize(event);
        propagateUser(deserialized, user);

        assertEquals(event, deserialized);
    }

    @Test
    public void JSONConversion_NoIdWorks() throws IOException {

        var event = createValidEvent();
        var user = event.getId().getUser();
        event.setId(null);
        var deserialized = serializeDeserialize(event);
        propagateUser(deserialized, user);

        event.setId(new ID(null, user));

        assertEquals(event, deserialized);
    }

    @Test
    public void JSONConversion_NoClientsWorks() throws IOException {

        var event = createValidEvent();
        var user = event.getId().getUser();
        event.setClients(null);
        var deserialized = serializeDeserialize(event);
        propagateUser(deserialized, user);

        event.setClients(new HashSet<>());

        assertEquals(event, deserialized);
    }

    private static void propagateUser(Event event, User user) {
        event.getId().setUser(user);
        event.getClients().forEach(c -> {
            c.getId().setUser(user);
        });
    }


    private static Event createValidEvent() {
        var user = createUser("test@email.com", "test");
        Set<Client> clients = new HashSet<>();
        clients.add(createClient("client id", user));
        clients.add(createClient("client id2", user));

        return  createEvent("event id", user, clients);
    }

    private Event serializeDeserialize(Event event) throws IOException {
        StringWriter writer = new StringWriter();
        mapper.writeValue(writer, event);
        return mapper.readValue(writer.toString(), Event.class);
    }


    private static Event createEvent(String id, User user, Set<Client> clients) {
        return new Event(null,
                null,
                null,
                clients,
                null,
                id,
                null,
                user);
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
        User user = new User(email, "password", username, true);
        user.setId(0L);
        return user;
    }
}