package de.necon.dateman_backend.service;

import de.necon.dateman_backend.exception.*;
import de.necon.dateman_backend.model.Client;
import de.necon.dateman_backend.network.RegisterUserDto;
import de.necon.dateman_backend.model.User;
import de.necon.dateman_backend.model.VerificationToken;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for interacting with users.
 */
public interface UserService {

    /**
     * Deletes a user.
     * @param user The user to be deleted.
     * @throws ServiceError If the user is not stored in the database or the user cannot be deleted since he is referenced
     * by other entities (e.g. verification tokens).
     */
    void deleteUser(User user) throws ServiceError;

    /**
     * Registers a new user account.
     * @param userDto Used for creating and registering the user.
     * @return The created user.
     * @throws ServiceError : thrown if no valid User object can be created and stored into the database or
     *  another user with the same email or username already exists. If the password length is too short
     *  (compare User.MIN_PASSWORD_LENGTH), this exception is thrown, too.
     */
    User registerNewUserAccount(RegisterUserDto userDto)
            throws ServiceError;

    /**
     * Verifies a user account using a verification token.
     * @param verificationToken The token for verifying the user.
     * @throws ServiceError If the token is not stored in the database or if the token is expired.
     */
    void verifyUserAccount(String verificationToken) throws ServiceError;

    /**
     * Gets a user by its linked verification token.
     * @param verificationToken The token
     * @return The found user.
     * @throws ServiceError If the verification token does not exist.
     */
    User getUserOfToken(String verificationToken) throws ServiceError;

    /**
     * Provides a user identified by its principal (email or username).
     * @param principal Email or username of the user.
     * @return The user identified by 'principal'
     * @throws ServiceError If 'principal' matches no user.
     */
    User getUserByPrincipal(String principal) throws ServiceError;

    /**
     * Updates an enabled user in the database.
     * @param principal The principal (email or username) of the user.
     * @param newUser The new content of the user.
     *
     * @throws ServiceError If argument 'principal' points to no registered user or the user is disabled.
     */
    @Transactional( propagation = Propagation.REQUIRED)
    void updateEnabledUser(String principal, User newUser) throws ServiceError;

    /**
     * Creates a new verification token for a user.
     * @param user The user for creating the token.
     * @param token The token which should be linked to the user.
     * @return The created (and in database stored) verification token.
     *
     * @throws ServiceError: If the user is not stored in the database, the user is not disabled or token
     * is not a valid token.
     */
    VerificationToken createVerificationToken(User user, String token) throws ServiceError;

    /**
     * Provides a verification token.
     * @param verificationToken The token to search.
     * @return The found token.
     * @throws ServiceError If the verification token couldn't be found.
     */
    VerificationToken getVerificationToken(String verificationToken) throws ServiceError;
}