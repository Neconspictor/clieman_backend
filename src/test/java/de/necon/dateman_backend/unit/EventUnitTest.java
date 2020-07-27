package de.necon.dateman_backend.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.necon.dateman_backend.model.Client;
import de.necon.dateman_backend.model.Event;
import de.necon.dateman_backend.model.ID;
import de.necon.dateman_backend.model.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

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

        event.setClients(new ArrayList<>());

        assertEquals(event, deserialized);
    }

    @Test
    public void JSONConversion_ExpectedSerialization() throws IOException {

        List<String> clientIDs = List.of("client1", "client2");

        var event = createValidEvent("eventID", clientIDs);

        var serialized = serialize(event);

        String expected = "{\"id\":\"eventID\",\"clients\":[\"client1\",\"client2\"]}";
        Assertions.assertEquals(expected, serialized);
    }

    private static void propagateUser(Event event, User user) {
        event.getId().setUser(user);
        event.getClients().forEach(c -> {
            c.getId().setUser(user);
        });
    }


    private static Event createValidEvent() {
        return createValidEvent("eventID", List.of("client1", "client2"));
    }

    private static Event createValidEvent(String eventID, List<String> clientIds) {
        var user = createUser("test@email.com", "test");
        List<Client> clients = new ArrayList<>();
        clientIds.forEach(id -> {
            clients.add(createClient(id, user));
        });

        return  createEvent(eventID, user, clients);
    }

    private Event serializeDeserialize(Event event) throws IOException {
        return mapper.readValue(serialize(event), Event.class);
    }

    private String serialize(Event event) throws IOException {
        StringWriter writer = new StringWriter();
        mapper.writeValue(writer, event);
        return writer.toString();
    }


    private static Event createEvent(String id, User user, List<Client> clients) {
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