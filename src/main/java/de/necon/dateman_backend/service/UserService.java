package de.necon.dateman_backend.service;

import de.necon.dateman_backend.exception.*;
import de.necon.dateman_backend.model.User;
import de.necon.dateman_backend.model.VerificationToken;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for interacting with users.
 */
public interface UserService {

    /**
     * Registers a new user account.
     * @param email: The email of the new user.
     * @aram password: The (not encrypted) password
     * @param username: (optional) the username. Can be null
     * @return The created user.
     * @throws ServiceError : thrown if no valid User object can be created and stored into the database or
     *  another user with the same email or username already exists. If the password length is too short
     *  (compare User.MIN_PASSWORD_LENGTH), this exception is thrown, too.
     */
    User registerNewUserAccount(String email, String password, String username)
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
     * Provides a disabled user identified by its email.
     * @param email The email of the user
     * @return The found user.
     * @throws ServiceError If no disabled user could be found.
     */
    User getDisabledUserByEmail(String email) throws ServiceError;

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
     * @throws ServiceError: If the user is not stored in the database, the user is not disabled, token
     * is not a valid token or a valid token already exists in the database.
     */
    VerificationToken createVerificationToken(User user, String token) throws ServiceError;

    /**
     * Deletes any existing verification tokens of a user.
     * @param user The user to delete the verification tokens for.
     */
    void deleteExistingVerificationToken(User user);

    /**
     * Deletes a user.
     * @param user The user to be deleted.
     * @throws ServiceError If the user is not stored in the database or the user cannot be deleted since he is referenced
     * by other entities (e.g. verification tokens).
     */
    void deleteUser(User user) throws ServiceError;

    /**
     * Provides a verification token.
     * @param verificationToken The token to search.
     * @return The found token.
     * @throws ServiceError If the verification token couldn't be found.
     */
    VerificationToken getVerificationToken(String verificationToken) throws ServiceError;


    /**
     * Changes the email of a user.
     * @param email The new email for the user.
     * @return The updated user.
     * @throws ServiceError If the new email is already occupied by another user;
     */
    User changeEmail(User user, String email) throws ServiceError;

    /**
     * Changes the password of a user.
     * @throws ServiceError If the old password does not match with the stored old password; if the confirmation password
     * does not match the new password; if the user is not enabled or does not exist;
     */
    void changePassword(User user, String oldPassword,
                        String newPassword,
                        String confirmationPassword) throws ServiceError;

    /**
     * Changes the email of a user.
     * @param username The new username. Can be null.
     * @return The updated user.
     * @throws ServiceError If the new username is already occupied by another user (except if the username is null/empty);
     */
    User changeUsername(User user, String username) throws ServiceError;


    /**
     * Validates a given user.
     * A user is valid if he is not null, has a not null id, is enabled and is stored in the database.
     * @param user The user to validate.
     * @return The user stored in the database.
     * @throws ServiceError If the user was not found.
     */
    User validateUser(User user) throws ServiceError;

    /**
     * Validates a given user.
     * A user is valid if he is not null, has a not null id, is enabled and is stored in the database.
     * @param user The user to validate.
     * @param allowDisabled : If true disabled users are treated as valid.
     * @return The user stored in the database.
     * @throws ServiceError If the user was not found.
     */
    User validateUser(User user, boolean allowDisabled) throws ServiceError;

    /**
     * Validates that a username is valid.
     * A username is valid if it is null (no username), not empty, does not contain any spaces and no other stored user
     * exists having the same username.
     * @throws ServiceError If the username is not valid.
     */
    void validateUsername(String username) throws ServiceError;
}