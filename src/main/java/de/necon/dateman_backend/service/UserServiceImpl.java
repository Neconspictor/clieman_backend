package de.necon.dateman_backend.service;

import de.necon.dateman_backend.controller.ServerMessageCodes;
import de.necon.dateman_backend.dto.RegisterUserDto;
import de.necon.dateman_backend.exception.ServerErrorList;
import de.necon.dateman_backend.model.User;
import de.necon.dateman_backend.model.VerificationToken;
import de.necon.dateman_backend.repository.UserRepository;
import de.necon.dateman_backend.repository.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

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

    @Autowired
    EmailServiceImpl emailService;

    @Override
    public User registerNewUserAccount(RegisterUserDto userDto) throws ServerErrorList {
        List<String> errors = new ArrayList<>();

        //if (userDto.getEmail() == null) {
        //    errors.add(ServerMessageCodes.NO_EMAIL);
        //}

        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            errors.add(ServerMessageCodes.EMAIL_ALREADY_EXISTS);
        }

        if (userRepository.findByUsername(userDto.getUsername()).isPresent()) {
            errors.add(ServerMessageCodes.USERNAME_ALREADY_EXISTS);
        }

        if (userDto.getPassword() == null) {
            errors.add(ServerMessageCodes.NO_PASSWORD);

        } else if (userDto.getPassword().length() < User.MIN_PASSWORD_LENGTH) {
            errors.add(ServerMessageCodes.PASSWORD_TOO_SHORT);
        }

        if (errors.size() > 0) throw new ServerErrorList(errors);

        var user = new User(userDto.getEmail(), userDto.getPassword(), userDto.getUsername(), false);

        return userRepository.saveAndFlush(user); // potentially throws validation exceptions handled by global exception handler
    }

    @Override
    public User getUser(String verificationToken) {
        return getVerificationToken(verificationToken).getUser();
    }

    @Override
    public void saveRegisteredUser(User user) {
        userRepository.saveAndFlush(user);
    }

    @Override
    public VerificationToken createVerificationToken(User user, String token) {
        var verificationToken = new VerificationToken(token, user);
        return tokenRepository.saveAndFlush(verificationToken);
    }

    @Override
    public VerificationToken getVerificationToken(String verificationToken) {
        return tokenRepository.findByToken(verificationToken);
    }
}
