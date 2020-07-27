package de.necon.dateman_backend.unit;

import de.necon.dateman_backend.factory.ModelFactory;
import de.necon.dateman_backend.model.User;
import de.necon.dateman_backend.repository.ClientRepository;
import de.necon.dateman_backend.repository.EventRepository;
import de.necon.dateman_backend.repository.UserRepository;
import de.necon.dateman_backend.service.EventService;
import de.necon.dateman_backend.service.EventServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.List;

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

    @TestConfiguration
    public static class Config {

        @Bean
        EventService eventService() {
            return new EventServiceImpl();
        }
    }


    @Test
    public void getEventsOfUser_onlyEventsOfUserAreReturned() {

        ModelFactory mf = new ModelFactory(userRepository, clientRepository, eventRepository);

        var users = mf.createUsers(2);
        mf.createEvents(List.of(
                new ModelFactory.SimpleEventCreationDesc(3, users.get(0)),
                new ModelFactory.SimpleEventCreationDesc(2, users.get(1))
        ));

        var events = eventService.getEventsOfUser(users.get(0));
        Assertions.assertTrue(events.size() == 3);
        events.forEach(event -> {
            Assertions.assertEquals(event.getId().getUser(), users.get(0));
        });

        events = eventService.getEventsOfUser(users.get(1));
        Assertions.assertTrue(events.size() == 2);
        events.forEach(event -> {
            Assertions.assertEquals(event.getId().getUser(), users.get(1));
        });
    }
}