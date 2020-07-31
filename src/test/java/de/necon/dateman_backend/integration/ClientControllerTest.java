package de.necon.dateman_backend.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.necon.dateman_backend.listeners.ResetDatabaseTestExecutionListener;
import de.necon.dateman_backend.model.Client;
import de.necon.dateman_backend.model.Sex;
import de.necon.dateman_backend.model.User;
import de.necon.dateman_backend.network.ErrorListDto;
import de.necon.dateman_backend.repository.ClientRepository;
import de.necon.dateman_backend.repository.EventRepository;
import de.necon.dateman_backend.repository.UserRepository;
import de.necon.dateman_backend.service.ClientService;
import de.necon.dateman_backend.service.JWTTokenService;
import de.necon.dateman_backend.util.ModelFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.io.StringWriter;
import java.util.Date;
import java.util.List;

import static de.necon.dateman_backend.config.ServiceErrorMessages.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

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
        modelFactory.createClient("client1", enabledUser, true);
        modelFactory.createClient("client2", enabledUser, true);

        var response = getClients(tokenService.createToken(enabledUser));
        assertTrue(response.getStatus() == HttpStatus.OK.value());

        var content = response.getContentAsString();

        var clients = mapper.readValue(content, new TypeReference<List<Client>>(){});
        assertTrue(clients.size() == 2);

        for (var client : clients) {
            assertEquals(null, client.getId().getUser());
        }
    }

    @Test
    public void addClient_valid() throws Exception {
        var user = new User("test@email.com",
                "password", "test", true);
        userRepository.saveAndFlush(user);
        var client = modelFactory.createClient("client1", user, false);

        var response = addClient(client, tokenService.createToken(user));
        assertTrue(response.getStatus() == HttpStatus.OK.value());

        var content = response.getContentAsString();

        var deserialized = mapper.readValue(content, Client.class);
        deserialized.getId().setUser(user);

        assertEquals(client, deserialized);
    }

    @Test
    public void addClient_addingTwiceNotAllowed() throws Exception {
        var user = new User("test@email.com",
                "password", "test", true);
        userRepository.saveAndFlush(user);
        var client = modelFactory.createClient("client1", user, false);

        var response = addClient(client, tokenService.createToken(user));
        assertTrue(response.getStatus() == HttpStatus.OK.value());
        response = addClient(client, tokenService.createToken(user));
        assertTrue(response.getStatus() == HttpStatus.BAD_REQUEST.value());

        var errorList = mapper.readValue(response.getContentAsString(), ErrorListDto.class);
        assertEquals(CLIENT_ALREADY_EXISTS, errorList.getErrors().get(0));
    }

    @Test
    public void removeClient_removedClientIsNotStoredAnymore() throws Exception {
        var user = new User("test@email.com",
                "password", "test", true);
        userRepository.saveAndFlush(user);
        var client = modelFactory.createClient("client1", user, true);

        var response = removeClient(client, tokenService.createToken(user));
        assertTrue(response.getStatus() == HttpStatus.OK.value());

        //check that client was indeed removed
        assertEquals(0, clientService.getClientsOfUser(user).size());
    }

    @Test
    public void removeClient_NotExisting() throws Exception {
        var user = new User("test@email.com",
                "password", "test", true);
        userRepository.saveAndFlush(user);
        var client = modelFactory.createClient("client1", user, false);

        var response = removeClient(client, tokenService.createToken(user));
        assertTrue(response.getStatus() == HttpStatus.BAD_REQUEST.value());

        var errorList = mapper.readValue(response.getContentAsString(), ErrorListDto.class);
        assertEquals(CLIENT_NOT_FOUND, errorList.getErrors().get(0));
    }

    @Test
    public void updateClient_valid() throws Exception {
        var user = new User("test@email.com",
                "password", "test", true);
        userRepository.saveAndFlush(user);
        var client = modelFactory.createClient("client1", user, true);
        var client2 = modelFactory.createClient("client1", user, false);

        //change all fields (except id, user) so that we can later assert that they have all been updated.
        client2.setAddress("address");
        client2.setBirthday(new Date(100));
        client2.setEmail("client@email.com");
        client2.setForename("forename");
        client2.setName("name");
        client2.setMobile("0393423");
        client2.setSex(Sex.DIVERSE);
        client2.setTitle("Dr.");


        var response = updateClient(client2, tokenService.createToken(user));
        assertTrue(response.getStatus() == HttpStatus.OK.value());

        //check that client was indeed removed
        var clients = clientService.getClientsOfUser(user);
        assertEquals(1, clientService.getClientsOfUser(user).size());
        assertEquals(client2, clients.get(0));
    }

    @Test
    public void updateClient_NotExisting() throws Exception {
        var user = new User("test@email.com",
                "password", "test", true);
        userRepository.saveAndFlush(user);
        var client = modelFactory.createClient("client1", user, false);

        var response = updateClient(client, tokenService.createToken(user));
        assertTrue(response.getStatus() == HttpStatus.BAD_REQUEST.value());

        var errorList = mapper.readValue(response.getContentAsString(), ErrorListDto.class);
        assertEquals(CLIENT_NOT_FOUND, errorList.getErrors().get(0));
    }

    @Test
    public void updateClient_invalidId_Null() throws Exception {
        var user = new User("test@email.com",
                "password", "test", true);
        userRepository.saveAndFlush(user);
        var client = modelFactory.createClient(null, user, false);

        var response = updateClient(client, tokenService.createToken(user));
        assertTrue(response.getStatus() == HttpStatus.BAD_REQUEST.value());

        var errorList = mapper.readValue(response.getContentAsString(), ErrorListDto.class);
        assertEquals(MALFORMED_DATA, errorList.getErrors().get(0));
    }

    @Test
    public void updateClient_invalidId_Empty() throws Exception {
        var user = new User("test@email.com",
                "password", "test", true);
        userRepository.saveAndFlush(user);
        var client = modelFactory.createClient("", user, false);

        var response = updateClient(client, tokenService.createToken(user));
        assertTrue(response.getStatus() == HttpStatus.BAD_REQUEST.value());

        var errorList = mapper.readValue(response.getContentAsString(), ErrorListDto.class);
        assertEquals(MALFORMED_DATA, errorList.getErrors().get(0));
    }

    @Test
    public void updateClient_invalidId_Blank() throws Exception {
        var user = new User("test@email.com",
                "password", "test", true);
        userRepository.saveAndFlush(user);
        var client = modelFactory.createClient("   ", user, false);

        var response = updateClient(client, tokenService.createToken(user));
        assertTrue(response.getStatus() == HttpStatus.BAD_REQUEST.value());

        var errorList = mapper.readValue(response.getContentAsString(), ErrorListDto.class);
        assertEquals(MALFORMED_DATA, errorList.getErrors().get(0));
    }


    private MockHttpServletResponse getClients(String token) throws Exception {
        var header = JWTTokenService.createTokenHeader(token);
        return mvc.perform(get("/clients/getAll")
                .header(header.getValue0(), header.getValue1())
                .secure(true))
                .andReturn()
                .getResponse();
    }

    private MockHttpServletResponse addClient(Client client, String token) throws Exception {
        var header = JWTTokenService.createTokenHeader(token);
        var writer = new StringWriter();
        mapper.writeValue(writer, client);
        return mvc.perform(post("/clients/add")
                .header(header.getValue0(), header.getValue1())
                .secure(true)
                .contentType("application/json")
                .content(writer.toString()))
                .andReturn()
                .getResponse();
    }

    private MockHttpServletResponse removeClient(Client client, String token) throws Exception {
        var header = JWTTokenService.createTokenHeader(token);
        var writer = new StringWriter();
        mapper.writeValue(writer, client);
        return mvc.perform(post("/clients/remove")
                .header(header.getValue0(), header.getValue1())
                .secure(true)
                .contentType("application/json")
                .content(writer.toString()))
                .andReturn()
                .getResponse();
    }

    private MockHttpServletResponse updateClient(Client client, String token) throws Exception {
        var header = JWTTokenService.createTokenHeader(token);
        var writer = new StringWriter();
        mapper.writeValue(writer, client);
        return mvc.perform(post("/clients/update")
                .header(header.getValue0(), header.getValue1())
                .secure(true)
                .contentType("application/json")
                .content(writer.toString()))
                .andReturn()
                .getResponse();
    }
}