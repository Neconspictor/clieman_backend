package de.necon.dateman_backend.service;

import de.necon.dateman_backend.dto.RegisterUserDto;
import de.necon.dateman_backend.exception.ExpiredException;
import de.necon.dateman_backend.exception.ServerErrorList;
import de.necon.dateman_backend.exception.ItemNotFoundException;
import de.necon.dateman_backend.model.User;
import de.necon.dateman_backend.model.VerificationToken;

public interface UserService {

    User registerNewUserAccount(RegisterUserDto userDto)
            throws ServerErrorList;

    void verifyUserAccount(String verificationToken) throws ItemNotFoundException, ExpiredException;

    User getUser(String verificationToken) throws ItemNotFoundException;

    void saveRegisteredUser(User user);

    VerificationToken createVerificationToken(User user, String token);

    VerificationToken getVerificationToken(String verificationToken) throws ItemNotFoundException;
}
