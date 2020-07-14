package de.necon.dateman_backend.repository;

import de.necon.dateman_backend.model.User;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface UserRepository extends Repository<User, String> {

    /**
     * Saves a given user. Use the returned instance for further operations as the save operation might have changed the
     * user instance completely.
     *
     * @param user must not be {@literal null}.
     * @return the saved user; will never be {@literal null}.
     * @throws IllegalArgumentException in case the given {@literal user} is {@literal null}.
     */
    <S extends User> S save(User user);

    /**
     * Saves all given users.
     *
     * @param users must not be {@literal null} nor must it contain {@literal null}.
     * @return the saved users; will never be {@literal null}. The returned {@literal Iterable} will have the same size
     *         as the {@literal Iterable} passed as an argument.
     * @throws IllegalArgumentException in case the given {@link Iterable users} or one of its users is
     *           {@literal null}.
     */
    <S extends User> Iterable<S> saveAll(Iterable<S> users);

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
     * Returns all users.
     *
     * @return all users
     */
    Iterable<User> findAll();

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
     * @throws IllegalArgumentException in case the given {@link Iterable emails} or one of its items is {@literal null}.
     */
    Iterable<User> findAllByEmailIn(Iterable<String> emails);


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
     * @throws IllegalArgumentException in case the given {@link Iterable usernames} or one of its items is {@literal null}.
     */
    Iterable<User> findAllByUsernameIn(Iterable<String> usernames);


    /**
     * Returns the number of users available.
     *
     * @return the number of users.
     */
    long count();

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

    /**
     * Deletes a given user.
     *
     * @param user must not be {@literal null}.
     * @throws IllegalArgumentException in case the given user is {@literal null}.
     */
    void delete(User user);

    /**
     * Deletes the given users.
     *
     * @param users must not be {@literal null}. Must not contain {@literal null} elements.
     * @throws IllegalArgumentException in case the given {@literal users} or one of its users is {@literal null}.
     */
    void deleteAll(Iterable<? extends User> users);

    /**
     * Deletes all users managed by the repository.
     */
    void deleteAll();
}