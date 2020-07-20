package de.necon.dateman_backend.unit;

import de.necon.dateman_backend.model.VerificationToken;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import static de.necon.dateman_backend.factory.ModelFactory.createToken;
import static de.necon.dateman_backend.factory.ModelFactory.createValidUser;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class VerificationTokenUnitTest {

    private static Validator validator;

    @BeforeAll
    public static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testTokenNotNull() {

        VerificationToken token = new VerificationToken(null, createValidUser());
        var violations = validator.validate(token);
        assertTrue(violations.size() == 1);
        var constraint = violations.iterator().next();
        assertTrue(constraint.getPropertyPath().toString().equals("token"));
    }

    @Test
    public void testUserNotNull() {

        VerificationToken token = new VerificationToken("token", null);
        var violations = validator.validate(token);
        assertTrue(violations.size() == 1);
        var constraint = violations.iterator().next();
        assertTrue(constraint.getPropertyPath().toString().equals("user"));
    }

    @Test
    public void testDateNotNull() {

        VerificationToken token = createToken();
        token.setExpiryDate(null);
        var violations = validator.validate(token);
        assertTrue(violations.size() == 1);
        var constraint = violations.iterator().next();
        assertTrue(constraint.getPropertyPath().toString().equals("expiryDate"));
    }
}