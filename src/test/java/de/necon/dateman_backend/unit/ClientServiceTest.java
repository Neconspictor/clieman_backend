package de.necon.dateman_backend.unit;

import de.necon.dateman_backend.exception.ServiceError;
import de.necon.dateman_backend.model.Client;
import de.necon.dateman_backend.model.ID;
import de.necon.dateman_backend.model.Sex;
import de.necon.dateman_backend.model.User;
import de.necon.dateman_backend.repository.ClientRepository;
import de.necon.dateman_backend.repository.UserRepository;
import de.necon.dateman_backend.service.ClientService;
import de.necon.dateman_backend.service.ClientServiceImpl;
import de.necon.dateman_backend.util.Asserter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.Date;

import static de.necon.dateman_backend.config.ServiceErrorMessages.*;

@DataJpaTest
public class ClientServiceTest {

    @Autowired
    ClientRepository clientRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ClientService clientService;

    @TestConfiguration
    public static class Config {

        @Bean
        ClientService clientService() {
            return new ClientServiceImpl();
        }
    }


    @Test
    public void getClientsOfUser_onlyClientsOfUserAreReturned() {
        var user1 = userRepository.save(new User("test@email.com", "password", "test", true));
        var user2 = userRepository.save(new User("test2@email.com", "password", "test2", true));

        clientRepository.save(new Client("address", new Date(),
                "client1@email.com", "forename", "client1", "client1", Sex.FEMALE, user1));
        clientRepository.save(new Client("address", new Date(),
                "client2@email.com", "forename", "client2", "client2", Sex.FEMALE, user1));
        clientRepository.save(new Client("address", new Date(),
                "client3@email.com", "forename", "client3", "client3", Sex.FEMALE, user1));

        clientRepository.save(new Client("address", new Date(),
                "client5@email.com", "forename", "client4", "client4", Sex.FEMALE, user2));
        clientRepository.save(new Client("address", new Date(),
                "client5@email.com", "forename", "client5", "client5", Sex.FEMALE, user2));

        var clients = clientService.getClientsOfUser(user1);
        Assertions.assertTrue(clients.size() == 3);
        clients.forEach(client -> {
            Assertions.assertEquals(client.getId().getUser(), user1);
        });
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

        User user = null;
        var serviceError = (ServiceError) Asserter.assertException(ServiceError.class).isThrownBy(()->{
            clientService.getClientsOfUser(user);
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), NO_USER);
    }


    @Test
    public void addClient_NullNotAllowed() {
        Client client = null;

        Asserter.assertException(NullPointerException.class).isThrownBy(()-> {
            clientService.addClient(client);
        });
    }

    @Test
    public void addClient_InvalidClientNoUser() {
        Client client = new Client(null, null, null, null,
                "clientID", null, Sex.DIVERSE, null);

        var serviceError = (ServiceError) Asserter.assertException(ServiceError.class).isThrownBy(()->{
            clientService.addClient(client);
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), USER_NOT_FOUND);
    }

    @Test
    public void addClient_InvalidClientNoID() {
        var user = userRepository.save(new User("test@email.com", "password",
                "test", true));
        Client client = new Client(null, null, null, null,
                null, null, Sex.DIVERSE, user);

        var serviceError = (ServiceError) Asserter.assertException(ServiceError.class).isThrownBy(()->{
            clientService.addClient(client);
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), INVALID_ID);
    }

    @Test
    public void addClient_InvalidClientEmptyID() {
        var user = userRepository.save(new User("test@email.com", "password",
                "test", true));
        Client client = new Client(null, null, null, null,
                "", null, Sex.DIVERSE, user);

        var serviceError = (ServiceError) Asserter.assertException(ServiceError.class).isThrownBy(()->{
            clientService.addClient(client);
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), INVALID_ID);
    }


    @Test
    public void addClient_ValidClient() {
        var user = userRepository.save(new User("test@email.com", "password",
                "test", true));
        Client client = new Client(null, null, null, null,
                "id", null, Sex.DIVERSE, user);

        clientService.addClient(client);
    }


    @Test
    public void addClient_DisabledUserNotAllowed() {
        var user = userRepository.save(new User("test@email.com", "password",
                "test", false));
        Client client = new Client(null, null, null, null,
                "id", null, Sex.DIVERSE, user);

        var serviceError = (ServiceError) Asserter.assertException(ServiceError.class).isThrownBy(()->{
            clientService.addClient(client);
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), USER_IS_DISABLED);
    }

    @Test
    public void addClient_NotStoredUserNotAllowed() {
        var user = new User("test@email.com", "password",
                "test", true);
        Client client = new Client(null, null, null, null,
                "id", null, Sex.DIVERSE, user);

        var serviceError = (ServiceError) Asserter.assertException(ServiceError.class).isThrownBy(()->{
            clientService.addClient(client);
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), USER_NOT_FOUND);
    }


