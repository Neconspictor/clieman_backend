package de.necon.dateman_backend.repository;

import de.necon.dateman_backend.model.User;
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

    @Test
    public void testEmailNotNull() {

        assertThatExceptionOfType(javax.validation.ConstraintViolationException.class).isThrownBy(()->{
            testEntityManager.persistAndFlush(new User(null, "password", "username", true));
        });
    }

    @Test
    public void testEmailNotValidMissingAt() {

        assertThatExceptionOfType(javax.validation.ConstraintViolationException.class).isThrownBy(()->{
            testEntityManager.persistAndFlush(new User("test.com", "password", "username", true));
        });
    }

    @Test
    public void testEmailNotValidMissingEnding() {

        assertThatExceptionOfType(javax.validation.ConstraintViolationException.class).isThrownBy(()->{
            testEntityManager.persistAndFlush(new User("test@email.", "password", "username", true));
        });
    }

    @Test
    public void testEmailNotValidMissingUsername() {

        assertThatExceptionOfType(javax.validation.ConstraintViolationException.class).isThrownBy(()->{
            testEntityManager.persistAndFlush(new User("@email.com", "password", "username", true));
        });
    }

    @Test
    public void testEmailNotValidMissingDot() {

        assertThatExceptionOfType(javax.validation.ConstraintViolationException.class).isThrownBy(()->{
            testEntityManager.persistAndFlush(new User("test@emailcom", "password", "username", true));
        });
    }

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
    public void testPasswordEmptyNotAllowed() {
        var user = createValidUser();
        user.setPassword("");

        assertThatExceptionOfType(javax.validation.ConstraintViolationException.class).isThrownBy(()->{
            testEntityManager.persistAndFlush(user);
        });

        // We have to clear the manager so that flush doesn't rethrow the same exception.
        testEntityManager.clear();

        var user2 = createValidUser();
        user2.setPassword("");

        assertThatExceptionOfType(javax.validation.ConstraintViolationException.class).isThrownBy(()->{
            testEntityManager.persistAndFlush(user2);
        });
    }

    @Test
    public void testPasswordNotNull() {

        assertThatExceptionOfType(javax.validation.ConstraintViolationException.class).isThrownBy(()->{
            var user = createValidUser();
            user.setPassword(null);
            testEntityManager.persistAndFlush(user);
        });
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