package de.necon.dateman_backend.repository;

import de.necon.dateman_backend.model.Client;
import de.necon.dateman_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClientRepository extends JpaRepository<Client, Long> {

    /**
     * Returns all clients of a given user.
     * <p>
     * If the given user is not found, no clients are returned.
     * <p>
     * Note that the order of elements in the result is not guaranteed.
     *
     * @param user must not be {@literal null}.
     * @return guaranteed to be not {@literal null}. Can be empty.
     * @throws IllegalArgumentException in case the given {@link User} is {@literal null}.
     */
    List<Client> findAllByUser(User user);

}
