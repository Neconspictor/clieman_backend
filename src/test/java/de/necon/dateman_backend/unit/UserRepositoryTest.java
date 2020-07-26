package de.necon.dateman_backend.unit;

import de.necon.dateman_backend.repository.UserRepository;
import de.necon.dateman_backend.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testFindByEmail() {
        final String email = "test@email.com";
        User user = new User(email, "password", "username", true);
        testEntityManager.persist(user);

        var optionalUser = userRepository.findByEmail(email);
        assertTrue(optionalUser.isPresent());

        assertThat(optionalUser.get().getEmail().equals(email));
    }

    @Test
    public void testFindByUsername() {
        final String username = "username";
        User user = new User("test@email.com", "password", username, true);
        testEntityManager.persist(user);

        var optionalUser = userRepository.findByUsername(username);
        assertTrue(optionalUser.isPresent());

        assertThat(optionalUser.get().getUsername().equals(username));
    }
}