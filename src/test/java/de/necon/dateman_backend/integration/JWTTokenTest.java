package de.necon.dateman_backend.integration;

import com.icegreen.greenmail.store.FolderException;
import de.necon.dateman_backend.model.User;
import de.necon.dateman_backend.repository.ClientRepository;
import de.necon.dateman_backend.repository.UserRepository;
import de.necon.dateman_backend.repository.VerificationTokenRepository;
import de.necon.dateman_backend.service.JWTTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class JWTTokenTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    private JWTTokenService tokenService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ClientRepository clientRepository;

    @Autowired
    VerificationTokenRepository tokenRepository;

    @BeforeEach
    public void setup() throws FolderException {

        //super.setup();
        clientRepository.deleteAll();
        tokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void shouldNotAllowAccessToUnauthenticatedUsers() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/test").secure(true))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldGenerateAuthToken() throws Exception {
        User user = new User("test@mail.com", "password", "tester", true);
        userRepository.saveAndFlush(user);
        String token = tokenService.createToken(user);
        assertNotNull(token);

        var tokenHeader = tokenService.createTokenHeader(token);

        mvc.perform(MockMvcRequestBuilders.get("/test")
                .header(tokenHeader.getValue0(), tokenHeader.getValue1()).secure(true))
                .andExpect(status().isOk());
    }
}
