package de.necon.clieman_backend.unit;

import de.necon.clieman_backend.config.RepositoryConfig;
import de.necon.clieman_backend.model.User;
import de.necon.clieman_backend.repository.ClientRepository;
import de.necon.clieman_backend.repository.EventRepository;
import de.necon.clieman_backend.repository.UserRepository;
import de.necon.clieman_backend.util.ModelFactory;
import org.apache.commons.text.RandomStringGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class UserUnitTest {

    private static Validator validator;

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

    @BeforeAll
    public static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testEmailNotNull() {

        var user = createValidUser();
        user.setEmail(null);
        var violations = validator.validate(user);
        assertTrue(violations.size() == 1);
        var constraint = violations.iterator().next();
        assertTrue(constraint.getPropertyPath().toString().equals("email"));
    }

    @Test
    public void testEmailNotValidMissingAt() {

        var user = createValidUser();
        user.setEmail("test.com");
        var violations = validator.validate(user);
        assertTrue(violations.size() == 1);
        var constraint = violations.iterator().next();
        assertTrue(constraint.getPropertyPath().toString().equals("email"));
    }

    @Test
    public void testEmailNotValidMissingEnding() {
        var user = createValidUser();
        user.setEmail("test@email.");
        var violations = validator.validate(user);
        assertTrue(violations.size() == 1);
        var constraint = violations.iterator().next();
        assertTrue(constraint.getPropertyPath().toString().equals("email"));
    }

    @Test
    public void testEmailNotValidMissingUsername() {
        var user = createValidUser();
        user.setEmail("@email.com");
        var violations = validator.validate(user);
        assertTrue(violations.size() == 1);
        var constraint = violations.iterator().next();
        assertTrue(constraint.getPropertyPath().toString().equals("email"));
    }

    @Test
    public void testEmailNotValidMissingDot() {

        var user = createValidUser();
        user.setEmail("test@emailcom");
        var violations = validator.validate(user);
        assertTrue(violations.size() == 1);
        var constraint = violations.iterator().next();
        assertTrue(constraint.getPropertyPath().toString().equals("email"));
    }


    @Test
    public void testPasswordEmptyNotAllowed() {

        var user = createValidUser();
        user.setPassword("");
        var violations = validator.validate(user);
        assertTrue(violations.size() > 0);
        var constraint = violations.iterator().next();
        assertTrue(constraint.getPropertyPath().toString().equals("password"));
    }

    @Test
    public void testPasswordNotNull() {

        var user = createValidUser();
        user.setPassword(null);
        var violations = validator.validate(user);
        assertTrue(violations.size() > 0);
        var constraint = violations.iterator().next();
        assertTrue(constraint.getPropertyPath().toString().equals("password"));
    }

    @Test
    public void testPasswordTooShort() {

        var user = createValidUser();
        user.setPassword("0");
        var violations = validator.validate(user);
        assertTrue(violations.size() > 0);
        var constraint = violations.iterator().next();
        assertTrue(constraint.getPropertyPath().toString().equals("password"));
    }

    @Test
    public void testPasswordTooLong() {

        var user = createValidUser();
        var generator = new RandomStringGenerator.Builder().build();
        user.setPassword(generator.generate(RepositoryConfig.MAX_STRING_SIZE + 1));
        var violations = validator.validate(user);
        assertTrue(violations.size() > 0);
        var constraint = violations.iterator().next();
        assertTrue(constraint.getPropertyPath().toString().equals("password"));
    }

    private User createValidUser() {
        return modelFactory.createUser("test@email.com", true, false);
    }
}
