package de.necon.dateman_backend.unit;

import de.necon.dateman_backend.util.ModelFactory;
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

        ModelFactory modelFactory = new ModelFactory(userRepository, clientRepository, eventRepository);

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



}