package de.necon.dateman_backend.service;

import de.necon.dateman_backend.exception.*;
import de.necon.dateman_backend.network.RegisterUserDto;
import de.necon.dateman_backend.model.User;
import de.necon.dateman_backend.model.VerificationToken;

/**
 * Service for interacting with users.
 */
public interface UserService {

    /**
     * Registers a new user account.
     * @param userDto Used for creating and registering the user.
     * @return The created user.
     * @throws ServerErrorList : thrown if no valid User object can be created and stored into the database or
     *  another user with the same email or username already exists. If the password length is too short
     *  (compare User.MIN_PASSWORD_LENGTH), this exception is thrown, too.
     */
    User registerNewUserAccount(RegisterUserDto userDto)
            throws ServerErrorList;

    /**
     * Verifies a user account using a verification token.
     * @param verificationToken The token for verifying the user.
     * @throws ItemNotFoundException If the token is not stored in the database.
     * @throws ExpiredException If the token is expired.
     */
    void verifyUserAccount(String verificationToken) throws ItemNotFoundException, ExpiredException;

    /**
     * Gets a user by its linked verification token.
     * @param verificationToken The token
     * @return The found user.
     * @throws ItemNotFoundException If the verification token does not exist.
     */
    User getUser(String verificationToken) throws ItemNotFoundException;

    /**
     * Saves or updates a registered user in the database.
     * @param user The user to be saved.
     *
     * @throws UserNotRegisteredException If the user is not registered.
     */
    void saveRegisteredUser(User user) throws UserNotRegisteredException;

    /**
     * Creates a new verification token for a user.
     * @param user The user for creating the token.
     * @param token The token which should be linked to the user.
     * @return The created (and in database stored) verification token.
     *
     * @throws TokenCreationException: If the user is not stored in the database, the user is not disabled or token
     * is not a valid token.
     */
    VerificationToken createVerificationToken(User user, String token) throws TokenCreationException;

    /**
     * Provides a verification token.
     * @param verificationToken The token to search.
     * @return The found token.
     * @throws ItemNotFoundException If the verification token couldn't be found.
     */
    VerificationToken getVerificationToken(String verificationToken) throws ItemNotFoundException;
}