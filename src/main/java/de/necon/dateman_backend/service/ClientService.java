package de.necon.dateman_backend.service;

import de.necon.dateman_backend.exception.ServiceError;
import de.necon.dateman_backend.model.Client;
import de.necon.dateman_backend.model.User;

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
     *  exists already.
     */
    void addClient(Client client) throws ServiceError;

    /**
     * Updates the data of a client, that was previously added to the database.
     * @param client The new client data.
     * @param id Identifies the stored client to be updated.
     * @throws ServiceError If 'id' does not match a stored client or 'client' is not valid.
     * The user of 'id' and the user of the client must match, too. Otherwise this exception is thrown, too.
     */
    void updateClient(Client client, Client.ID id) throws ServiceError;

    /**
     * Removes a client from the database.
     * @param client The client to remove.
     * @throws ServiceError If the client is not stored in the database.
     */
    void removeClient(Client client) throws ServiceError;
}
