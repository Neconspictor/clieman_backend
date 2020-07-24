package de.necon.dateman_backend.service;

import de.necon.dateman_backend.exception.ServiceError;
import de.necon.dateman_backend.model.Client;
import de.necon.dateman_backend.model.User;
import de.necon.dateman_backend.repository.ClientRepository;
import de.necon.dateman_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static de.necon.dateman_backend.config.ServiceErrorMessages.NO_USER;
import static de.necon.dateman_backend.config.ServiceErrorMessages.USER_NOT_FOUND;

@Service
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
}
