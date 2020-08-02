package de.necon.dateman_backend.logic;

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
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class TestDatabaseInitializer implements CommandLineRunner{
    private static final Logger log = LoggerFactory.getLogger(TestDatabaseInitializer.class);

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private Environment env;

    @Autowired
    private UserRepository repository;

    @Autowired
    private ClientRepository clientRepository;

    @Override
    public void run(String... args) throws Exception {
        Parser parser = new Parser();
        var groups = parser.parse("01/01/1960 00:00:00 Z");
        Date date = null;
        for (DateGroup group:groups) {
            date = group.getDates().get(0);
            break;
        }
        var testUser = repository.save(new User("test@email.com",
                encoder.encode("pass"), "test", true));
        Client client = new Client("test address 1",
                date,
                "client@email.com",
                "forename",
                "client1",
                null,
                "family name",
                Sex.DIVERSE,
                null,
                testUser);


        log.info("Preloading " + repository.save(new User("pumuckl@muenchen.de",
                encoder.encode("r3dG0blin"), "Pumuckl", true)));
        log.info("Preloading " + repository.save(new User("schlomo@testimonial.de",
                encoder.encode("sleepAbitAndRest"), "Schlomo", true)));
        log.info("Preloading " + testUser);
        log.info("Preloading " + clientRepository.save(client));
        log.info("Preloading " + clientRepository.save(new Client(null, null, null, null,
                "client2",null,
                null, null, null, repository.findByUsername("Pumuckl").get())));
        log.info("Preloading " + clientRepository.save(new Client("address of client 3", new Date(),
                "client3@email.com", "forename of client 3", "client2", null,
                "name of client 3", Sex.MALE, null, testUser)));
    }
}
