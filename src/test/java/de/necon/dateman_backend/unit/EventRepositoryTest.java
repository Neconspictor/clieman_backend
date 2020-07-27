package de.necon.dateman_backend.unit;

import de.necon.dateman_backend.model.Client;
import de.necon.dateman_backend.model.Event;
import de.necon.dateman_backend.model.User;
import de.necon.dateman_backend.repository.ClientRepository;
import de.necon.dateman_backend.repository.EventRepository;
import de.necon.dateman_backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

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

    @Test
    public void findAllByUser_allClientsForUserReturned() {

        List<User> users = createUsers(3);
        List<Event> events = createEvents(List.of(
                new SimpleCreationDesc(5, users.get(0)),
                new SimpleCreationDesc(2, users.get(1))));

        var result0 = eventRepository.findAllByUser(users.get(0));
        var result1 = eventRepository.findAllByUser(users.get(1));
        var result2 = eventRepository.findAllByUser(users.get(2));

        assertTrue(result0.size() == 5);
        assertTrue(result1.size() == 2);
        assertTrue(result2.size() == 0);

        assertTrue(result0.containsAll(events.subList(0,5)));
        assertTrue(result1.containsAll(events.subList(5,7)));
    }


    private List<Event> createEvents(List<SimpleCreationDesc> descs) {

        final int clientCount = 2;

        List<EventCreationDesc> creationDescs = new ArrayList<>();
        descs.forEach(desc-> {
            for (int i = 0; i < desc.eventCount; ++i)
                creationDescs.add(new EventCreationDesc(clientCount, desc.user));
        });

        return createEventsWithCreationDesc(creationDescs);
    }

    private List<Event> createEventsWithCreationDesc(List<EventCreationDesc> descs) {
        List<Event> events = new ArrayList<>();
        for (int i = 0; i < descs.size(); ++i) {
            var desc = descs.get(i);
            List<Client> clients = createClients(desc.clientCount, desc.user);
            events.add(createEvent("event" + i, desc.user, clients));
        }

        return events;
    }

    private Event createEvent(String id, User user, List<Client> clients) {
        var event = new Event(null,
                null,
                null,
                clients,
                null,
                id,
                null,
                user);
        return eventRepository.saveAndFlush(event);
    }

    private List<Client> createClients(int count, User user) {
        List<Client> clients = new ArrayList<>();
        for (int i = 0; i < count; ++i) {
            clients.add(createClient("test" + i + "@email.com", user));
        }
        return clients;
    }

    private Client createClient(String id, User user) {
        return clientRepository.saveAndFlush(new Client(null, null, null, null,
                id, null, null, user));
    }

    private List<User> createUsers(int count) {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < count; ++i) {
            users.add(createUser("test" + i + "@email.com"));
        }
        return users;
    }

    private User createUser(String email) {
        return userRepository.saveAndFlush(new User(email, "password", null, true));
    }

    private static class SimpleCreationDesc {
        public int eventCount;
        public User user;

        public SimpleCreationDesc(int eventCount, User user) {
            this.eventCount = eventCount;
            this.user = user;
        }
    }

    private static class EventCreationDesc {
        public int clientCount;
        public User user;

        public EventCreationDesc(int clientCount, User user) {
            this.clientCount = clientCount;
            this.user = user;
        }
    }
}