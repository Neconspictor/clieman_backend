package de.necon.clieman_backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.necon.clieman_backend.network.ExceptionToMessageMapper;
import de.necon.clieman_backend.util.ResponseWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.BadCredentialsException;

@Configuration
public class GeneralConfig {

    private final ObjectMapper objectMapper;

    public GeneralConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    ResponseWriter responseWriter() {
        return new ResponseWriter(objectMapper);
    }

    @Bean
    ExceptionToMessageMapper exceptionToMessageMapper() {

        var mapper = new ExceptionToMessageMapper()
                .register(DisabledException.class, ServiceErrorMessages.USER_IS_DISABLED)
                .register(BadCredentialsException.class, ServiceErrorMessages.BAD_CREDENTIALS);

        return mapper;
    }
}