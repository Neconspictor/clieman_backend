package de.necon.dateman_backend.integration;

import de.necon.dateman_backend.model.Client;
import de.necon.dateman_backend.model.Event;
import de.necon.dateman_backend.model.User;
import de.necon.dateman_backend.repository.ClientRepository;
import de.necon.dateman_backend.repository.EventRepository;
import de.necon.dateman_backend.repository.UserRepository;
import de.necon.dateman_backend.util.ModelFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class UserIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(UserIntegrationTest.class);

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private UserRepository userRepository;

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

    /**
     * Tests that it is not possible to add multiple users with the same email address.
     */
    @Test
    public void testEmailUniqueConstraint() {
        final String email = "test@email.com";

        var user = modelFactory.createUser("test@email.com", true, false);
        var user2 = modelFactory.createUser("test2@email.com", true, false);
        user2.setEmail(user.getEmail());

        testEntityManager.persistAndFlush(user);
        assertThatExceptionOfType(javax.persistence.PersistenceException.class).isThrownBy(()->{
            testEntityManager.persistAndFlush(user2);
        }).withCauseInstanceOf(org.hibernate.exception.ConstraintViolationException.class);
    }

    @Test
    public void testUsernameMultipleNullAllowed() {
        var user = modelFactory.createUser("test@email.com", true, false);
        user.setUsername(null);
        testEntityManager.persistAndFlush(user);

        var user2 = modelFactory.createUser("test2@email.com", true, false);
        user2.setUsername(null);
        testEntityManager.persistAndFlush(user2);
    }

    /**
     * Tests that it is not possible to add multiple users with the same user name.
     */
    @Test
    public void testUsernameUnique() {

        /**
         * This keyword is part of the stacktrace when JPA tries to insert the user object and the constraint
         * on the username fails.
         */
        final String errorMessageKeyword = "ON PUBLIC.TB_USER(USERNAME)";

        testEntityManager.persist(modelFactory.createValidUser());
        assertThatExceptionOfType(javax.persistence.PersistenceException.class).isThrownBy(()->{
            var user = modelFactory.createValidUser();
            user.setEmail(ANOTHER_VALID_EMAIL);
            testEntityManager.persistAndFlush(user);
        }).withStackTraceContaining(errorMessageKeyword);

    }


    /**
     * Checks that data associated to a given user is deleted when the user gets deleted.
     */
    @Test
    public void deleteUser_ClientsAreDeletedTooIfNoEventsReferencingClients() {

        var user = modelFactory.createUser("test@email.com", true, true);
        var client = modelFactory.createClient("clientID", user, true);
        //var event = modelFactory.createEvent("eventID", user, List.of(client), true);

        testEntityManager.flush();


        // Note: we have to delete all events referencing the client before we delete the client!

        testEntityManager.remove(user);
        testEntityManager.flush();
        testEntityManager.clear();

        assertTrue(testEntityManager.find(User.class, user.getId()) == null);
        assertTrue(testEntityManager.find(Client.class, client.getId()) == null);
        //assertTrue(testEntityManager.find(Event.class, event.getId()) == null);
    }

    private User createSecondValidUser() {
        return new User(ANOTHER_VALID_EMAIL, "password", "username2", true);
    }

    private static final String ANOTHER_VALID_EMAIL = "test2@email.com";

}