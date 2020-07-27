package de.necon.dateman_backend.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.necon.dateman_backend.util.ModelFactory;
import de.necon.dateman_backend.listeners.ResetDatabaseTestExecutionListener;
import de.necon.dateman_backend.model.Event;
import de.necon.dateman_backend.model.User;
import de.necon.dateman_backend.network.ErrorListDto;
import de.necon.dateman_backend.repository.ClientRepository;
import de.necon.dateman_backend.repository.EventRepository;
import de.necon.dateman_backend.repository.UserRepository;
import de.necon.dateman_backend.service.EventService;
import de.necon.dateman_backend.service.JWTTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.io.StringWriter;
import java.util.Date;
import java.util.List;

import static de.necon.dateman_backend.config.ServiceErrorMessages.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ActiveProfiles("test")
@TestExecutionListeners(mergeMode =
        TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS,
        listeners = {ResetDatabaseTestExecutionListener.class}
)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class EventControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    JWTTokenService tokenService;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    private EventService eventService;

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


    @Test
    public void getEvents_notAuthenticated() throws Exception {
        var response = getEvents(null);
        assertTrue(response.getStatus() == HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    public void getEvents_authenticated() throws Exception {
        var enabledUser = new User("test@email.com",
                "password", "test", true);
        userRepository.saveAndFlush(enabledUser);
        var response = getEvents(tokenService.createToken(enabledUser));
        assertTrue(response.getStatus() == HttpStatus.OK.value());
    }


    /**
     * Ensures that the getEvents endpoint does not send user data.
     * @throws Exception
     */
    @Test
    public void getEvents_userDataIsNotSend() throws Exception {
        var user = modelFactory.createUser("test@email.com", true, true);
        modelFactory.createEvents(List.of(
                new ModelFactory.SimpleEventCreationDesc(2, user)
        ), true);

        var response = getEvents(tokenService.createToken(user));
        assertTrue(response.getStatus() == HttpStatus.OK.value());

        var content = response.getContentAsString();

        var events = mapper.readValue(content, new TypeReference<List<Event>>(){});
        assertTrue(events.size() == 2);

        for (var event : events) {
            assertEquals(null, event.getId().getUser());

            event.getClients().forEach(c -> {
                assertEquals(null, c.getId().getUser());
            });
        }
    }

    @Test
    public void addEvent_valid() throws Exception {
        var user = modelFactory.createUser("test@email.com", true, true);
        var clients = modelFactory.createClients(3, user, true);
        var event = modelFactory.createEvent("eventID", user, clients, false);

        var response = addEvent(event, tokenService.createToken(user));
        assertTrue(response.getStatus() == HttpStatus.OK.value());

        Event deserialized = deserialize(response.getContentAsString());
        deserialized.setUser(user);

        assertEquals(event, deserialized);
    }

    @Test
    public void addEvent_addingTwiceNotAllowed() throws Exception {
        var user = modelFactory.createUser("test@email.com", true, true);
        var clients = modelFactory.createClients(3, user, true);
        var event = modelFactory.createEvent("eventID", user, clients, true);

        var response = addEvent(event, tokenService.createToken(user));
        assertTrue(response.getStatus() == HttpStatus.BAD_REQUEST.value());

        var errorList = mapper.readValue(response.getContentAsString(), ErrorListDto.class);
        assertEquals(EVENT_ALREADY_EXISTS, errorList.getErrors().get(0));
    }

    @Test
    public void addEvent_invalidNotAllowed() throws Exception {
        var user = modelFactory.createUser("test@email.com", true, true);
        var clients = modelFactory.createClients(3, user, false);
        var event = modelFactory.createEvent("eventID", user, clients, false);

        var response = addEvent(event, tokenService.createToken(user));
        assertTrue(response.getStatus() == HttpStatus.BAD_REQUEST.value());

        var errorList = mapper.readValue(response.getContentAsString(), ErrorListDto.class);
        assertEquals(EVENT_NOT_VALID, errorList.getErrors().get(0));
    }


    @Test
    public void removeEvent_valid() throws Exception {
        var user = modelFactory.createUser("test@email.com", true, true);
        var clients = modelFactory.createClients(3, user, true);
        var event = modelFactory.createEvent("eventID", user, clients, true);

        var response = removeEvent(event, tokenService.createToken(user));
        assertTrue(response.getStatus() == HttpStatus.OK.value());

        //check that client was indeed removed
        assertEquals(0, eventService.getEventsOfUser(user).size());
    }

    @Test
    public void removeEvent_invalid_notExisting() throws Exception {
        var user = modelFactory.createUser("test@email.com", true, true);
        var clients = modelFactory.createClients(3, user, true);
        var event = modelFactory.createEvent("eventID", user, clients, false);

        var response = removeEvent(event, tokenService.createToken(user));
        assertTrue(response.getStatus() == HttpStatus.BAD_REQUEST.value());

        var errorList = mapper.readValue(response.getContentAsString(), ErrorListDto.class);
        assertEquals(EVENT_NOT_FOUND, errorList.getErrors().get(0));
    }


    @Test
    public void updateEvent_valid() throws Exception {
        var user = modelFactory.createUser("test@email.com", true, true);
        var clients = modelFactory.createClients(3, user, true);
        var event = modelFactory.createEvent("eventID", user, clients, true);
        var event2 = event.copyMiddle();

        event2.setStart(new Date());
        event2.setColor("#0030AA");
        event2.setName("Updated Event");

        var response = updateEvent(event2, tokenService.createToken(user));
        assertTrue(response.getStatus() == HttpStatus.OK.value());

        //check that exactly one event exists
        var events = eventService.getEventsOfUser(user);
        assertEquals(1, events.size());

        //check that the event matches the event2
        assertEquals(event2, events.get(0));
    }

    @Test
    public void updateEvent_invalid_notExisting() throws Exception {
        var user = modelFactory.createUser("test@email.com", true, true);
        var clients = modelFactory.createClients(3, user, true);
        var event = modelFactory.createEvent("eventID", user, clients, false);

        var response = updateEvent(event, tokenService.createToken(user));
        assertTrue(response.getStatus() == HttpStatus.BAD_REQUEST.value());

        var errorList = mapper.readValue(response.getContentAsString(), ErrorListDto.class);
        assertEquals(EVENT_NOT_FOUND, errorList.getErrors().get(0));
    }

    @Test
    public void updateEvent_invalid_idNull() throws Exception {
        var user = modelFactory.createUser("test@email.com", true, true);
        var clients = modelFactory.createClients(3, user, true);
        var event = modelFactory.createEvent(null, user, clients, false);

        var response = updateEvent(event, tokenService.createToken(user));
        assertTrue(response.getStatus() == HttpStatus.BAD_REQUEST.value());

        var errorList = mapper.readValue(response.getContentAsString(), ErrorListDto.class);
        assertEquals(EVENT_NOT_FOUND, errorList.getErrors().get(0));
    }

    @Test
    public void updateEvent_invalid_idBlank() throws Exception {
        var user = modelFactory.createUser("test@email.com", true, true);
        var clients = modelFactory.createClients(3, user, true);
        var event = modelFactory.createEvent("   ", user, clients, false);

        var response = updateEvent(event, tokenService.createToken(user));
        assertTrue(response.getStatus() == HttpStatus.BAD_REQUEST.value());

        var errorList = mapper.readValue(response.getContentAsString(), ErrorListDto.class);
        assertEquals(EVENT_NOT_FOUND, errorList.getErrors().get(0));
    }


    private Event deserialize(String serialized) throws JsonProcessingException {
        return mapper.readValue(serialized, Event.class);
    }

    private MockHttpServletResponse getEvents(String token) throws Exception {
        var header = JWTTokenService.createTokenHeader(token);
        return mvc.perform(get("/events/getAll")
                .header(header.getValue0(), header.getValue1())
                .secure(true))
                .andReturn()
                .getResponse();
    }

    private MockHttpServletResponse addEvent(Event event, String token) throws Exception {
        var header = JWTTokenService.createTokenHeader(token);
        var writer = new StringWriter();
        mapper.writeValue(writer, event);
        return mvc.perform(post("/events/add")
                .header(header.getValue0(), header.getValue1())
                .secure(true)
                .contentType("application/json")
                .content(writer.toString()))
                .andReturn()
                .getResponse();
    }

    private MockHttpServletResponse removeEvent(Event event, String token) throws Exception {
        var header = JWTTokenService.createTokenHeader(token);
        var writer = new StringWriter();
        mapper.writeValue(writer, event);
        return mvc.perform(post("/events/remove")
                .header(header.getValue0(), header.getValue1())
                .secure(true)
                .contentType("application/json")
                .content(writer.toString()))
                .andReturn()
                .getResponse();
    }

    private MockHttpServletResponse updateEvent(Event event, String token) throws Exception {
        var header = JWTTokenService.createTokenHeader(token);
        var writer = new StringWriter();
        mapper.writeValue(writer, event);
        return mvc.perform(post("/events/update")
                .header(header.getValue0(), header.getValue1())
                .secure(true)
                .contentType("application/json")
                .content(writer.toString()))
                .andReturn()
                .getResponse();
    }
}