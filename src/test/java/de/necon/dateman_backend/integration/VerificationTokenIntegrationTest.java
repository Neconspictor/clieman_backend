package de.necon.dateman_backend.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static de.necon.dateman_backend.util.ModelFactory.createToken;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class VerificationTokenIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(VerificationTokenIntegrationTest.class);

    @Autowired
    private TestEntityManager testEntityManager;

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