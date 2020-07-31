package de.necon.dateman_backend.unit;

import de.necon.dateman_backend.exception.ServiceError;
import de.necon.dateman_backend.model.Client;
import de.necon.dateman_backend.model.ID;
import de.necon.dateman_backend.model.Sex;
import de.necon.dateman_backend.model.User;
import de.necon.dateman_backend.repository.ClientRepository;
import de.necon.dateman_backend.repository.EventRepository;
import de.necon.dateman_backend.repository.UserRepository;
import de.necon.dateman_backend.service.ClientService;
import de.necon.dateman_backend.service.ClientServiceImpl;
import de.necon.dateman_backend.util.Asserter;
import de.necon.dateman_backend.util.ModelFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.Date;
import java.util.List;

import static de.necon.dateman_backend.config.ServiceErrorMessages.*;

@DataJpaTest
public class ClientServiceTest {

    @Autowired
    ClientRepository clientRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ClientService clientService;

    @Autowired
    ModelFactory modelFactory;

    @TestConfiguration
    public static class Config {

        @Bean
        ClientService clientService() {
            return new ClientServiceImpl();
        }

        @Bean
        ModelFactory modelFactory(@Autowired UserRepository userRepository,
                                  @Autowired ClientRepository clientRepository,
                                  @Autowired EventRepository eventRepository) {
            return new ModelFactory(userRepository, clientRepository, eventRepository);
        }
    }


    @Test
    public void getClientsOfUser_onlyClientsOfUserAreReturned() {
        var user1 = modelFactory.createUser("test@email.com", true, true);
        var user2 = modelFactory.createUser("test2@email.com", true, true);
        var user3 = modelFactory.createUser("test3@email.com", true, true);

        modelFactory.createClient("client1", user1, true);
        modelFactory.createClient("client2", user1, true);
        modelFactory.createClient("client3", user1, true);

        modelFactory.createClient("client1", user2, true);
        modelFactory.createClient("client2", user2, true);

        var clients = clientService.getClientsOfUser(user1);
        Assertions.assertTrue(clients.size() == 3);
        clients.forEach(client -> {
            Assertions.assertEquals(client.getId().getUser(), user1);
        });

        clients = clientService.getClientsOfUser(user2);
        Assertions.assertTrue(clients.size() == 2);
        clients.forEach(client -> {
            Assertions.assertEquals(client.getId().getUser(), user2);
        });

        clients = clientService.getClientsOfUser(user3);
        Assertions.assertTrue(clients.size() == 0);
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

        var serviceError = (ServiceError) Asserter.assertException(ServiceError.class).isThrownBy(()->{
            clientService.getClientsOfUser(null);
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), NO_USER);
    }


    @Test
    public void addClient_NullNotAllowed() {
        Asserter.assertException(NullPointerException.class).isThrownBy(()-> {
            clientService.addClient(null);
        });
    }

    @Test
    public void addClient_InvalidClientNoUser() {
        var client = modelFactory.createClient("id", null, false);

        var serviceError = (ServiceError) Asserter.assertException(ServiceError.class).isThrownBy(()->{
            clientService.addClient(client);
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), USER_NOT_FOUND);
    }

    @Test
    public void addClient_InvalidClientNoID() {
        var user = modelFactory.createUser("test@email.com", true, true);;
        var client = modelFactory.createClient(null, user, false);

        var serviceError = (ServiceError) Asserter.assertException(ServiceError.class).isThrownBy(()->{
            clientService.addClient(client);
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), INVALID_ID);
    }

    @Test
    public void addClient_InvalidClientEmptyID() {
        var user = modelFactory.createUser("test@email.com", true, true);;
        var client = modelFactory.createClient("", user, false);

        var serviceError = (ServiceError) Asserter.assertException(ServiceError.class).isThrownBy(()->{
            clientService.addClient(client);
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), INVALID_ID);
    }


    @Test
    public void addClient_ValidClient() {
        var user = modelFactory.createUser("test@email.com", true, true);;
        var client = modelFactory.createClient("id", user, false);

        clientService.addClient(client);
    }


    @Test
    public void addClient_DisabledUserNotAllowed() {
        var user = userRepository.save(new User("test@email.com", "password",
                "test", false));
        var client = modelFactory.createClient("id", user, false);

        var serviceError = (ServiceError) Asserter.assertException(ServiceError.class).isThrownBy(()->{
            clientService.addClient(client);
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), USER_IS_DISABLED);
    }

    @Test
    public void addClient_NotStoredUserNotAllowed() {
        var user = new User("test@email.com", "password",
                "test", true);
        var client = modelFactory.createClient("id", user, false);

        var serviceError = (ServiceError) Asserter.assertException(ServiceError.class).isThrownBy(()->{
            clientService.addClient(client);
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), USER_NOT_FOUND);
    }


