package de.necon.dateman_backend.unit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class UserUnitTest {

    private static Validator validator;

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

    private User createValidUser() {
        return new User("test@email.com", "password", "username", true);
    }

    private User createSecondValidUser() {
        return new User(ANOTHER_VALID_EMAIL, "password", "username2", true);
    }

    private static final String ANOTHER_VALID_EMAIL = "test2@email.com";

}
