package de.necon.clieman_backend.unit;

import de.necon.clieman_backend.model.Client;
import de.necon.clieman_backend.util.Asserter;
import de.necon.clieman_backend.util.ModelFactory;
import de.necon.clieman_backend.model.Event;
import de.necon.clieman_backend.model.User;
import de.necon.clieman_backend.repository.ClientRepository;
import de.necon.clieman_backend.repository.EventRepository;
import de.necon.clieman_backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class EventRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ModelFactory modelFactory;

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
    public void findAllByUser_allEventsForUserReturned() {

        List<User> users = modelFactory.createUsers(3, true);
        List<Event> events = modelFactory.createEvents(List.of(
                new ModelFactory.SimpleEventCreationDesc(5, users.get(0)),
                new ModelFactory.SimpleEventCreationDesc(2, users.get(1))), true);

        var result0 = eventRepository.findAllByUser(users.get(0));
        var result1 = eventRepository.findAllByUser(users.get(1));
        var result2 = eventRepository.findAllByUser(users.get(2));

        assertTrue(result0.size() == 5);
        assertTrue(result1.size() == 2);
        assertTrue(result2.size() == 0);

        assertTrue(result0.containsAll(events.subList(0,5)));
        assertTrue(result1.containsAll(events.subList(5,7)));
    }

    @Test
    public void findAllByUser_invalid_notExistingUser() {
        User user = modelFactory.createUser("test@email.com", true, true);
        User user2 = modelFactory.createUser("test2@email.com", true, false);

        List<Event> events = new ArrayList<>();
        events.add(modelFactory.createEvent("event1", user, List.of(), true));

        Asserter.assertException(InvalidDataAccessApiUsageException.class).isThrownBy(()->{
            eventRepository.findAllByUser(user2);
        });
    }

    @Test
    public void findAllByClient_allEventsForClientReturned() {

        User user = modelFactory.createUser("test@email.com", true, true);
        User user2 = modelFactory.createUser("test2@email.com", true, true);
        Client client = modelFactory.createClient("clientID", user, true);
        Client client2 = modelFactory.createClient("clientID2", user, true);
        Client client3 = modelFactory.createClient("clientID", user2, true);



        List<Event> events = new ArrayList<>();
        events.add(modelFactory.createEvent("event1", user, List.of(client, client2), true));
        events.add(modelFactory.createEvent("event2", user, List.of(), true));
        events.add(modelFactory.createEvent("event3", user, List.of(client), true));


        var result0 = eventRepository.findAllByClient(client);
        var result1 = eventRepository.findAllByClient(client2);
        var result2 = eventRepository.findAllByClient(client3);

        assertEquals(2, result0.size());
        assertEquals(1, result1.size());
        assertEquals(0, result2.size());

        assertTrue(result0.containsAll(List.of(events.get(0), events.get(2))));
        assertTrue(result1.containsAll(List.of(events.get(0))));
    }

    @Test
    public void findAllByClient_valid_notExistingClient() {

        List<Event> events = new ArrayList<>();
        User user = modelFactory.createUser("test@email.com", true, true);
        events.add(modelFactory.createEvent("event1", user, List.of(), true));

        Client client = modelFactory.createClient("clientID", user, false);

        var result0 = eventRepository.findAllByClient(client);
        assertEquals(0, result0.size());
    }



}