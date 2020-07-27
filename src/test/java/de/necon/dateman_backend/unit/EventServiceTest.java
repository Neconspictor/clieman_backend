package de.necon.dateman_backend.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.necon.dateman_backend.exception.ServiceError;
import de.necon.dateman_backend.util.Json;
import de.necon.dateman_backend.util.ModelFactory;
import de.necon.dateman_backend.model.*;
import de.necon.dateman_backend.repository.ClientRepository;
import de.necon.dateman_backend.repository.EventRepository;
import de.necon.dateman_backend.repository.UserRepository;
import de.necon.dateman_backend.service.EventService;
import de.necon.dateman_backend.service.EventServiceImpl;
import de.necon.dateman_backend.util.Asserter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static de.necon.dateman_backend.config.ServiceErrorMessages.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
public class EventServiceTest {

    @Autowired
    ClientRepository clientRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    EventService eventService;

    @Autowired
    ModelFactory modelFactory;

    @TestConfiguration
    public static class Config {

        @Bean
        EventService eventService() {
            return new EventServiceImpl();
        }

        @Bean
        ModelFactory modelFactory(@Autowired UserRepository userRepository,
                                  @Autowired ClientRepository clientRepository,
                                  @Autowired EventRepository eventRepository) {
            return new ModelFactory(userRepository, clientRepository, eventRepository);
        }
    }


    @Test
    public void getEventsOfUser_onlyEventsOfUserAreReturned() {

        ModelFactory mf = new ModelFactory(userRepository, clientRepository, eventRepository);

        var users = mf.createUsers(2, true);
        mf.createEvents(List.of(
                new ModelFactory.SimpleEventCreationDesc(3, users.get(0)),
                new ModelFactory.SimpleEventCreationDesc(2, users.get(1))
        ), true);

        var events = eventService.getEventsOfUser(users.get(0));
        Assertions.assertTrue(events.size() == 3);
        events.forEach(event -> {
            assertEquals(event.getId().getUser(), users.get(0));
        });

        events = eventService.getEventsOfUser(users.get(1));
        Assertions.assertTrue(events.size() == 2);
        events.forEach(event -> {
            assertEquals(event.getId().getUser(), users.get(1));
        });
    }

    @Test
    public void getEventsOfUser_notExistingUserThrows() {

        var user = modelFactory.createUser("test@email.com", true, false);
        var serviceError = (ServiceError) Asserter.assertException(ServiceError.class).isThrownBy(()->{
            eventService.getEventsOfUser(user);
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), USER_NOT_FOUND);
    }

    @Test
    public void getEventsOfUser_NullNotAllowed () {

        var serviceError = (ServiceError) Asserter.assertException(ServiceError.class).isThrownBy(()->{
            eventService.getEventsOfUser(null);
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), NO_USER);
    }

    @Test
    public void addEvent_ValidEvent() {
        var user = modelFactory.createUser("test@email.com", true, true);
        Event event = new Event(null, null, null, new ArrayList<>(),
                null, "id", null, user);

        eventService.addEvent(event);
    }

    @Test
    public void addEvent_NullNotAllowed() {

        var serviceError = (ServiceError)Asserter.assertException(ServiceError.class).isThrownBy(()-> {
            eventService.addEvent(null);
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), NO_EVENT);
    }


    @Test
    public void addEvent_InvalidEvent_NullID() {
        var user = modelFactory.createUser("test@email.com", true, true);
        Event event = modelFactory.createEvent(null, user, new ArrayList<>());

        var serviceError = (ServiceError) Asserter.assertException(ServiceError.class).isThrownBy(()->{
            eventService.addEvent(event);
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), INVALID_ID);
    }

    @Test
    public void addEvent_InvalidEvent_EmptyID() {
        var user = modelFactory.createUser("test@email.com", true, true);
        Event event = modelFactory.createEvent("", user, new ArrayList<>());

        var serviceError = (ServiceError) Asserter.assertException(ServiceError.class).isThrownBy(()->{
            eventService.addEvent(event);
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), INVALID_ID);
    }

    @Test
    public void addEvent_InvalidEvent_BlankID() {
        var user = modelFactory.createUser("test@email.com", true, true);
        Event event = modelFactory.createEvent("  ", user, new ArrayList<>());

        var serviceError = (ServiceError) Asserter.assertException(ServiceError.class).isThrownBy(()->{
            eventService.addEvent(event);
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), INVALID_ID);
    }


    @Test
    public void addEvent_Invalid_ClientsNull() {
        var user = modelFactory.createUser("test@email.com", true, true);
        Event event = modelFactory.createEvent("id", user, null);

        var serviceError = (ServiceError) Asserter.assertException(ServiceError.class).isThrownBy(()->{
            eventService.addEvent(event);
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), EVENT_NOT_VALID);
    }

    @Test
    public void addEvent_Ivalid_DisabledUser() {
        var user = modelFactory.createUser("test@email.com", false, true);
        Event event = modelFactory.createEvent("id", user, new ArrayList<>());

        var serviceError = (ServiceError) Asserter.assertException(ServiceError.class).isThrownBy(()->{
            eventService.addEvent(event);
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), USER_IS_DISABLED);
    }

    @Test
    public void addEvent_NotStoredUserNotAllowed() {
        var user = modelFactory.createUser("test@email.com", true, false);
        Event event = modelFactory.createEvent("id", user, new ArrayList<>());

        var serviceError = (ServiceError) Asserter.assertException(ServiceError.class).isThrownBy(()->{
            eventService.addEvent(event);
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), USER_NOT_FOUND);
    }


