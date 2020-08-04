package de.necon.clieman_backend.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.necon.clieman_backend.model.Client;
import de.necon.clieman_backend.model.Event;
import de.necon.clieman_backend.model.ID;
import de.necon.clieman_backend.repository.ClientRepository;
import de.necon.clieman_backend.repository.EventRepository;
import de.necon.clieman_backend.repository.UserRepository;
import de.necon.clieman_backend.util.ModelFactory;
import org.junit.jupiter.api.Assertions;
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
import java.io.StringWriter;
import java.util.*;

import static de.necon.clieman_backend.config.ServiceErrorMessages.INVALID_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class EventUnitTest {

    private static Validator validator;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    ModelFactory modelFactory;

    @TestConfiguration
    public static class Config {

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
        var event = createValidEvent();
        var violations = validator.validate(event);
        assertTrue(violations.size() == 0);
    }

    @Test
    public void id_valid() {
        var user = modelFactory.createUser("test@email.com", true, false);
        Event event = modelFactory.createEvent("event id", user, new ArrayList<>());

        var violations = validator.validate(event);
        assertTrue(violations.size() == 0);
    }

    @Test
    public void id_invalid() {

        Event event = modelFactory.createEvent(null, null, new ArrayList<>());

        var violations = validator.validate(event);
        assertTrue(violations.size() == 1);

        var constraint = violations.iterator().next();
        assertTrue(constraint.getMessageTemplate().equals(INVALID_ID));
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
        deserialized.setUser(user);

        assertEquals(event, deserialized);
    }

    @Test
    public void JSONConversion_NoIdWorks() throws IOException {

        var event = createValidEvent();
        var user = event.getId().getUser();
        event.setId(null);
        var deserialized = serializeDeserialize(event);
        deserialized.setUser(user);

        event.setId(new ID(null, user));

        assertEquals(event, deserialized);
    }

    @Test
    public void JSONConversion_NoClientsWorks() throws IOException {

        var event = createValidEvent();
        var user = event.getId().getUser();
        event.setClients(null);
        var deserialized = serializeDeserialize(event);
        deserialized.setUser(user);

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

    private Event createValidEvent() {
        return createValidEvent("eventID", List.of("client1", "client2"));
    }

    private Event createValidEvent(String eventID, List<String> clientIds) {
        var user = modelFactory.createUser("test@email.com", true, false);
        List<Client> clients = new ArrayList<>();
        clientIds.forEach(id -> {
            clients.add(modelFactory.createClient(id, user, false));
        });

        return  modelFactory.createEvent(eventID, user, clients, false);
    }

    private Event serializeDeserialize(Event event) throws IOException {
        return mapper.readValue(serialize(event), Event.class);
    }

    private String serialize(Event event) throws IOException {
        StringWriter writer = new StringWriter();
        mapper.writeValue(writer, event);
        return writer.toString();
    }

}