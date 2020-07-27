package de.necon.dateman_backend.service;

import de.necon.dateman_backend.exception.ServiceError;
import de.necon.dateman_backend.model.Event;
import de.necon.dateman_backend.model.User;
import org.springframework.dao.DataIntegrityViolationException;

import javax.validation.ConstraintViolationException;
import java.util.List;

public interface EventService {

    /**
     * Provides the events created by the given user.
     * @param user The user for who we want retrieve the events.
     * @return The events of the user. The list is never be null.
     * @throws ServiceError If 'user' is null, if 'user' is not stored in the database or an io error occurs.
     */
    List<Event> getEventsOfUser(User user) throws ServiceError;

    /**
     * Adds a not yet added event to the database.
     * @param event The client to be added.
     * @throws ServiceError If event is null or if the event is not valid or another event with the same (id, user)
     * primary key exists already.
     * @throws ConstraintViolationException If the event is not valid
     * @throws DataIntegrityViolationException If the event is not valid
     */
    Event addEvent(Event event) throws ServiceError;

    /**
     * Updates the data of an event, that was previously added to the database.
     * NOTE: the id of the event mustn't be changed, since it is used to find the old event data.
     * @param event The new event data.
     * @throws ServiceError If the event is null or if the event is not valid or not stored in the database.
     */
    void updateEvent(Event event) throws ServiceError;

    /**
     * Removes an event from the database.
     * @param event The event to remove.
     * @throws ServiceError If the event is null or if the event is not stored in the database.
     */
    void removeEvent(Event event) throws ServiceError;
}