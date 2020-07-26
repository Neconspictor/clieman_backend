package de.necon.dateman_backend.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.necon.dateman_backend.listeners.ResetDatabaseTestExecutionListener;
import de.necon.dateman_backend.model.Client;
import de.necon.dateman_backend.model.User;
import de.necon.dateman_backend.repository.UserRepository;
import de.necon.dateman_backend.service.ClientService;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Autowired
    ObjectMapper mapper;

    @Autowired
    private ClientService clientService;


    @Test
    public void getClients_notAuthenticated() throws Exception {
        var response = getClients(null);
        assertTrue(response.getStatus() == HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    public void getClients_authenticated() throws Exception {
        var enabledUser = new User("test@email.com",
                "password", "test", true);
        userRepository.saveAndFlush(enabledUser);
        var response = getClients(tokenService.createToken(enabledUser));
        assertTrue(response.getStatus() == HttpStatus.OK.value());
    }


    /**
     * Ensures that the getClients endpoint does not send user data.
     * @throws Exception
     */
    @Test
    public void getClients_userDataIsNotSend() throws Exception {
        var enabledUser = new User("test@email.com",
                "password", "test", true);
        userRepository.saveAndFlush(enabledUser);
        createAndAddClient("client1", enabledUser);
        createAndAddClient("client2", enabledUser);

        var response = getClients(tokenService.createToken(enabledUser));
        assertTrue(response.getStatus() == HttpStatus.OK.value());

        var content = response.getContentAsString();

        var clients = mapper.readValue(content, new TypeReference<List<Client>>(){});
        assertTrue(clients.size() == 2);

        for (var client : clients) {
            assertEquals(null, client.getId().getUser());
        }
    }


    private MockHttpServletResponse getClients(String token) throws Exception {
        var header = JWTTokenService.createTokenHeader(token);
        return mvc.perform(get("/clients/getAll")
                .header(header.getValue0(), header.getValue1())
                .secure(true))
                .andReturn()
                .getResponse();
    }

    private Client createAndAddClient(String id, User user) {
        var client = new Client(null, null, null, null,
                id, null, null, user);

        clientService.addClient(client);
        return client;
    }
}