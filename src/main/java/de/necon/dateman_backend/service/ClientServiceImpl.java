package de.necon.dateman_backend.service;

import de.necon.dateman_backend.exception.ServiceError;
import de.necon.dateman_backend.model.Client;
import de.necon.dateman_backend.model.User;
import de.necon.dateman_backend.repository.ClientRepository;
import de.necon.dateman_backend.repository.UserRepository;
import de.necon.dateman_backend.util.MessageExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ConstraintViolationException;
import java.util.List;

import static de.necon.dateman_backend.config.ServiceErrorMessages.*;

@Service
@Transactional
public class ClientServiceImpl implements ClientService {

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
    public void addClient(Client client) throws ServiceError {
        var id = client.getId().getId();

        if (id == null || id.isBlank()) {
            throw new ServiceError(CLIENT_INVLAID_ID);
        }

        checkUser(client.getUser());

        var optional = clientRepository.findById(client.getId());
        if (optional.isPresent()) throw new ServiceError(CLIENT_ALREADY_EXISTS);


        clientRepository.saveAndFlush(client);
    }

    @Override
    public void updateClient(Client client, Client.ID id) throws ServiceError {

        if (id.getUser() != client.getId().getUser()) {
            throw new ServiceError(CLIENT_CHANGING_USER_NOT_ALLOWED);
        }

        var optional = clientRepository.findById(id);
        if (optional.isEmpty()) throw new ServiceError(CLIENT_NOT_FOUND);

        try {
            clientRepository.deleteById(id);
            addClient(client);
        } catch(ConstraintViolationException e) {
            throw new ServiceError(MessageExtractor.extract(e));
        }
    }

    @Override
    public void removeClient(Client client) throws ServiceError {
        var optional = clientRepository.findById(client.getId());
        if (optional.isEmpty()) throw new ServiceError(CLIENT_NOT_FOUND);

        clientRepository.delete(client);
        clientRepository.flush();
    }

    private void checkUser(User user) {
        if (user == null || user.getId() == null) throw new ServiceError(USER_NOT_FOUND);
        var optional = userRepository.findById(user.getId());
        if (optional.isEmpty()) throw new ServiceError(USER_NOT_FOUND);
        if (!user.isEnabled()) throw new ServiceError(USER_IS_DISABLED);
    }
}