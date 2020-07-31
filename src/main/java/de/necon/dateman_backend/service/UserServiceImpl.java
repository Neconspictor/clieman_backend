package de.necon.dateman_backend.service;

import de.necon.dateman_backend.exception.*;
import de.necon.dateman_backend.model.User;
import de.necon.dateman_backend.model.VerificationToken;
import de.necon.dateman_backend.repository.EventRepository;
import de.necon.dateman_backend.repository.UserRepository;
import de.necon.dateman_backend.repository.VerificationTokenRepository;
import de.necon.dateman_backend.util.MessageExtractor;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import java.util.Date;

import static de.necon.dateman_backend.config.ServiceErrorMessages.*;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    UserRepository userRepository;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    EventService eventService;

    @Autowired
    private VerificationTokenRepository tokenRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    Environment env;

    @Override
    public void deleteUser(User user) throws ServiceError {

        user = validateUser(user, true);

        try {
            //Note: we have to delete events first, clients are than automatically
            // deleted when deleting the user.
            var events = eventService.getEventsOfUser(user);
            eventRepository.deleteAll(events);
            var optionalToken = tokenRepository.findByUser(user);
            if (optionalToken.isPresent()) tokenRepository.delete(optionalToken.get());

            userRepository.deleteById(user.getId());
            userRepository.flush();
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            logger.error("Exception : "+ ExceptionUtils.getStackTrace(e));
            throw new ServiceError(USER_IS_LINKED_TO_ENTITIES);
        }
    }

    @Override
    public User registerNewUserAccount(String email, String password, String username) throws ServiceError {

        if (userRepository.findByEmail(email).isPresent()) {
            throw new ServiceError(EMAIL_ALREADY_EXISTS);
        }

        validateUsername(username);

        if (username != null &&
                userRepository.findByUsername(username).isPresent()) {
            throw new ServiceError(USERNAME_ALREADY_EXISTS);
        }

        if (password.length() < User.MIN_PASSWORD_LENGTH) {
            throw new ServiceError(PASSWORD_TOO_SHORT);
        }

        try {
            var user = new User(email,
                    encoder.encode(password),
                    username, false);
            var validator = Validation.buildDefaultValidatorFactory().getValidator();
            validator.validate(user);

            return userRepository.saveAndFlush(user); // potentially throws validation exceptions handled by global exception handler
        } catch(ConstraintViolationException e) {

            throw new ServiceError(MessageExtractor.extract(e));
        }
    }


    @Override
    public void verifyUserAccount(String verificationToken) throws ServiceError{

        var token = getVerificationToken(verificationToken);

        if (token.getExpiryDate().before(new Date())) {
            throw new ServiceError(TOKEN_IS_EXPIRED);
        }

        var user = getUserOfToken(verificationToken);
        user.setEnabled(true);

        tokenRepository.delete(token);
        userRepository.saveAndFlush(user);
    }

    @Override
    public User getUserOfToken(String verificationToken) throws ServiceError {
        return getVerificationToken(verificationToken).getUser();
    }

    @Override
    public User getUserByPrincipal(String principal) throws ServiceError {
        //check that the user is registerd (enabled) and stored in the databse.
        var optionalUser = userRepository.findByEmail(principal);

        if (!optionalUser.isPresent())
            optionalUser = userRepository.findByUsername(principal);

        if (!optionalUser.isPresent()) {
            throw new ServiceError(USER_NOT_FOUND);
        }

        return optionalUser.get();
    }

    @Override
    public User getDisabledUserByEmail(String email) throws ServiceError {

        var optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) throw new ServiceError(USER_NOT_FOUND);
        var user = optionalUser.get();

        if (user.isEnabled()) throw new ServiceError(USER_IS_NOT_DISABLED);

        return user;
    }

    @Override
    public void updateEnabledUser(String principal, User user) throws ServiceError {

        var oldUser = getUserByPrincipal(principal);
        if (!oldUser.isEnabled()) {
            throw new ServiceError(USER_IS_DISABLED);
        }

        user.setId(oldUser.getId()); //we want to stay the internal id the same.
        userRepository.delete(oldUser);
        userRepository.flush(); // necessary so that the following statement doesn't raise a constraint violation.
        userRepository.saveAndFlush(user);
    }

    @Override
    public VerificationToken createVerificationToken(User user, String token) throws ServiceError {
        var verificationToken = new VerificationToken(token, user);

        user = getUserByPrincipal(user.getEmail());
        if (user.isEnabled()) {
            throw new ServiceError(USER_IS_NOT_DISABLED);
        }

        try {
            return tokenRepository.saveAndFlush(verificationToken);
        } catch(ConstraintViolationException e) {
            throw new ServiceError(MessageExtractor.extract(e));
        } catch (DataIntegrityViolationException e) {
            throw new ServiceError(ANOTHER_TOKEN_ALREADY_EXISTS);
        }
    }

    @Override
    public void deleteExistingVerificationToken(User user) {

        var optionalToken = tokenRepository.findByUser(user);
        if (optionalToken.isPresent()) {
            tokenRepository.deleteById(optionalToken.get().getId());
            tokenRepository.flush();
        }
    }

    @Override
    public VerificationToken getVerificationToken(String verificationToken) throws ServiceError {
            var optional = tokenRepository.findByToken(verificationToken);
            if (!optional.isPresent()) {
                logger.error("Couldn't find verification token with id: " + verificationToken);
                throw new ServiceError(TOKEN_IS_NOT_VALID);
            }
            return optional.get();
    }

    @Override
    public User changeEmail(User user, String email) throws ServiceError {

        validateUser(user, false);

        var optional = userRepository.findByEmail(email);
        if (optional.isPresent() && !user.getEmail().equals(email)) throw new ServiceError(EMAIL_ALREADY_EXISTS);
        user.setEmail(email);

        try {
            return userRepository.saveAndFlush(user);
        } catch (ConstraintViolationException e) {
            throw new ServiceError(MALFORMED_DATA);
        }
    }

    @Override
    public void changePassword(User user, String oldPassword, String newPassword, String confirmationPassword)
            throws ServiceError {

        if (user == null || user.getId() == null) throw new ServiceError(USER_NOT_FOUND);
        var optionalUser = userRepository.findById(user.getId());
        if (optionalUser.isEmpty()) throw new ServiceError(USER_NOT_FOUND);

        user = optionalUser.get();
        if (user.isDisabled()) throw new ServiceError(USER_IS_DISABLED);

        if (!encoder.matches(oldPassword, user.getPassword())) {
            throw new ServiceError(OLD_PASSWORD_NOT_MATCHING);
        }

        if (newPassword.length() < User.MIN_PASSWORD_LENGTH) {
            throw new ServiceError(PASSWORD_TOO_SHORT);
        }

        if (!newPassword.equals(confirmationPassword)) {
            throw new ServiceError(NEW_PASSWORD_CONFIRMATION_NOT_MATCHING);
        }

        newPassword = encoder.encode(newPassword);
        user.setPassword(newPassword);
        userRepository.saveAndFlush(user);
    }

    @Override
    public User changeUsername(User user, String username) throws ServiceError {

        validateUser(user, false);
        user = userRepository.findByEmail(user.getEmail()).get();

        var userUsername = user.getUsername();

        if (userUsername != null && userUsername.equals(username)) return user;

        validateUsername(username);
        user.setUsername(username);
        return userRepository.saveAndFlush(user);
    }

    @Override
    public void validatePassword(User user, String rawPassword) throws ServiceError {

        if (user == null || rawPassword == null) {
            throw new IllegalArgumentException("user or password are null");
        }

        var optionalUser = userRepository.findByEmail(user.getEmail());
        if (optionalUser.isEmpty()) throw new IllegalArgumentException("Expected user to be valid!");
        user = optionalUser.get();

        if (!encoder.matches(rawPassword, user.getPassword())) {
            throw new ServiceError(PASSWORD_WRONG);
        }
    }

    @Override
    public User validateUser(User user) throws ServiceError {
        return validateUser(user, false);
    }

    @Override
    public User validateUser(User user, boolean allowDisabled) throws ServiceError {
        if (user == null || user.getId() == null) throw new ServiceError(USER_NOT_FOUND);
        var optionalUser = userRepository.findById(user.getId());
        if (optionalUser.isEmpty()) throw new ServiceError(USER_NOT_FOUND);
        user = optionalUser.get();
        if (!allowDisabled && user.isDisabled()) throw new ServiceError(USER_IS_DISABLED);
        return user;
    }

    @Override
    public void validateUsername(String username) {

        if (username == null) return;

        //we do not allow an empty username and do not allow spaces
        if (username.isEmpty() || username.contains(" ")) throw new ServiceError(USERNAME_INVALID);

        if (userRepository.findByUsername(username).isPresent()) {
            throw new ServiceError(USERNAME_ALREADY_EXISTS);
        }
    }
}