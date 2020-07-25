package de.necon.dateman_backend.integration;

import de.necon.dateman_backend.listeners.ResetDatabaseTestExecutionListener;
import de.necon.dateman_backend.model.User;
import de.necon.dateman_backend.repository.UserRepository;
import de.necon.dateman_backend.service.JWTTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@ActiveProfiles("test")
@TestExecutionListeners(mergeMode =
        TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS,
        listeners = {ResetDatabaseTestExecutionListener.class}
)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class ClientControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    JWTTokenService tokenService;


    @Test
    public void clients_notAuthenticated() throws Exception {
        var response = getClients(null);
        assertTrue(response.getStatus() == HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    public void clients_authenticated() throws Exception {
        var enabledUser = new User("test@email.com",
                "password", "test", true);
        userRepository.saveAndFlush(enabledUser);
        var response = getClients(tokenService.createToken(enabledUser));
        assertTrue(response.getStatus() == HttpStatus.OK.value());
    }

    private MockHttpServletResponse getClients(String token) throws Exception {
        var header = JWTTokenService.createTokenHeader(token);
        return mvc.perform(get("/clients/getAll")
                .header(header.getValue0(), header.getValue1())
                .secure(true))
                .andReturn()
                .getResponse();
    }
}