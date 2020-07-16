package de.necon.dateman_backend.config;

import de.necon.dateman_backend.model.User;
import de.necon.dateman_backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class LoadDatabase {
    private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);

    @Autowired
    private PasswordEncoder encoder;

    @Bean
    CommandLineRunner initDatabase(UserRepository repository) {
        return args -> {
            log.info("Preloading " + repository.save(new User("pumuckl@muenchen.de", encoder.encode("r3dG0blin"), "Pumuckl")));
            log.info("Preloading " + repository.save(new User("schlomo@testimonial.de", encoder.encode("sleepAbitAndRest"), "Schlomo")));
            log.info("Preloading " + repository.save(new User("test@email.com", encoder.encode("pass"), "test")));
        };
    }
}
