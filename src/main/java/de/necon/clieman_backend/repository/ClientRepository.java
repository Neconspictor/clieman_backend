package de.necon.clieman_backend.repository;

import de.necon.clieman_backend.model.Client;
import de.necon.clieman_backend.model.ID;
import de.necon.clieman_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClientRepository extends JpaRepository<Client, ID> {

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
    @Query("SELECT c FROM Client c WHERE c.id.user = :user")
    List<Client> findAllByUser(@Param("user")User user);
}
