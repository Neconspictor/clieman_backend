package de.necon.clieman_backend.logic;

import de.necon.clieman_backend.events.DtoEvent;
import de.necon.clieman_backend.exception.ServiceError;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Validation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;

import static de.necon.clieman_backend.config.ServiceErrorMessages.MALFORMED_DATA;

@Component
@Transactional
public class DtoValidaterListener implements
        ApplicationListener<DtoEvent> {

    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Override
    public void onApplicationEvent(DtoEvent event) {

        var dto = event.getDto();

        if (dto == null) throw new ServiceError(MALFORMED_DATA);
        try {
            validator.validate(dto);
        } catch (ValidationException e) {
            throw new ServiceError(MALFORMED_DATA);
        }
    }
}