    @Test
    public void addEvent_AlreadyExistingPrimaryKeyNotAllowed() {
        var user = modelFactory.createUser("test@email.com", true, true);
        Event event = modelFactory.createEvent("id", user, new ArrayList<>());

        eventService.addEvent(event);

        var serviceError = (ServiceError) Asserter.assertException(ServiceError.class).isThrownBy(()->{
            eventService.addEvent(event);
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), EVENT_ALREADY_EXISTS);
    }

    @Test
    public void addEvent_NotExistingClientsNotAllowed() {
        var user = modelFactory.createUser("test@email.com", true, true);
        Client client = modelFactory.createClient("clientID", user, false);
        Event event = modelFactory.createEvent("id", user, List.of(client));

        var serviceError = (ServiceError) Asserter.assertException(ServiceError.class).isThrownBy(()->{
            eventService.addEvent(event);
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), EVENT_NOT_VALID);
    }

    @Test
    public void addEvent_SameUserDifferentIdsAllowed() {
        var user = modelFactory.createUser("test@email.com", true, true);
        Event event = modelFactory.createEvent("id", user, new ArrayList<>());
        Event event2 = modelFactory.createEvent("id2", user, new ArrayList<>());

        eventService.addEvent(event);
        eventService.addEvent(event2);
    }

    @Test
    public void removeEvent_NullNotAllowed() {

        var serviceError = (ServiceError) Asserter.assertException(ServiceError.class).isThrownBy(()->{
            eventService.removeEvent(null);
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), NO_EVENT);
    }

    @Test
    public void removeEvent_EventNotFound() {
        var user = modelFactory.createUser("test@email.com", true, true);
        Event event = modelFactory.createEvent("id", user, new ArrayList<>());

        var serviceError = (ServiceError) Asserter.assertException(ServiceError.class).isThrownBy(()->{
            eventService.removeEvent(event);
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), EVENT_NOT_FOUND);
    }

    @Test
    public void removeEvent_validRemove() {

        var user = modelFactory.createUser("test@email.com", true, true);
        Event event = modelFactory.createEvent("id", user, new ArrayList<>(), true);

        Assertions.assertTrue(eventRepository.findAll().size() == 1);
        eventService.removeEvent(event);
        Assertions.assertTrue(eventRepository.findAll().size() == 0);
    }

    @Test
    public void updateEvent_EventNullNotAllowed() {
        var serviceError = (ServiceError) Asserter.assertException(ServiceError.class).isThrownBy(()->{
            eventService.updateEvent(null);
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), NO_EVENT);
    }

    @Test
    public void updateEvent_NullIDNotAllowed() {
        var user = modelFactory.createUser("test@email.com", true, true);
        Event event = modelFactory.createEvent(null, user, new ArrayList<>(), false);

        var serviceError = (ServiceError) Asserter.assertException(ServiceError.class).isThrownBy(()->{
            eventService.updateEvent(event);
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), INVALID_ID);
    }

    @Test
    public void updateEvent_invalid_eventNotStored() {
        var user = modelFactory.createUser("test@email.com", true, true);
        Event event = modelFactory.createEvent("eventID", user, new ArrayList<>(), false);

        var serviceError = (ServiceError) Asserter.assertException(ServiceError.class).isThrownBy(()->{
            eventService.updateEvent(event);
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), EVENT_NOT_FOUND);
    }

    @Test
    public void updateEvent_valid() {
        var user = modelFactory.createUser("test@email.com", true, true);
        Event event = modelFactory.createEvent("eventID", user, new ArrayList<>(), true);

        //ensure that the event is found
        var storedEvent = eventRepository.findById(event.getId()).get();
        assertEquals(event, storedEvent);

        //now change the event and assert that the changes will be adopted.
        //Note create a new Event object, since transaction is not yet completed.
        event = event.copyMiddle();
        event.setColor("#303050");
        event.setStart(new Date());
        event.setEnd(new Date());

        storedEvent = eventRepository.findById(event.getId()).get();
        Assertions.assertNotEquals(event, storedEvent);

        eventService.updateEvent(event);
        storedEvent = eventRepository.findById(event.getId()).get();
        assertEquals(event, storedEvent);
    }

    @Test
    public void removeEvent_invalid_null() {
        var serviceError = (ServiceError) Asserter.assertException(ServiceError.class).isThrownBy(()->{
            eventService.removeEvent(null);
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), NO_EVENT);
    }

    @Test
    public void removeEvent_invalidid() {

        var user = modelFactory.createUser("test@email.com", true, true);
        Event event = modelFactory.createEvent(null, user, new ArrayList<>(), false);

        var serviceError = (ServiceError) Asserter.assertException(ServiceError.class).isThrownBy(()->{
            eventService.removeEvent(event);
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), NO_EVENT);
    }

    @Test
    public void removeEvent_invalidid_user() {

        var user = modelFactory.createUser("test@email.com", true, false);
        Event event = modelFactory.createEvent("eventID", user, new ArrayList<>(), false);

        var serviceError = (ServiceError) Asserter.assertException(ServiceError.class).isThrownBy(()->{
            eventService.removeEvent(event);
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), NO_EVENT);
    }

    @Test
    public void removeEvent_valid() {

        var user = modelFactory.createUser("test@email.com", true, true);
        Event event = modelFactory.createEvent("eventID", user, new ArrayList<>(), true);

        assertEquals(1, eventRepository.findAllByUser(user).size());
        eventService.removeEvent(event);
        assertEquals(0, eventRepository.findAllByUser(user).size());
    }
}