    @Test
    public void addClient_AlreadyExistingPrimaryKeyNotAllowed() {
        var user = modelFactory.createUser("test@email.com", true, true);;
        String id = "id";

        var client = modelFactory.createClient(id, user, false);
        var client2 = modelFactory.createClient(id, user, false);

        clientService.addClient(client);

        var serviceError = (ServiceError) Asserter.assertException(ServiceError.class).isThrownBy(()->{
            clientService.addClient(client2);
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), CLIENT_ALREADY_EXISTS);
    }

    @Test
    public void addClient_SameUserDifferentIdsAllowed() {
        var user = modelFactory.createUser("test@email.com", true, true);;

        var client = modelFactory.createClient("id1", user, false);
        var client2 = modelFactory.createClient("id2", user, false);

        clientService.addClient(client);
        clientService.addClient(client2);
    }

    @Test
    public void removeClient_NullNotAllowed() {

        Asserter.assertException(NullPointerException.class).isThrownBy(()->{
            clientService.removeClient(null);
        });
    }

    @Test
    public void removeClient_ClientNotFound() {

        var client = modelFactory.createClient("id", null, false);


        var serviceError = (ServiceError) Asserter.assertException(ServiceError.class).isThrownBy(()->{
            clientService.removeClient(client);
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), CLIENT_NOT_FOUND);
    }

    @Test
    public void removeClient_clientWillBeRemoved() {

        var user = modelFactory.createUser("test@email.com", true, true);
        var client = modelFactory.createClient("id", user, true);

        Assertions.assertTrue(clientRepository.findAll().size() == 1);
        clientService.removeClient(client);
        Assertions.assertTrue(clientRepository.findAll().size() == 0);
    }

    @Test
    public void removeClient_invalid_referencingEvent() {

        var user = modelFactory.createUser("test@email.com", true, true);
        var client = modelFactory.createClient("id", user, true);
        var event = modelFactory.createEvent("event", user, List.of(client), true);

        client.setAddress("new address");

        var serviceError = (ServiceError) Asserter.assertException(ServiceError.class).isThrownBy(()->{
            clientService.removeClient(client);
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), CLIENT_CANNOT_BE_DELETED);
    }



    @Test
    public void updateClient_ClientNullNotAllowed() {
        var user = modelFactory.createUser("test@email.com", true, true);

        Asserter.assertException(NullPointerException.class).isThrownBy(()->{
            clientService.updateClient(null);
        });
    }

    @Test
    public void updateClient_ClientIDNullNotAllowed() {
        var user = modelFactory.createUser("test@email.com", true, true);

        var client = new Client(null, null, null, null,
                null, null, Sex.DIVERSE, user);

        Asserter.assertException(ServiceError.class).isThrownBy(()->{
            clientService.updateClient(client);
        });
    }


    @Test
    public void updateClient_clientIsUpdated() {
        var user = modelFactory.createUser("test@email.com", true, true);
        var client = modelFactory.createClient("id", user, true);

        //ensure that the client is found
       var storedClient = clientRepository.findById(client.getId()).get();
       Assertions.assertEquals(client, storedClient);

       //now change the client and assert that the changes will be adopted.
        client = client.copyShallow();
        client.setAddress("new address");
        client.setBirthday(new Date());

        storedClient = clientRepository.findById(client.getId()).get();
        Assertions.assertNotEquals(client, storedClient);

        clientService.updateClient(client);
        storedClient = clientRepository.findById(client.getId()).get();
        Assertions.assertEquals(client, storedClient);
    }

    @Test
    public void updateClient_clientWithUpdatedIDIsNotAllowed() {
        var user = modelFactory.createUser("test@email.com", true, true);
        var client = modelFactory.createClient("id", user, true).copyShallow();

        //now change the client and assert that the changes will be adopted.

        client.setAddress("new address");
        client.setBirthday(new Date());
        client.setId(new ID("new id", user));

        var serviceError = (ServiceError) Asserter.assertException(ServiceError.class).isThrownBy(()->{
            clientService.updateClient(client);
        }).source();
    }

    /**
     * This tests ensures that the user of the client id cannot be altered.
     */
    @Test
    public void updateClient_changingUserIsNotAllowed() {
        var user = modelFactory.createUser("test@email.com", true, true);
        var user2 = modelFactory.createUser("test2@email.com", true, true);
        var client = modelFactory.createClient("id", user, true);
        //now change the client and assert that the changes will be adopted.

        client.setAddress("new address");
        client.setBirthday(new Date());
        client.setId(new ID("another id", user2));

        var serviceError = (ServiceError) Asserter.assertException(ServiceError.class).isThrownBy(()->{
            clientService.updateClient(client);
        }).source();
    }

    @Test
    public void updateClient_referencingEventAllowed() {
        var user = modelFactory.createUser("test@email.com", true, true);
        var client = modelFactory.createClient("id", user, true);
        var event = modelFactory.createEvent("event", user, List.of(client), true);
        //now change the client and assert that the changes will be adopted.

        client.setAddress("new address");
        clientService.updateClient(client);
    }
}