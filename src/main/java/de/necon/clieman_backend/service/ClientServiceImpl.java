package de.necon.clieman_backend.service;

import de.necon.clieman_backend.exception.ServiceError;
import de.necon.clieman_backend.model.Client;
import de.necon.clieman_backend.model.User;
import de.necon.clieman_backend.repository.ClientRepository;
import de.necon.clieman_backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ConstraintViolationException;
import java.util.List;

import static de.necon.clieman_backend.config.ServiceErrorMessages.*;

@Service
@Transactional
public class ClientServiceImpl implements ClientService {

    private static Logger logger = LoggerFactory.getLogger(ClientServiceImpl.class);

    @Autowired
    UserRepository userRepository;

    @Autowired
    ClientRepository clientRepository;

    @Override
    public List<Client> getClientsOfUser(User user) throws ServiceError {

        if (user == null) {
            throw new ServiceError(NO_USER);
        }

        if (!userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new ServiceError(USER_NOT_FOUND);
        }

        return clientRepository.findAllByUser(user);
    }

    @Override
    public Client addClient(Client client) throws ServiceError {
        var id = client.getId().getId();

        if (id == null || id.isBlank()) {
            throw new ServiceError(INVALID_ID);
        }

        checkUser(client.getId().getUser());

        var optional = clientRepository.findById(client.getId());
        if (optional.isPresent()) throw new ServiceError(CLIENT_ALREADY_EXISTS);

        return clientRepository.saveAndFlush(client);
    }

    @Override
    public void updateClient(Client client) throws ServiceError {

        var id = client.getId();

        if (id.getUser() != client.getId().getUser()) {
            throw new ServiceError(CLIENT_CHANGING_USER_NOT_ALLOWED);
        }

        var optional = clientRepository.findById(id);
        if (optional.isEmpty()) throw new ServiceError(CLIENT_NOT_FOUND);

        try {
            clientRepository.saveAndFlush(client);
        } catch(ConstraintViolationException | JpaSystemException e) {
            logger.error(e.toString());
            throw new ServiceError(List.of(CLIENT_CANNOT_BE_UPDATED));
        }
    }

    @Override
    public void removeClient(Client client) throws ServiceError {
        var optional = clientRepository.findById(client.getId());
        if (optional.isEmpty()) throw new ServiceError(CLIENT_NOT_FOUND);

        try {
            clientRepository.delete(client);
            clientRepository.flush();
        } catch (Exception e) {
            logger.error(e.toString());
            throw new ServiceError(CLIENT_CANNOT_BE_DELETED);
        }
    }

    private void checkUser(User user) {
        if (user == null || user.getId() == null) throw new ServiceError(USER_NOT_FOUND);
        var optional = userRepository.findById(user.getId());
        if (optional.isEmpty()) throw new ServiceError(USER_NOT_FOUND);
        if (!user.isEnabled()) throw new ServiceError(USER_IS_DISABLED);
    }
}