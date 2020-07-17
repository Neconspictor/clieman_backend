package de.necon.dateman_backend.repository;

import de.necon.dateman_backend.model.User;
import org.springframework.data.repository.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Retrieves a user by its email.
     *
     * @param email must not be {@literal null}.
     * @return the entity with the given email or {@literal Optional#empty()} if none found.
     * @throws IllegalArgumentException if {@literal email} is {@literal null}.
     */
    Optional<User> findByEmail(String email);

    /**
     * Returns whether a user with the given email exists.
     *
     * @param email must not be {@literal null}.
     * @return {@literal true} if a user with the given email exists, {@literal false} otherwise.
     * @throws IllegalArgumentException if {@literal email} is {@literal null}.
     */
    boolean existsByEmail(String email);

    /**
     * Retrieves a user by its username.
     *
     * @param username must not be {@literal null}.
     * @return the user with the given username or {@literal Optional#empty()} if none found.
     * @throws IllegalArgumentException if {@literal username} is {@literal null}.
     */
    Optional<User> findByUsername(String username);

    /**
     * Returns whether a user with the given username exists.
     *
     * @param username must not be {@literal null}.
     * @return {@literal true} if a user with the given username exists, {@literal false} otherwise.
     * @throws IllegalArgumentException if {@literal username} is {@literal null}.
     */
    boolean existsByUsername(String username);


    /**
     * Returns all users with the given emails.
     * <p>
     * If some or all emails are not found, no users are returned for these emails.
     * <p>
     * Note that the order of elements in the result is not guaranteed.
     *
     * @param emails must not be {@literal null} nor contain any {@literal null} values.
     * @return guaranteed to be not {@literal null}. The size can be equal or less than the number of given
     *         {@literal emails}.
     * @throws IllegalArgumentException in case the given {@link List emails} or one of its items is {@literal null}.
     */
    List<User> findAllByEmailIn(List<String> emails);


    /**
     * Returns all users with the given usernames.
     * <p>
     * If some or all usernames are not found, no users are returned for these usernames.
     * <p>
     * Note that the order of elements in the result is not guaranteed.
     *
     * @param usernames must not be {@literal null} nor contain any {@literal null} values.
     * @return guaranteed to be not {@literal null}. The size can be equal or less than the number of given
     *         {@literal usernames}.
     * @throws IllegalArgumentException in case the given {@link List usernames} or one of its items is {@literal null}.
     */
    List<User> findAllByUsernameIn(List<String> usernames);

    /**
     * Deletes the user with the given email.
     *
     * @param email must not be {@literal null}.
     * @throws IllegalArgumentException in case the given {@literal email} is {@literal null}
     */
    void deleteByEmail(String email);

    /**
     * Deletes the user with the given username.
     *
     * @param username must not be {@literal null}.
     * @throws IllegalArgumentException in case the given {@literal email} is {@literal null}
     */
    void deleteByUsername(String username);
}