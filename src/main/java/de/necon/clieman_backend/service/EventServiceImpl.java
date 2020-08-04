package de.necon.clieman_backend.service;

import de.necon.clieman_backend.exception.ServiceError;
import de.necon.clieman_backend.model.Client;
import de.necon.clieman_backend.model.Event;
import de.necon.clieman_backend.model.User;
import de.necon.clieman_backend.repository.EventRepository;
import de.necon.clieman_backend.repository.UserRepository;
import de.necon.clieman_backend.util.MessageExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ConstraintViolationException;
import java.util.List;

import static de.necon.clieman_backend.config.ServiceErrorMessages.*;

@Service
@Transactional
public class EventServiceImpl implements EventService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    EventRepository eventRepository;

    @Override
    public List<Event> getEventsOfUser(User user) throws ServiceError {

        baseCheck(user);
        return eventRepository.findAllByUser(user);
    }

    @Override
    public List<Event> getEventsOfClient(Client client) throws ServiceError {
        return eventRepository.findAllByClient(client);
    }

    @Override
    public Event addEvent(Event event) throws ServiceError {

        baseCheck(event);

        var id = event.getId().getId();

        if (id == null || id.isBlank()) {
            throw new ServiceError(INVALID_ID);
        }

        checkUser(event.getId().getUser());

        var optional = eventRepository.findById(event.getId());
        if (optional.isPresent()) throw new ServiceError(EVENT_ALREADY_EXISTS);

        try {
            return eventRepository.saveAndFlush(event);
        } catch (ConstraintViolationException | JpaObjectRetrievalFailureException e) {
            throw new ServiceError(EVENT_NOT_VALID, e);
        }
    }

    @Override
    public void updateEvent(Event event) throws ServiceError {

        baseCheck(event);

        var id = event.getId();

        var optional = eventRepository.findById(id);
        if (optional.isEmpty()) throw new ServiceError(EVENT_NOT_FOUND);

        try {
            removeEvent(event);
            addEvent(event);
        } catch(ConstraintViolationException e) {
            throw new ServiceError(MessageExtractor.extract(e), e);
        }
    }

    @Override
    public void removeEvent(Event event) throws ServiceError {

        baseCheck(event);

        var optional = eventRepository.findById(event.getId());
        if (optional.isEmpty()) throw new ServiceError(EVENT_NOT_FOUND);

        eventRepository.delete(event);
        eventRepository.flush();
    }

    private void checkUser(User user) {
        if (user == null || user.getId() == null) throw new ServiceError(USER_NOT_FOUND);
        var optional = userRepository.findById(user.getId());
        if (optional.isEmpty()) throw new ServiceError(USER_NOT_FOUND);
        if (!user.isEnabled()) throw new ServiceError(USER_IS_DISABLED);
    }

    private void baseCheck(Event event) {
        if (event == null || event.getId() == null) {
            throw new ServiceError(NO_EVENT);
        }

        baseCheck(event.getId().getUser());
    }

    private void baseCheck(User user) {
        if (user == null || user.getId() == null) {
            throw new ServiceError(NO_USER);
        }

        if (userRepository.findByEmail(user.getEmail()).isEmpty()) {
            throw new ServiceError(USER_NOT_FOUND);
        }
    }
}