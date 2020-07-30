package de.necon.dateman_backend.integration;

import de.necon.dateman_backend.repository.ClientRepository;
import de.necon.dateman_backend.repository.EventRepository;
import de.necon.dateman_backend.repository.UserRepository;
import de.necon.dateman_backend.util.Asserter;
import de.necon.dateman_backend.util.ModelFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.persistence.PersistenceException;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class EventIntegrationTest {

    @Autowired
    private TestEntityManager testEntityManager;

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
    public void storingEvemt_UserNullNotAllowed() {

        var event = modelFactory.createEvent("id", null, new ArrayList<>(), false);

        var ex = (PersistenceException) Asserter.assertException(PersistenceException.class).isThrownBy(()->{
            testEntityManager.persistAndFlush(event);
        }).source();

        var constraintEx = ex.getCause().getCause();
        var msg = constraintEx.getMessage();

        assertTrue(msg.startsWith("NULL"));
    }
}