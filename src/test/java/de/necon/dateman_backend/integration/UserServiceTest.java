package de.necon.dateman_backend.integration;

import de.necon.dateman_backend.config.RepositoryConfig;
import de.necon.dateman_backend.exception.ServiceError;
import de.necon.dateman_backend.listeners.ResetDatabaseTestExecutionListener;
import de.necon.dateman_backend.model.User;
import de.necon.dateman_backend.model.VerificationToken;
import de.necon.dateman_backend.network.PasswordChangeDto;
import de.necon.dateman_backend.network.RegisterUserDto;
import de.necon.dateman_backend.repository.ClientRepository;
import de.necon.dateman_backend.repository.UserRepository;
import de.necon.dateman_backend.repository.VerificationTokenRepository;
import de.necon.dateman_backend.service.UserService;
import de.necon.dateman_backend.util.Asserter;
import org.apache.commons.text.RandomStringGenerator;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static de.necon.dateman_backend.config.ServiceErrorMessages.*;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@TestExecutionListeners(mergeMode =
        TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS,
        listeners = {ResetDatabaseTestExecutionListener.class}
)
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private VerificationTokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder encoder;


    @Test
    public void registerNewUserAccount_NewUserIsAdded() throws ServiceError {

        var user = userService.registerNewUserAccount(new RegisterUserDto("test@email.com", "password", "username"));
        assertTrue(userRepository.findByEmail(user.getEmail()).isPresent());
    }

    @Test
    public void registerNewUserAccount_UserCannotBeAddedTwice() throws ServiceError {

        assertTrue(userRepository.findAll().size() == 0);

        var dto = new RegisterUserDto("test@email.com", "password", "username");

        userService.registerNewUserAccount(dto);

        assertTrue(userRepository.findAll().size() == 1);

        ServiceError serviceError = (ServiceError)Asserter.assertException(ServiceError.class).isThrownBy(()->{
            userService.registerNewUserAccount(dto);
        }).source();

        var errors = serviceError.getErrors();

        Asserter.assertContainsError(errors, EMAIL_ALREADY_EXISTS);

        assertTrue(userRepository.findAll().size() == 1);
    }

    @Test
    public void registerNewUserAccount_TooShortPassworNotAllowed() {

        assertTrue(userRepository.findAll().size() == 0);

        RandomStringGenerator generator = new RandomStringGenerator.Builder()
                .withinRange('0', 'Z').build();

        var dto = new RegisterUserDto("test@email.com", generator.generate(User.MIN_PASSWORD_LENGTH - 1),
                "username");

        ServiceError serviceError = (ServiceError)Asserter.assertException(ServiceError.class).isThrownBy(()->{
            userService.registerNewUserAccount(dto);
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), PASSWORD_TOO_SHORT);
    }

    /**
     * Note: Too long passwords are accepted since they are cut by the the password encoder!
     */
    @Test
    public void registerNewUserAccount_TooLongPasswordIsAllowed() throws ServiceError {

        assertTrue(userRepository.findAll().size() == 0);

        RandomStringGenerator generator = new RandomStringGenerator.Builder()
                .withinRange('0', 'Z').build();

        var dto = new RegisterUserDto("test@email.com",
                generator.generate(RepositoryConfig.MAX_STRING_SIZE * 2 + 1),
                "username");

        userService.registerNewUserAccount(dto);
    }

    @Test
    public void registerNewUserAccount_NoEmailNotAllowed() {

        var dto = new RegisterUserDto(null, "password",
                "username");

        ServiceError serviceError = (ServiceError)Asserter.assertException(ServiceError.class).isThrownBy(()->{
            userService.registerNewUserAccount(dto);
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), NO_EMAIL);
    }

    @Test
    public void registerNewUserAccount_EmptyEmailNotAllowed() {

        var dto = new RegisterUserDto("", "password",
                "username");

        ServiceError serviceError = (ServiceError)Asserter.assertException(ServiceError.class).isThrownBy(()->{
            userService.registerNewUserAccount(dto);
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), EMAIL_NOT_VALID);
    }

    @Test
    public void registerNewUserAccount_InvalidEmailNotAllowed() {

        var dto = new RegisterUserDto("test.com", "password",
                "username");

        ServiceError serviceError = (ServiceError)Asserter.assertException(ServiceError.class).isThrownBy(()->{
            userService.registerNewUserAccount(dto);
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), EMAIL_NOT_VALID);
    }

    @Test
    public void createVerificationToken_validCreation() throws ServiceError {

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
        var serviceError  = (ServiceError)Asserter.assertException(ServiceError.class).isThrownBy(()->{
            userService.createVerificationToken(user, tokenString);
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), USER_IS_NOT_DISABLED);
    }

    @Test
    public void createVerificationToken_NullTokenIsNotAllowed() {

        var user = new User("test@email.com", "password",
                "username", false);
        userRepository.saveAndFlush(user);

        var serviceError = (ServiceError)Asserter.assertException(ServiceError.class).isThrownBy(()->{
            userService.createVerificationToken(user, null);
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), NO_TOKEN);
    }

    @Test
    public void createVerificationToken_EmptyTokenIsNotAllowed() {

        var user = new User("test@email.com", "password",
                "username", false);
        userRepository.saveAndFlush(user);

        var serviceError = (ServiceError)Asserter.assertException(ServiceError.class).isThrownBy(()->{
            userService.createVerificationToken(user, "");
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), NO_TOKEN);
    }

    @Test
    public void createVerificationToken_CreatingMultipleTokensIsNotAllowed() {

        var user = new User("test@email.com", "password",
                "username", false);
        userRepository.saveAndFlush(user);
        userService.createVerificationToken(user, "token1");


        var serviceError = (ServiceError)Asserter.assertException(ServiceError.class).isThrownBy(()->{
            userService.createVerificationToken(user, "token2");
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), ANOTHER_TOKEN_ALREADY_EXISTS);
    }

    @Test
    public void deleteExistingVerificationToken_valid() {

        var user = new User("test@email.com", "password",
                "username", false);
        userRepository.saveAndFlush(user);
        tokenRepository.saveAndFlush(new VerificationToken("token", user));

        userService.deleteExistingVerificationToken(user);

        assertEquals(0, tokenRepository.findAll().size());
    }

    @Test
    public void deleteExistingVerificationToken_valid_noTokens() {

        var user = new User("test@email.com", "password",
                "username", false);
        userRepository.saveAndFlush(user);
        userService.deleteExistingVerificationToken(user);
    }

    @Test
    public void deleteExistingVerificationToken_valid_noValidUser() {

        var user = new User("test@email.com", "password",
                "username", false);
        user.setId(93L);
        userService.deleteExistingVerificationToken(user);
    }

    @Test
    public void getVerificationToken_registeredTokenIsProvided() throws ServiceError {

        var user = new User("test@email.com", "password",
                "username", false);
        userRepository.saveAndFlush(user);

        var token = new VerificationToken("012345", user);

        tokenRepository.saveAndFlush(token);

        var providedToken = userService.getVerificationToken(token.getToken());

        assertEquals(token, providedToken);
    }

    @Test
    public void getVerificationToken_ExceptionIfNotRegisteredTokenIsRequested() {

        var tokenString = "012583";
        Assertions.assertThatExceptionOfType(ServiceError.class).isThrownBy(()->{
            userService.getVerificationToken(tokenString);
        }).withMessage(TOKEN_IS_NOT_VALID);
    }

    @Test
    public void updateEnableddUser_EnabledUserGetsUpdated_withEmail() throws ServiceError {

        //Add an enabled user
        var user = new User("test@email.com", "password",
                "username", true);
        userRepository.saveAndFlush(user);

        var updatedUser = new User("test2@demail.de", user.getPassword(), user.getUsername(), user.isEnabled());
        userService.updateEnabledUser(user.getEmail(), updatedUser);

        assertFalse(userRepository.findByEmail(user.getEmail()).isPresent());
        assertTrue(userRepository.findByEmail(updatedUser.getEmail()).isPresent());
    }

    @Test
    public void updateEnableddUser_EnabledUserGetsUpdated_withUsername() throws ServiceError {

        //Add an enabled user
        var user = new User("test@email.com", "password",
                "username", true);
        userRepository.saveAndFlush(user);

        var updatedUser = new User("test2@demail.de", user.getPassword(), user.getUsername(), user.isEnabled());
        userService.updateEnabledUser(user.getUsername(), updatedUser);

        assertFalse(userRepository.findByEmail(user.getEmail()).isPresent());
        assertTrue(userRepository.findByEmail(updatedUser.getEmail()).isPresent());
    }

    @Test
    public void updateEnableddUser_DisabledUserRejected() throws ServiceError {

        //Add an disabled user
        var user = new User("test@email.com", "password",
                "username", false);
        userRepository.saveAndFlush(user);

        var updatedUser = new User("test2@demail.de", user.getPassword(), user.getUsername(), true);

        var serviceError = (ServiceError)Asserter.assertException(ServiceError.class).isThrownBy(()->{
            userService.updateEnabledUser(user.getEmail(), updatedUser);
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), USER_IS_DISABLED);
    }

    @Test
    public void updateEnableddUser_NotExistingUserIsRejected() throws ServiceError {

        String email = "test@email.com";
        var updatedUser = new User("test2@demail.de", "password", "username", true);

        var serviceError = (ServiceError)Asserter.assertException(ServiceError.class).isThrownBy(()->{
            userService.updateEnabledUser(email, updatedUser);
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), USER_NOT_FOUND);
    }

    @Test
    public void getUserOfToken_ExistingToken() throws ServiceError {

        var user = new User("test2@demail.de", "password", "username", true);
        userRepository.saveAndFlush(user);
        String token = "01234";
        tokenRepository.saveAndFlush(new VerificationToken(token, user));

        var retrivedUser = userService.getUserOfToken(token);
        assertEquals(user, retrivedUser);
    }

    @Test
    public void getUserOfToken_NotExistingToken() throws ServiceError {

        String token = "01234";

        var serviceError = (ServiceError)Asserter.assertException(ServiceError.class).isThrownBy(()->{
            userService.getUserOfToken(token);
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), TOKEN_IS_NOT_VALID);
    }


    @Transactional
    @Test
    public void deleteUser_DeleteFailsWithLinkedToken() throws ServiceError {

        var user = new User("test2@demail.de", "password", "username", false);
        userRepository.saveAndFlush(user);
        userService.createVerificationToken(user, "01234");

        var serviceError = (ServiceError)Asserter.assertException(ServiceError.class).isThrownBy(()->{
            userService.deleteUser(user);
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), USER_IS_LINKED_TO_ENTITIES);
    }

    @Test
    public void deleteUser_ValidUser() throws ServiceError {

        var user = new User("test2@demail.de", "password", "username", false);
        userRepository.saveAndFlush(user);
        userService.deleteUser(user);
    }

    @Test
    public void deleteUser_InvalidUser() throws ServiceError {

        var user = new User("test2@demail.de", "password", "username", false);

        var serviceError = (ServiceError)Asserter.assertException(ServiceError.class).isThrownBy(()->{
            userService.deleteUser(user);
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), USER_NOT_FOUND);
    }

    @Test
    public void verifyUserAccount_verificationWorks() throws ServiceError {

        var registerUserDto = new RegisterUserDto("test2@demail.de", "password", "username");
        var user = userService.registerNewUserAccount(registerUserDto);
        var token = userService.createVerificationToken(user, "012345");

        userService.verifyUserAccount(token.getToken());

        //check that verification token is consumed
        assertFalse(tokenRepository.findByToken(token.getToken()).isPresent());

        //check that user is enabled
        var optionalUser = userRepository.findByEmail(user.getEmail());
        user = optionalUser.get();
        assertTrue(user.isEnabled());
    }

    @Test
    public void verifyUserAccount_invalidTokenNotAllowed() throws ServiceError {
        var serviceError = (ServiceError)Asserter.assertException(ServiceError.class).isThrownBy(()->{
            userService.verifyUserAccount("invalidToken");
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), TOKEN_IS_NOT_VALID);
    }

    @Test
    public void getUserByPrincipal_EmailWorks() throws ServiceError {

        var registerUserDto = new RegisterUserDto("test@email.com", "password", "username");
        var user = userService.registerNewUserAccount(registerUserDto);
        var retrievedUser = userService.getUserByPrincipal(user.getEmail());
        assertEquals(user, retrievedUser);
    }

    @Test
    public void getUserByPrincipal_UsernameWorks() throws ServiceError {

        var registerUserDto = new RegisterUserDto("test@email.com", "password", "username");
        var user = userService.registerNewUserAccount(registerUserDto);
        var retrievedUser = userService.getUserByPrincipal(user.getUsername());
        assertEquals(user, retrievedUser);
    }

    @Test
    public void getUserByPrincipal_NotExistingFails() throws ServiceError {

        var serviceError = (ServiceError)Asserter.assertException(ServiceError.class).isThrownBy(()->{
            userService.getUserByPrincipal("notExisting");
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), USER_NOT_FOUND);
    }

    @Test
    public void changePassword_invalid_userNull() {
        var serviceError = (ServiceError)Asserter.assertException(ServiceError.class).isThrownBy(()->{
            userService.changePassword(null, new PasswordChangeDto(
                    "old", "new", "new"));
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), USER_NOT_FOUND);
    }

    @Test
    public void changePassword_invalid_userIdNull() {
        var serviceError = (ServiceError)Asserter.assertException(ServiceError.class).isThrownBy(()->{
            userService.changePassword(new User(), new PasswordChangeDto(
                    "old", "new", "new"));
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), USER_NOT_FOUND);
    }

    @Test
    public void changePassword_invalid_userIdNotExisting() {
        var serviceError = (ServiceError)Asserter.assertException(ServiceError.class).isThrownBy(()->{
            var user = new User();
            user.setId(933L);
            userService.changePassword(user, new PasswordChangeDto(
                    "old", "new", "new"));
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), USER_NOT_FOUND);
    }

    @Test
    public void changePassword_invalid_oldPasswordNotMatching() {

        var user = userRepository.saveAndFlush(new User("test@email.com",
                encoder.encode("password"), null, true));

        var serviceError = (ServiceError)Asserter.assertException(ServiceError.class).isThrownBy(()->{
            userService.changePassword(user, new PasswordChangeDto(
                    "password2", "new", "new"));
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), OLD_PASSWORD_NOT_MATCHING);
    }

    @Test
    public void changePassword_invalid_UserDisabled() {

        var user = userRepository.saveAndFlush(new User("test@email.com", "oldPassword",
                null, false));

        var serviceError = (ServiceError)Asserter.assertException(ServiceError.class).isThrownBy(()->{
            userService.changePassword(user, new PasswordChangeDto(
                    "old", "new", "new"));
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), USER_IS_DISABLED);
    }

    @Test
    public void changePassword_invalid_anythingNullInDto() {

        var user = userRepository.saveAndFlush(new User("test@email.com",
                encoder.encode("password"), null, true));

        var serviceError = (ServiceError)Asserter.assertException(ServiceError.class).isThrownBy(()->{
            userService.changePassword(user, null);
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), MALFORMED_DATA);

        serviceError = (ServiceError)Asserter.assertException(ServiceError.class).isThrownBy(()->{
            userService.changePassword(user, new PasswordChangeDto(
                    null, "new", "new"));
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), MALFORMED_DATA);

        serviceError = (ServiceError)Asserter.assertException(ServiceError.class).isThrownBy(()->{
            userService.changePassword(user, new PasswordChangeDto(
                    "password", null, "newPassword"));
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), MALFORMED_DATA);

        serviceError = (ServiceError)Asserter.assertException(ServiceError.class).isThrownBy(()->{
            userService.changePassword(user, new PasswordChangeDto(
                    "password", "newPassword", null));
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), MALFORMED_DATA);
    }


    @Test
    public void changePassword_invalid_newPasswordConfirmationNotMatching() {

        var user = userRepository.saveAndFlush(new User("test@email.com",
                encoder.encode("password"), null, true));

        var serviceError = (ServiceError)Asserter.assertException(ServiceError.class).isThrownBy(()->{
            userService.changePassword(user, new PasswordChangeDto("password",
                    "newPassword", "newPassword2"));
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), NEW_PASSWORD_CONFIRMATION_NOT_MATCHING);
    }

    @Test
    public void changePassword_invalid_newPasswordTooShort() {

        var user = userRepository.saveAndFlush(new User("test@email.com",
                encoder.encode("password"), null, true));

        var serviceError = (ServiceError)Asserter.assertException(ServiceError.class).isThrownBy(()->{
            userService.changePassword(user, new PasswordChangeDto("password",
                    "new", "new"));
        }).source();

        Asserter.assertContainsError(serviceError.getErrors(), PASSWORD_TOO_SHORT);
    }

    @Test
    public void changePassword_valid() {

        var user = userRepository.saveAndFlush(new User("test@email.com",
                encoder.encode("password"), null, true));
        var dto = new PasswordChangeDto("password",
                "newPassword", "newPassword");

        userService.changePassword(user, dto);
        var storedUser = userRepository.findById(user.getId()).get();
        var newPassword = dto.getNewPassword();

        assertTrue(encoder.matches(newPassword, storedUser.getPassword()));
    }
}