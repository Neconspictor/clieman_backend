package de.necon.dateman_backend.service;

import de.necon.dateman_backend.exception.*;
import de.necon.dateman_backend.network.RegisterUserDto;
import de.necon.dateman_backend.model.User;
import de.necon.dateman_backend.model.VerificationToken;
import de.necon.dateman_backend.repository.UserRepository;
import de.necon.dateman_backend.repository.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static de.necon.dateman_backend.config.ServerMessages.*;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    private VerificationTokenRepository tokenRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    Environment env;

    @Override
    public User registerNewUserAccount(RegisterUserDto userDto) throws ServerErrorList {
        List<String> errors = new ArrayList<>();

        //if (userDto.getEmail() == null) {
        //    errors.add(ServerMessageCodes.NO_EMAIL);
        //}

        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            errors.add(EMAIL_ALREADY_EXISTS);
        }

        if (userDto.getUsername() != null &&
                userRepository.findByUsername(userDto.getUsername()).isPresent()) {
            errors.add(USERNAME_ALREADY_EXISTS);
        }

        if (userDto.getPassword() == null) {
            errors.add(NO_PASSWORD);

        } else if (userDto.getPassword().length() < User.MIN_PASSWORD_LENGTH) {
            errors.add(PASSWORD_TOO_SHORT);
        }

        if (errors.size() > 0) throw new ServerErrorList(errors);

        try {
            var user = new User(userDto.getEmail(),
                    encoder.encode(userDto.getPassword()),
                    userDto.getUsername(), false);
            var validator = Validation.buildDefaultValidatorFactory().getValidator();
            validator.validate(user);

            return userRepository.saveAndFlush(user); // potentially throws validation exceptions handled by global exception handler
        } catch(ConstraintViolationException e) {

            throw new ServerErrorList(e);
        }
    }


    @Override
    public void verifyUserAccount(String verificationToken) throws ItemNotFoundException, ExpiredException {

        var token = getVerificationToken(verificationToken);

        if (token.getExpiryDate().before(new Date())) {
            throw new ExpiredException("Token is expired.");
        }

        var user = getUser(verificationToken);
        user.setEnabled(true);

        tokenRepository.delete(token);
        userRepository.saveAndFlush(user);
    }

    @Override
    public User getUser(String verificationToken) throws ItemNotFoundException {
        return getVerificationToken(verificationToken).getUser();
    }

    @Override
    public void saveRegisteredUser(User user) throws UserNotRegisteredException {

        //check that the user is registerd (enabled) and stored in the databse.
        var optionalUser = userRepository.findByEmail(user.getEmail());
        if (!optionalUser.isPresent()) {
            throw new UserNotRegisteredException(USER_NOT_FOUND);
        } else if (!optionalUser.get().isEnabled()) {
            throw new UserNotRegisteredException(USER_IS_DISABLED);
        }

        userRepository.saveAndFlush(user);
    }

    @Override
    public VerificationToken createVerificationToken(User user, String token) throws TokenCreationException {
        var verificationToken = new VerificationToken(token, user);

        var optionalUser = userRepository.findByEmail(user.getEmail());

        //check that the user exists
        if (!optionalUser.isPresent()) {
            throw new TokenCreationException(USER_NOT_FOUND);
        }

        if (optionalUser.get().isEnabled()) {
            throw new TokenCreationException(USER_IS_NOT_DISABLED);
        }

        try {
            return tokenRepository.saveAndFlush(verificationToken);
        } catch(ConstraintViolationException e) {
            throw new TokenCreationException(e.getMessage());
        }
    }

    @Override
    public VerificationToken getVerificationToken(String verificationToken) throws ItemNotFoundException {
            var optional = tokenRepository.findByToken(verificationToken);
            if (!optional.isPresent())
                throw new ItemNotFoundException("Couldn't find verification token with id: " + verificationToken, null);
            return optional.get();
    }
}
