package de.necon.dateman_backend.integration;

import de.necon.dateman_backend.model.User;
import de.necon.dateman_backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class UserIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(UserIntegrationTest.class);

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private UserRepository userRepository;

    /**
     * Tests that it is not possible to add multiple users with the same email address.
     */
    @Test
    public void testEmailUniqueConstraint() {
        final String email = "test@email.com";

        var user = createValidUser();
        var user2 = createSecondValidUser();
        user2.setEmail(user.getEmail());

        testEntityManager.persistAndFlush(user);
        assertThatExceptionOfType(javax.persistence.PersistenceException.class).isThrownBy(()->{
            testEntityManager.persistAndFlush(user2);
        }).withCauseInstanceOf(org.hibernate.exception.ConstraintViolationException.class);
    }

    @Test
    public void testUsernameMultipleNullAllowed() {
        var user = createValidUser();
        user.setUsername(null);
        testEntityManager.persistAndFlush(user);

        var user2 = createSecondValidUser();
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

        testEntityManager.persist(createValidUser());
        assertThatExceptionOfType(javax.persistence.PersistenceException.class).isThrownBy(()->{
            var user = createValidUser();
            user.setEmail(ANOTHER_VALID_EMAIL);
            testEntityManager.persistAndFlush(user);
        }).withStackTraceContaining(errorMessageKeyword);

    }

    private User createValidUser() {
        return new User("test@email.com", "password", "username", true);
    }

    private User createSecondValidUser() {
        return new User(ANOTHER_VALID_EMAIL, "password", "username2", true);
    }

    private static final String ANOTHER_VALID_EMAIL = "test2@email.com";

}