    @Test
    public void addClient_AlreadyExistingPrimaryKeyNotAllowed() {
        var user = userRepository.save(new User("test@email.com", "password",
                "test", true));
        String id = "id";

        var client = new Client(null, null, null, null,
                id, null, Sex.DIVERSE, user);
        var client2 = new Client("address", null, null, null,
                id, "client 2", Sex.DIVERSE, user);

        clientService.addClient(client);

        var serviceError = (ServiceError) Asserter.assertException(ServiceError.class).isThrownBy(()->{
            clientService.addClient(client2);
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), CLIENT_ALREADY_EXISTS);
    }

    @Test
    public void addClient_SameUserDifferentIdsAllowed() {
        var user = userRepository.save(new User("test@email.com", "password",
                "test", true));

        var client = new Client(null, null, null, null,
                "id1", null, Sex.DIVERSE, user);
        var client2 = new Client("address", null, null, null,
                "id2", "client 2", Sex.DIVERSE, user);

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

        var client = new Client(null, null, null, null,
                "id1", null, Sex.DIVERSE, null);


        var serviceError = (ServiceError) Asserter.assertException(ServiceError.class).isThrownBy(()->{
            clientService.removeClient(client);
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), CLIENT_NOT_FOUND);
    }

    @Test
    public void removeClient_clientWillBeRemoved() {

        var user = createAndSaveUser("test@email.com", "test");
        var client = createAndAddClient("id", user);

        Assertions.assertTrue(clientRepository.findAll().size() == 1);
        clientService.removeClient(client);
        Assertions.assertTrue(clientRepository.findAll().size() == 0);
    }



    @Test
    public void updateClient_ClientNullNotAllowed() {
        var user = createAndSaveUser("test@email.com", "test");

        Asserter.assertException(NullPointerException.class).isThrownBy(()->{
            clientService.updateClient(null, new ID("id", user));
        });
    }

    @Test
    public void updateClient_ClientIDNullNotAllowed() {
        var user = createAndSaveUser("test@email.com", "test");

        var client = new Client(null, null, null, null,
                "id", null, Sex.DIVERSE, user);

        Asserter.assertException(NullPointerException.class).isThrownBy(()->{
            clientService.updateClient(client, null);
        });
    }


    @Test
    public void updateClient_clientIsUpdated() {
        var user = createAndSaveUser("test@email.com", "test");
        var client = createAndAddClient("id", user);

        //ensure that the client is found
       var storedClient = clientRepository.findById(client.getId()).get();
       Assertions.assertEquals(client, storedClient);

       //now change the client and assert that the changes will be adopted.

        client.setAddress("new address");
        client.setBirthday(new Date());

        storedClient = clientRepository.findById(client.getId()).get();
        Assertions.assertNotEquals(client, storedClient);

        clientService.updateClient(client, client.getId());
        storedClient = clientRepository.findById(client.getId()).get();
        Assertions.assertEquals(client, storedClient);
    }

    @Test
    public void updateClient_clientWithUpdatedIDIsAllowed() {
        var user = createAndSaveUser("test@email.com", "test");
        var client = createAndAddClient("id", user);

        //now change the client and assert that the changes will be adopted.

        client.setAddress("new address");
        client.setBirthday(new Date());
        var id  = client.getId();
        client.setId(new ID("new id", user));

        clientService.updateClient(client, id);
        var storedClient = clientRepository.findById(client.getId()).get();
        Assertions.assertEquals(client, storedClient);
        Assertions.assertTrue(clientRepository.findAll().size() == 1);
    }

    /**
     * This test ensures that updateClient fails if the client id is changed, but another client
     * with the same resulting primary key (id-user combination) already exists.
     */
    @Test
    public void updateClient_updatedIDFailsIfAlreadyExsists() {
        var user = createAndSaveUser("test@email.com", "test");
        var client = createAndAddClient("id", user);
        createAndAddClient("another id", user);

        //now change the client and assert that the changes will be adopted.

        client.setAddress("new address");
        client.setBirthday(new Date());
        var id  = client.getId();
        client.setId(new ID("another id", user));



        var serviceError = (ServiceError) Asserter.assertException(ServiceError.class).isThrownBy(()->{
            clientService.updateClient(client, id);
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), CLIENT_ALREADY_EXISTS);
    }

    /**
     * This tests ensures that the user of the client id cannot be altered.
     */
    @Test
    public void updateClient_changingUserIsNotAllowed() {
        var user = createAndSaveUser("test@email.com", "test");
        var user2 = createAndSaveUser("test2@email.com", "test2");
        var client = createAndAddClient("id", user);
        //now change the client and assert that the changes will be adopted.

        client.setAddress("new address");
        client.setBirthday(new Date());
        var id  = client.getId();
        client.setId(new ID("another id", user2));

        var serviceError = (ServiceError) Asserter.assertException(ServiceError.class).isThrownBy(()->{
            clientService.updateClient(client, id);
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), CLIENT_CHANGING_USER_NOT_ALLOWED);
    }


    private User createAndSaveUser(String email, String username) {
        return userRepository.saveAndFlush(new User(email, "password",
                username, true));
    }

    private Client createAndAddClient(String id, User user) {
        var client = new Client(null, null, null, null,
                id, null, null, user);

        clientService.addClient(client);
        return client;
    }
}