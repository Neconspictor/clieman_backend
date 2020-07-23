package de.necon.dateman_backend.config;

import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;
import de.necon.dateman_backend.model.Client;
import de.necon.dateman_backend.model.Sex;
import de.necon.dateman_backend.model.User;
import de.necon.dateman_backend.repository.ClientRepository;
import de.necon.dateman_backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

@Configuration
public class LoadDatabase {
    private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private Environment env;

    @Bean
    CommandLineRunner initDatabase(UserRepository repository, ClientRepository clientRepository) {
        return args -> {

            Parser parser = new Parser();
            var groups = parser.parse("01/01/1960 00:00:00 Z");
            Date date = null;
            for (DateGroup group:groups) {
                date = group.getDates().get(0);
                break;
            }
            var testUser = repository.save(new User("test@email.com", encoder.encode("pass"), "test", true));
            Client client = new Client("test address 1",
                    date,
                    "client@email.com",
                    "forename",
                    "family name",
                    Sex.DIVERSE,
                    testUser);


            log.info("Preloading " + repository.save(new User("pumuckl@muenchen.de", encoder.encode("r3dG0blin"), "Pumuckl", true)));
            log.info("Preloading " + repository.save(new User("schlomo@testimonial.de", encoder.encode("sleepAbitAndRest"), "Schlomo", true)));
            log.info("Preloading " + testUser);
            log.info("Preloading " + clientRepository.save(client));
            log.info("Preloading " + clientRepository.save(new Client(null, null, null, null, null, null, testUser)));

        };
    }
}
