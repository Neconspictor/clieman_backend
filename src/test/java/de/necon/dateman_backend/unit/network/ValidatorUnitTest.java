package de.necon.dateman_backend.unit.network;

import de.necon.dateman_backend.network.EmailDto;
import de.necon.dateman_backend.network.Validator;
import de.necon.dateman_backend.util.Asserter;
import org.junit.jupiter.api.Test;

import javax.validation.ValidationException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ValidatorUnitTest {

    @Test
    public void validate_valid_withoutSubClass() {

        EmailDto dto = new EmailDto("test@email.com");
        Validator.validate(dto);
    }

    @Test
    public void validate_valid_withSubClass() {

        TestDtoSubclass dto = new TestDtoSubclass("test@email.com", "testField");
        Validator.validate(dto);
    }

    @Test
    public void validate_invalid_fieldNull() {

        EmailDto dto = new EmailDto();

        var exception = (ValidationException)Asserter.assertException(ValidationException.class).isThrownBy(()->{
            Validator.validate(dto);
        }).source();

        assertEquals("Field value is null: email", exception.getMessage());
    }

    @Test
    public void validate_invalid_inheritedFieldNull() {

        TestDtoSubclass dto = new TestDtoSubclass(null, "testField");

        var exception = (ValidationException)Asserter.assertException(ValidationException.class).isThrownBy(()->{
            Validator.validate(dto);
        }).source();

        assertEquals("Field value is null: email", exception.getMessage());
    }

    @Test
    public void validate_invalid_null() {

        var exception = (ValidationException)Asserter.assertException(ValidationException.class).isThrownBy(()->{
            Validator.validate(null);
        }).source();

        assertEquals("dto is null", exception.getMessage());
    }
}