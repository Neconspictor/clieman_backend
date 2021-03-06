package de.necon.clieman_backend.service;

import de.necon.clieman_backend.exception.ServiceError;
import de.necon.clieman_backend.model.Client;
import de.necon.clieman_backend.model.User;
import org.springframework.dao.DataIntegrityViolationException;

import javax.validation.ConstraintViolationException;
import java.util.List;

public interface ClientService {

    /**
     * Provides the clients created by the given user.
     * @param user The user for who we want retrieve the clients.
     * @return The clients of the user. The list is never be null.
     * @throws ServiceError If 'user' is null, if 'user' is not stored in the database or an io error occurs.
     */
    List<Client> getClientsOfUser(User user) throws ServiceError;

    /**
     * Adds a not yet added client to the database.
     * @param client The client to be added.
     * @throws ServiceError If the client is not valid or another client with the same (id, user) primary key
     *  exists already. Is also thrown if the id of the client is blank.
     * @throws NullPointerException If client is null
     * @throws ConstraintViolationException If client is not valid
     * @throws DataIntegrityViolationException If client is not valid
     */
    Client addClient(Client client) throws ServiceError;

    /**
     * Updates the data of a client, that was previously added to the database.
     * @param client The new client data.
     * @throws ServiceError If the client does not match a stored client or the client is not valid.
     * The user of the client and the user of the stored client must match, too. Otherwise this exception is thrown, too.
     *
     * @throws NullPointerException If client is null.
     */
    void updateClient(Client client) throws ServiceError;

    /**
     * Removes a client from the database.
     * @param client The client to remove.
     * @throws ServiceError If the client is not stored in the database.
     *
     * @throws NullPointerException If client is null.
     */
    void removeClient(Client client) throws ServiceError;
}
