package de.necon.dateman_backend.unit;

import de.necon.dateman_backend.exception.ServiceError;
import de.necon.dateman_backend.model.Client;
import de.necon.dateman_backend.model.Sex;
import de.necon.dateman_backend.model.User;
import de.necon.dateman_backend.repository.ClientRepository;
import de.necon.dateman_backend.repository.UserRepository;
import de.necon.dateman_backend.service.ClientService;
import de.necon.dateman_backend.service.ClientServiceImpl;
import de.necon.dateman_backend.util.Asserter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.Date;

import static de.necon.dateman_backend.config.ServiceErrorMessages.NO_USER;
import static de.necon.dateman_backend.config.ServiceErrorMessages.USER_NOT_FOUND;

@DataJpaTest
public class ClientServiceTest {

    @Autowired
    ClientRepository clientRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ClientService clientService;

    @TestConfiguration
    public static class Config {

        @Bean
        ClientService clientService() {
            return new ClientServiceImpl();
        }
    }


    @Test
    public void getClientsOfUser_onlyClientsOfUserAreReturned() {
        var user1 = userRepository.save(new User("test@email.com", "password", "test", true));
        var user2 = userRepository.save(new User("test2@email.com", "password", "test2", true));

        clientRepository.save(new Client("address", new Date(),
                "client1@email.com", "forename", "client1", Sex.FEMALE, user1));
        clientRepository.save(new Client("address", new Date(),
                "client2@email.com", "forename", "client2", Sex.FEMALE, user1));
        clientRepository.save(new Client("address", new Date(),
                "client3@email.com", "forename", "client3", Sex.FEMALE, user1));

        clientRepository.save(new Client("address", new Date(),
                "client5@email.com", "forename", "client4", Sex.FEMALE, user2));
        clientRepository.save(new Client("address", new Date(),
                "client5@email.com", "forename", "client5", Sex.FEMALE, user2));

        var clients = clientService.getClientsOfUser(user1);
        Assertions.assertTrue(clients.size() == 3);
        clients.forEach(client -> {
            Assertions.assertEquals(client.getUser(), user1);
        });
    }


    @Test
    public void getClientsOfUser_notExistingUserThrows() {

        var notExisting = new User("test3@email.com", "password", "test3", true);
        var serviceError = (ServiceError) Asserter.assertException(ServiceError.class).isThrownBy(()->{
            clientService.getClientsOfUser(notExisting);
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), USER_NOT_FOUND);
    }

    @Test
    public void getClientsOfUser_NullNotAllowed () {

        User user = null;
        var serviceError = (ServiceError) Asserter.assertException(ServiceError.class).isThrownBy(()->{
            clientService.getClientsOfUser(user);
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), NO_USER);
    }
}
