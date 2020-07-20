package de.necon.dateman_backend.integration;

import de.necon.dateman_backend.config.RepositoryConfig;
import de.necon.dateman_backend.exception.ServerErrorList;
import de.necon.dateman_backend.exception.TokenCreationException;
import de.necon.dateman_backend.model.User;
import de.necon.dateman_backend.network.RegisterUserDto;
import de.necon.dateman_backend.repository.UserRepository;
import de.necon.dateman_backend.repository.VerificationTokenRepository;
import de.necon.dateman_backend.service.UserService;
import de.necon.dateman_backend.util.Asserter;
import org.apache.commons.text.RandomStringGenerator;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static de.necon.dateman_backend.config.ServerMessages.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationTokenRepository tokenRepository;

    @BeforeEach
    public void setup() {
        userRepository.deleteAll();
    }


    @Test
    public void registerNewUserAccount_NewUserIsAdded() {

        var user = userService.registerNewUserAccount(new RegisterUserDto("test@email.com", "password", "username"));
        assertTrue(userRepository.findByEmail(user.getEmail()).isPresent());
    }

    @Test
    public void registerNewUserAccount_UserCannotBeAddedTwice() {

        assertTrue(userRepository.findAll().size() == 0);

        var dto = new RegisterUserDto("test@email.com", "password", "username");

        userService.registerNewUserAccount(dto);

        assertTrue(userRepository.findAll().size() == 1);

        ServerErrorList errorList = (ServerErrorList)Asserter.assertException(ServerErrorList.class).isThrownBy(()->{
            userService.registerNewUserAccount(dto);
        }).source();

        Asserter.assertContainsError(errorList, EMAIL_ALREADY_EXISTS);
        Asserter.assertContainsError(errorList, USERNAME_ALREADY_EXISTS);

        assertTrue(userRepository.findAll().size() == 1);
    }

    @Test
    public void registerNewUserAccount_TooShortPassworNotAllowed() {

        assertTrue(userRepository.findAll().size() == 0);

        RandomStringGenerator generator = new RandomStringGenerator.Builder()
                .withinRange('0', 'Z').build();

        var dto = new RegisterUserDto("test@email.com", generator.generate(User.MIN_PASSWORD_LENGTH - 1),
                "username");

        ServerErrorList errorList = (ServerErrorList)Asserter.assertException(ServerErrorList.class).isThrownBy(()->{
            userService.registerNewUserAccount(dto);
        }).source();

        Asserter.assertContainsError(errorList, PASSWORD_TOO_SHORT);
    }

    /**
     * Note: Too long passwords are accepted since they are cut by the the password encoder!
     */
    @Test
    public void registerNewUserAccount_TooLongPasswordIsAllowed() {

        assertTrue(userRepository.findAll().size() == 0);

        RandomStringGenerator generator = new RandomStringGenerator.Builder()
                .withinRange('0', 'Z').build();

        var dto = new RegisterUserDto("test@email.com", generator.generate(RepositoryConfig.MAX_STRING_SIZE * 2 + 1),
                "username");

        userService.registerNewUserAccount(dto);
    }

    @Test
    public void registerNewUserAccount_NoEmailNotAllowed() {

        var dto = new RegisterUserDto(null, "password",
                "username");

        ServerErrorList errorList = (ServerErrorList)Asserter.assertException(ServerErrorList.class).isThrownBy(()->{
            userService.registerNewUserAccount(dto);
        }).source();

        Asserter.assertContainsError(errorList, NO_EMAIL);
    }

    @Test
    public void registerNewUserAccount_EmptyEmailNotAllowed() {

        var dto = new RegisterUserDto("", "password",
                "username");

        ServerErrorList errorList = (ServerErrorList)Asserter.assertException(ServerErrorList.class).isThrownBy(()->{
            userService.registerNewUserAccount(dto);
        }).source();

        Asserter.assertContainsError(errorList, EMAIL_NOT_VALID);
    }

    @Test
    public void registerNewUserAccount_InvalidEmailNotAllowed() {

        var dto = new RegisterUserDto("test.com", "password",
                "username");

        ServerErrorList errorList = (ServerErrorList)Asserter.assertException(ServerErrorList.class).isThrownBy(()->{
            userService.registerNewUserAccount(dto);
        }).source();

        Asserter.assertContainsError(errorList, EMAIL_NOT_VALID);
    }

    @Test
    public void createVerificationToken_validCreation() {

        var dto = new RegisterUserDto("test@email.com", "password",
                "username");
        var user = userService.registerNewUserAccount(dto);
        var tokenString = "012583";
        var verificationToken = userService.createVerificationToken(user, tokenString);
        assertEquals(verificationToken.getToken(), tokenString);
        assertEquals(verificationToken.getUser(), user);

        var savedToken = tokenRepository.findByToken(tokenString).get();
        assertEquals(verificationToken, savedToken);
    }

    @Test
    public void createVerificationToken_enabledUserNotAllowed() {

        var user = new User("test@email.com", "password",
                "username", true);
        userRepository.saveAndFlush(user);

        var tokenString = "012583";
        Assertions.assertThatExceptionOfType(TokenCreationException.class).isThrownBy(()->{
            userService.createVerificationToken(user, tokenString);
        }).withMessage(USER_IS_NOT_DISABLED);
    }
}
