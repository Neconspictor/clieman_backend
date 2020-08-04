package de.necon.clieman_backend.repository;

import de.necon.clieman_backend.model.Client;
import de.necon.clieman_backend.model.Event;
import de.necon.clieman_backend.model.ID;
import de.necon.clieman_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, ID> {

    /**
     * Returns all events of a given user.
     * <p>
     * If the given user is not found, no events are returned.
     * <p>
     * Note that the order of elements in the result is not guaranteed.
     *
     * @param user must not be {@literal null}.
     * @return guaranteed to be not {@literal null}. Can be empty.
     * @throws IllegalArgumentException in case the given {@link User} is {@literal null}.
     */
    @Query("SELECT e FROM Event e WHERE e.id.user = :user")
    List<Event> findAllByUser(@Param("user")User user);

    @Query("SELECT e FROM Event e WHERE :client MEMBER OF e.clients")
    List<Event> findAllByClient(@Param("client") Client client);
}
