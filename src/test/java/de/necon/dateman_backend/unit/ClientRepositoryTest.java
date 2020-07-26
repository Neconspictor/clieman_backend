package de.necon.dateman_backend.unit;

import de.necon.dateman_backend.model.Client;
import de.necon.dateman_backend.model.User;
import de.necon.dateman_backend.repository.ClientRepository;
import de.necon.dateman_backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
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

    @Test
    public void findAllByUser_allClientsForUserReturned() {

        var user1 = createUser("test@email.com");
        var user2 = createUser("test2@email.com");
        var user3 = createUser("test3@email.com");

        var client11 = createClient("client1", user1);
        var client12 = createClient("client2", user1);
        var client13 = createClient("client3", user1);

        var client21 = createClient("client1", user2);
        var client22 = createClient("client2", user2);

        var result1 = clientRepository.findAllByUser(user1);
        var result2 = clientRepository.findAllByUser(user2);
        var result3 = clientRepository.findAllByUser(user3);

        assertTrue(result1.size() == 3);
        assertTrue(result2.size() == 2);
        assertTrue(result3.size() == 0);

        assertTrue(result1.containsAll(List.of(client11, client12, client13)));
        assertTrue(result2.containsAll(List.of(client21, client22)));
    }

    private Client createClient(String id, User user) {
        return clientRepository.saveAndFlush(new Client(null, null, null, null, id, null, null, user));
    }

    private User createUser(String email) {
        return userRepository.saveAndFlush(new User(email, "password", null, true));
    }
}