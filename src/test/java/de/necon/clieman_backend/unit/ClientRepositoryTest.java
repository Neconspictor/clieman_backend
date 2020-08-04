package de.necon.clieman_backend.unit;

import de.necon.clieman_backend.repository.ClientRepository;
import de.necon.clieman_backend.repository.EventRepository;
import de.necon.clieman_backend.repository.UserRepository;
import de.necon.clieman_backend.util.ModelFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class ClientRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClientRepository clientRepository;

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

    @Test
    public void findAllByUser_allClientsForUserReturned() {

        var user1 = modelFactory.createUser("test@email.com", true, true);
        var user2 = modelFactory.createUser("test2@email.com", true, true);
        var user3 = modelFactory.createUser("test3@email.com", true, true);

        var client11 = modelFactory.createClient("client1", user1, true);
        var client12 = modelFactory.createClient("client2", user1, true);
        var client13 = modelFactory.createClient("client3", user1, true);

        var client21 = modelFactory.createClient("client1", user2, true);
        var client22 = modelFactory.createClient("client2", user2, true);

        var result1 = clientRepository.findAllByUser(user1);
        var result2 = clientRepository.findAllByUser(user2);
        var result3 = clientRepository.findAllByUser(user3);

        assertTrue(result1.size() == 3);
        assertTrue(result2.size() == 2);
        assertTrue(result3.size() == 0);

        assertTrue(result1.containsAll(List.of(client11, client12, client13)));
        assertTrue(result2.containsAll(List.of(client21, client22)));
    }
}