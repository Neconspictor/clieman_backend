package de.necon.dateman_backend.integration;

import com.icegreen.greenmail.store.FolderException;
import de.necon.dateman_backend.BaseControllerTest;
import de.necon.dateman_backend.model.User;
import de.necon.dateman_backend.repository.ClientRepository;
import de.necon.dateman_backend.repository.UserRepository;
import de.necon.dateman_backend.repository.VerificationTokenRepository;
import de.necon.dateman_backend.service.JWTTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class ClientControllerTest {

    @Autowired PasswordEncoder encoder;

    @Autowired
    MockMvc mvc;

    @Autowired
    Environment env;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ClientRepository clientRepository;

    @Autowired
    VerificationTokenRepository tokenRepository;

    @Autowired
    JWTTokenService tokenService;


    private User disabledUser;
    private static final String disabledUserPassword = "password";
    private User enabledUser;
    private static final String enabledUserPassword = "password2";
    private static final String wrongPassword = "wrong password";
    private static final String notExistingUser = "not@existing.com";


    @BeforeEach
    //@Override
    public void setup() throws FolderException {

        //super.setup();

        disabledUser = new User("test@email.com",
                encoder.encode(disabledUserPassword), "test", false);
        enabledUser = new User("test2@email.com",
                encoder.encode(enabledUserPassword), null, true);

        clientRepository.deleteAll();
        tokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void clients_notAuthenticated() throws Exception {
        var response = getClients(null);
        assertTrue(response.getStatus() == HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    public void clients_authenticated() throws Exception {
        userRepository.saveAndFlush(enabledUser);
        var response = getClients(tokenService.createToken(enabledUser));
        assertTrue(response.getStatus() == HttpStatus.OK.value());
    }

    private MockHttpServletResponse getClients(String token) throws Exception {
        var header = JWTTokenService.createTokenHeader(token);
        return mvc.perform(get("/clients")
                .header(header.getValue0(), header.getValue1())
                .secure(true))
                .andReturn()
                .getResponse();
    }
}