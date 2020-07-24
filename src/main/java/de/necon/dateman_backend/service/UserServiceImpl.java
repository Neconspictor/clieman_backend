package de.necon.dateman_backend.service;

import de.necon.dateman_backend.exception.*;
import de.necon.dateman_backend.network.RegisterUserDto;
import de.necon.dateman_backend.model.User;
import de.necon.dateman_backend.model.VerificationToken;
import de.necon.dateman_backend.repository.UserRepository;
import de.necon.dateman_backend.repository.VerificationTokenRepository;
import de.necon.dateman_backend.util.MessageExtractor;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import java.util.Date;

import static de.necon.dateman_backend.config.ServiceErrorMessages.*;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    UserRepository userRepository;

    @Autowired
    private VerificationTokenRepository tokenRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    Environment env;

    @Override
    public void deleteUser(User user) throws ServiceError {

        user = getUserByPrincipal(user.getEmail());

        try {
            userRepository.deleteById(user.getId());
            userRepository.flush();
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            logger.error("Exception : "+ ExceptionUtils.getStackTrace(e));
            throw new ServiceError(USER_IS_LINKED_TO_ENTITIES);
        }
    }

    @Override
    public User registerNewUserAccount(RegisterUserDto userDto) throws ServiceError {

        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new ServiceError(EMAIL_ALREADY_EXISTS);
        }

        if (userDto.getUsername() != null &&
                userRepository.findByUsername(userDto.getUsername()).isPresent()) {
            throw new ServiceError(USERNAME_ALREADY_EXISTS);
        }

        if (userDto.getPassword() == null) {
            throw new ServiceError(NO_PASSWORD);

        } else if (userDto.getPassword().length() < User.MIN_PASSWORD_LENGTH) {
            throw new ServiceError(PASSWORD_TOO_SHORT);
        }

        try {
            var user = new User(userDto.getEmail(),
                    encoder.encode(userDto.getPassword()),
                    userDto.getUsername(), false);
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
}
