package de.necon.dateman_backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.necon.dateman_backend.util.ResponseWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

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

}