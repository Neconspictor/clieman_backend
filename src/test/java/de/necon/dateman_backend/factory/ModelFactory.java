package de.necon.dateman_backend.factory;

import de.necon.dateman_backend.model.Client;
import de.necon.dateman_backend.model.Event;
import de.necon.dateman_backend.model.User;
import de.necon.dateman_backend.model.VerificationToken;
import de.necon.dateman_backend.repository.ClientRepository;
import de.necon.dateman_backend.repository.EventRepository;
import de.necon.dateman_backend.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

public final class ModelFactory {

    private ClientRepository clientRepository;
    private EventRepository eventRepository;
    private UserRepository userRepository;

    public ModelFactory(UserRepository userRepository, ClientRepository clientRepository, EventRepository eventRepository) {
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.eventRepository = eventRepository;
    }

    public static final String ANOTHER_VALID_EMAIL = "test2@email.com";


    public static VerificationToken createToken() {
        return new VerificationToken("token", createValidUser());
    }

    public static User createValidUser() {
        return new User("test@email.com", "password", "username", true);
    }

    public static User createSecondValidUser() {
        return new User(ANOTHER_VALID_EMAIL, "password", "username2", true);
    }




    public List<Event> createEvents(List<SimpleEventCreationDesc> descs) {

        final int clientCount = 2;

        List<EventCreationDesc> creationDescs = new ArrayList<>();
        descs.forEach(desc-> {
            for (int i = 0; i < desc.eventCount; ++i)
                creationDescs.add(new EventCreationDesc(clientCount, desc.user));
        });

        return createEventsWithCreationDesc(creationDescs);
    }

    public List<Event> createEventsWithCreationDesc(List<EventCreationDesc> descs) {
        List<Event> events = new ArrayList<>();
        for (int i = 0; i < descs.size(); ++i) {
            var desc = descs.get(i);
            List<Client> clients = createClients(desc.clientCount, desc.user);
            events.add(createEvent("event" + i, desc.user, clients));
        }

        return events;
    }

    public Event createEvent(String id, User user, List<Client> clients) {
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

    public List<Client> createClients(int count, User user) {
        List<Client> clients = new ArrayList<>();
        for (int i = 0; i < count; ++i) {
            clients.add(createClient("test" + i + "@email.com", user));
        }
        return clients;
    }

    public Client createClient(String id, User user) {
        return clientRepository.saveAndFlush(new Client(null, null, null, null,
                id, null, null, user));
    }

    public List<User> createUsers(int count) {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < count; ++i) {
            users.add(createUser("test" + i + "@email.com"));
        }
        return users;
    }

    public User createUser(String email) {
        return userRepository.saveAndFlush(new User(email, "password", null, true));
    }

    public static class SimpleEventCreationDesc {
        public int eventCount;
        public User user;

        public SimpleEventCreationDesc(int eventCount, User user) {
            this.eventCount = eventCount;
            this.user = user;
        }
    }

    public static class EventCreationDesc {
        public int clientCount;
        public User user;

        public EventCreationDesc(int clientCount, User user) {
            this.clientCount = clientCount;
            this.user = user;
        }
    }
}
