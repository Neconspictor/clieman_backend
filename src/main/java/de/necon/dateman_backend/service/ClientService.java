package de.necon.dateman_backend.service;

import de.necon.dateman_backend.exception.ServiceError;
import de.necon.dateman_backend.model.Client;
import de.necon.dateman_backend.model.User;

import java.util.List;

public interface ClientService {

    /**
     * Provides the clients created by the given user.
     * @param user The user for who we want retrive the clients.
     * @return The clients of the user. The list is never be null.
     * @throws ServiceError If 'user' is null, if 'user' is not stored in the database or an io error occurs.
     */
    List<Client> getClientsOfUser(User user) throws ServiceError;
}
