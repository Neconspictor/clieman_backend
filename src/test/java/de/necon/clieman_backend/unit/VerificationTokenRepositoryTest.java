package de.necon.clieman_backend.unit;

import de.necon.clieman_backend.repository.VerificationTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static de.necon.clieman_backend.util.ModelFactory.createToken;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class VerificationTokenRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private VerificationTokenRepository repository;

    @Test
    public void testFindByToken() {

        var token = createToken();

        testEntityManager.persist(token.getUser());
        testEntityManager.persist(token);

        var optionalToken = repository.findByToken(token.getToken());
        assertTrue(optionalToken.isPresent());
        assertThat(optionalToken.get().getToken().equals(token.getToken()));
    }

    @Test
    public void testFindByUser() {

        var token = createToken();
        var user = token.getUser();

        testEntityManager.persist(token.getUser());
        testEntityManager.persist(token);

        var optionalToken = repository.findByUser(user);
        assertTrue(optionalToken.isPresent());
        assertThat(optionalToken.get().getToken().equals(token.getToken()));
    }

    @Test
    public void testUserUnique() {
        var token = createToken();
        var token2 = createToken();
        token2.setToken("token2");
        token2.setUser(token.getUser());

        testEntityManager.persistAndFlush(token.getUser());
        testEntityManager.persistAndFlush(token);
        assertThatExceptionOfType(javax.persistence.PersistenceException.class).isThrownBy(()->{
            testEntityManager.persistAndFlush(token2);
        }).withCauseInstanceOf(org.hibernate.exception.ConstraintViolationException.class);
    }
}