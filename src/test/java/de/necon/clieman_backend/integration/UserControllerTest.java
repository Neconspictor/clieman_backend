package de.necon.clieman_backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icegreen.greenmail.store.FolderException;
import de.necon.clieman_backend.config.ServiceErrorMessages;
import de.necon.clieman_backend.network.*;
import de.necon.clieman_backend.repository.ClientRepository;
import de.necon.clieman_backend.repository.EventRepository;
import de.necon.clieman_backend.repository.UserRepository;
import de.necon.clieman_backend.repository.VerificationTokenRepository;
import de.necon.clieman_backend.extensions.TestSmtpServer;
import de.necon.clieman_backend.model.User;
import de.necon.clieman_backend.service.JWTTokenService;
import de.necon.clieman_backend.util.Asserter;
import de.necon.clieman_backend.util.ModelFactory;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.io.StringWriter;
import java.util.Date;

import static de.necon.clieman_backend.config.ServiceErrorMessages.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired PasswordEncoder encoder;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MockMvc mvc;

    @Autowired
    Environment env;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ClientRepository clientRepository;

    @Autowired
    VerificationTokenRepository tokenRepository;

    @Autowired
    JWTTokenService tokenService;

    private static TestSmtpServer testSmtpServer;

    private User disabledUser;
    private static final String disabledUserPassword = "password";
    private User enabledUser;
    private static final String enabledUserPassword = "password2";
    private static final String wrongPassword = "wrong password";
    private static final String notExistingUser = "not@existing.com";

    private static int MAIL_PORT;

    @Autowired
    ModelFactory modelFactory;

    @TestConfiguration
    public static class Config {
        @Bean
        ModelFactory modelFactory(@Autowired UserRepository userRepository,
                                  @Autowired ClientRepository clientRepository,
                                  @Autowired EventRepository eventRepository) {
            return new ModelFactory(userRepository, clientRepository, eventRepository);

        }
    }


    @BeforeAll
    public static void staticSetup(@Autowired Environment env) {
        MAIL_PORT = Integer.parseInt(env.getProperty("spring.mail.port"));
        testSmtpServer = new TestSmtpServer(MAIL_PORT);
    }

    @AfterAll
    public static void staticShutdown(@Autowired Environment env) {
        testSmtpServer.stop();
    }

    @BeforeEach
    //@Override
    public void setup() throws FolderException {

        //super.setup();

        disabledUser = new User("test@email.com",
                encoder.encode(disabledUserPassword), "test", false);
        enabledUser = new User("test2@email.com",
                encoder.encode(enabledUserPassword), null, true);

        clientRepository.deleteAll();
        tokenRepository.deleteAll();
        userRepository.deleteAll();
        testSmtpServer.reset();
    }

    @Test
    public void login_enabledUserSucceedsToLogin() throws Exception {

        var user = userRepository.saveAndFlush(enabledUser);
        var response = login(new LoginDto(enabledUser.getEmail(), enabledUserPassword));
        assertTrue(response.getStatus() == HttpStatus.OK.value());

        //check that authorization header is set and contains the token
        String authorizationHeader = response.getHeader("authorization");
        assertNotNull(authorizationHeader);
        String token = tokenService.createToken(user);
        assertTrue(authorizationHeader.contains(token));
    }

    @Test
    public void login_disabledUserFailsToLogin() throws Exception {

        userRepository.saveAndFlush(disabledUser);
        var response = login(new LoginDto(disabledUser.getEmail(), disabledUserPassword));
        assertTrue(response.getStatus() == HttpStatus.BAD_REQUEST.value());
        var errors = objectMapper.readValue(response.getContentAsString(), ErrorListDto.class).getErrors();
        assertTrue(errors.get(0).equals(ServiceErrorMessages.INVALID_LOGIN));
        assertTrue(errors.get(1).equals(USER_IS_DISABLED));
    }

    @Test
    public void login_wrongPassword() throws Exception {

        userRepository.saveAndFlush(enabledUser);
        var response = login(new LoginDto(enabledUser.getEmail(), wrongPassword));
        assertTrue(response.getStatus() == HttpStatus.BAD_REQUEST.value());
        var errors = objectMapper.readValue(response.getContentAsString(), ErrorListDto.class).getErrors();
        assertTrue(errors.get(0).equals(ServiceErrorMessages.INVALID_LOGIN));
        assertTrue(errors.get(1).equals(ServiceErrorMessages.BAD_CREDENTIALS));
    }

    @Test
    public void login_notExistingUser() throws Exception {

        var response = login(new LoginDto(notExistingUser, "somePassword"));
        assertTrue(response.getStatus() == HttpStatus.BAD_REQUEST.value());
        var errors = objectMapper.readValue(response.getContentAsString(), ErrorListDto.class).getErrors();
        assertTrue(errors.get(0).equals(ServiceErrorMessages.INVALID_LOGIN));
        assertTrue(errors.get(1).equals(ServiceErrorMessages.BAD_CREDENTIALS));
    }

    @Test
    public void register_verificationEmailIsSentToUser() throws Exception {

        assertTrue(testSmtpServer.getMessages().length == 0);

        RegisterUserDto userDto = new RegisterUserDto();
        userDto.setEmail("new@user.com");
        userDto.setPassword("password");

        var response = registerUser(userDto);
        assertTrue(response.getStatus() == HttpStatus.OK.value());

        var messages = testSmtpServer.getMessages();

        assertTrue(messages.length == 1);

        var message = messages[0];
        var recipients = message.getAllRecipients();

        assertTrue(recipients.length == 1);
        System.out.println(recipients[0].toString().equals(userDto.getEmail()));
    }


    @Test
    public void register_newUserIsDisabled() throws Exception {

        RegisterUserDto userDto = new RegisterUserDto();
        userDto.setEmail("new@user.com");
        userDto.setPassword("password");

        var response = registerUser(new RegisterUserDto("new@user.com", "password", null));
        assertTrue(response.getStatus() == HttpStatus.OK.value());

        var user = userRepository.findByEmail(userDto.getEmail()).get();
        assertTrue(!user.isEnabled());
    }

    @Test
    public void register_multipleUsersWithNoUsernameAreAllowed() throws Exception {

        var user1 = new RegisterUserDto("new@user.com", "password", null);
        var user2 = new RegisterUserDto("new2@user.com", "password", null);

        var response = registerUser(user1);
        assertTrue(response.getStatus() == HttpStatus.OK.value());

        response = registerUser(user2);
        assertTrue(response.getStatus() == HttpStatus.OK.value());
    }


    @Test
    public void confirmUser_tokenCanOnlyBeUsedOnce() throws Exception {

        var user = new RegisterUserDto("new@user.com", "password", null);
        registerUser(user);

        var token = tokenRepository.
                findAll().
                get(0).
                getToken();

        // confirm account
        var response = confirmUser(new TokenDto(token));
        assertTrue(response.getStatus() == HttpStatus.OK.value());

        //check that a second confirmation fails
        response = confirmUser(new TokenDto(token));
        assertTrue(response.getStatus() == HttpStatus.BAD_REQUEST.value());

    }

    @Test
    public void register_tokenIsCreatedAfterRegisteringUser() throws Exception {

        var user = new RegisterUserDto("new@user.com", "password", null);
        var response = registerUser(user);
        assertTrue(response.getStatus() == HttpStatus.OK.value());

        assertTrue(tokenRepository.findAll().size() == 1);
        var userOfToken = tokenRepository.
                findAll().
                get(0).
                getUser();
        var savedUser = userRepository.findByEmail(user.getEmail()).get();

        assertTrue(userOfToken.equals(savedUser));
    }

    @Test
    public void register_usernameNullIsAllowed() throws Exception {

        var user = new RegisterUserDto("new@user.com", "password", null);
        var response = registerUser(user);
        assertTrue(response.getStatus() == HttpStatus.OK.value());
    }


    @Test
    public void confirmUser_tokenActivatesUserAccount() throws Exception {

        var user = new RegisterUserDto("new@user.com", "password", null);
        registerUser(user);

        var token = tokenRepository.
                findAll().
                get(0).
                getToken();

        //check that login doesn't work
        var response = login(new LoginDto(user.getEmail(), user.getPassword()));
        assertTrue(response.getStatus() == HttpStatus.BAD_REQUEST.value());

        // confirm account
        response = confirmUser(new TokenDto(token));
        assertTrue(response.getStatus() == HttpStatus.OK.value());

        //check that login now works
        response = login(new LoginDto(user.getEmail(), user.getPassword()));
        assertTrue(response.getStatus() == HttpStatus.OK.value());

        // user should be enabled
        var savedUser = userRepository.findByEmail(user.getEmail()).get();
        assertTrue(savedUser.isEnabled());
    }


    @Test
    public void confirmUser_expiredTokenDoesntActivatesUserAccount() throws Exception {

        var user = new RegisterUserDto("new@user.com", "password", null);
        registerUser(user);

        //check that login doesn't work
        var response = login(new LoginDto(user.getEmail(), user.getPassword()));
        assertTrue(response.getStatus() == HttpStatus.BAD_REQUEST.value());

        // expire token
        var token = tokenRepository.
                findAll().
                get(0);

        token.setExpiryDate(new Date(0));
        tokenRepository.saveAndFlush(token);

        //check that user confirmation doesn't work
        response = confirmUser(new TokenDto(token.getToken()));
        assertTrue(response.getStatus() == HttpStatus.BAD_REQUEST.value());
        var errorList = objectMapper.readValue(response.getContentAsString(), ErrorListDto.class);
        errorList.getErrors().get(0).equals(ServiceErrorMessages.TOKEN_IS_EXPIRED);

        // user should be disabled
        var savedUser = userRepository.findByEmail(user.getEmail()).get();
        assertTrue(!savedUser.isEnabled());
    }


    @Test
    public void confirmUser_noTokenSpecified() throws Exception {

        var user = new RegisterUserDto("new@user.com", "password", null);
        registerUser(user);

        //check that login doesn't work
        var response = login(new LoginDto(user.getEmail(), user.getPassword()));
        assertTrue(response.getStatus() == HttpStatus.BAD_REQUEST.value());


        //check that user confirmation doesn't work if no token is specified
        response = confirmUser(new TokenDto(null));
        assertTrue(response.getStatus() == HttpStatus.BAD_REQUEST.value());
        var errorList = objectMapper.readValue(response.getContentAsString(), ErrorListDto.class);
        errorList.getErrors().get(0).equals(ServiceErrorMessages.NO_TOKEN);
    }

    @Test
    public void confirmUser_invalidTokenSpecified() throws Exception {

        var user = new RegisterUserDto("new@user.com", "password", null);
        registerUser(user);

        //check that login doesn't work
        var response = login(new LoginDto(user.getEmail(), user.getPassword()));
        assertTrue(response.getStatus() == HttpStatus.BAD_REQUEST.value());


        //check that user confirmation doesn't work if an invalid token is specified
        response = confirmUser(new TokenDto("a"));
        assertTrue(response.getStatus() == HttpStatus.BAD_REQUEST.value());
        var errorList = objectMapper.readValue(response.getContentAsString(), ErrorListDto.class);
        errorList.getErrors().get(0).equals(ServiceErrorMessages.TOKEN_IS_NOT_VALID);
    }

    @Test
    public void sendVerificationCode_valid() throws Exception {

        var user = userRepository.save(new User("test@email.com", "password", null, false));
        var response = sendVerificationCode(new EmailDto(user.getEmail()));
        assertTrue(response.getStatus() == HttpStatus.OK.value());

        //check that email was send
        var messages = testSmtpServer.getMessages();
        assertEquals(1, messages.length);
        var message = messages[0];
        var recipients = message.getAllRecipients();
        assertEquals(1, recipients.length);
        var recipient = recipients[0];
        assertEquals(user.getEmail(), recipient.toString());

        var content = (String) message.getContent();
        var token = tokenRepository.findByUser(user).get();
        assertTrue(content.contains(token.getToken()));
    }

    @Test
    public void sendVerificationCode_invalid_noUserFound() throws Exception {

        var user = new User("test@email.com", "password", null, false);
        user.setId(1922L);
        var response = sendVerificationCode(new EmailDto(user.getEmail()));
        assertTrue(response.getStatus() == HttpStatus.BAD_REQUEST.value());

        var errorList = objectMapper.readValue(response.getContentAsString(), ErrorListDto.class);
        errorList.getErrors().get(0).equals(ServiceErrorMessages.USER_NOT_FOUND);

        //check that no email was send
        var messages = testSmtpServer.getMessages();
        assertEquals(0, messages.length);
    }

    @Test
    public void sendVerificationCode_invalid_nothingSend() throws Exception {

        var response = sendVerificationCode(null);
        assertTrue(response.getStatus() == HttpStatus.BAD_REQUEST.value());

        var errorList = objectMapper.readValue(response.getContentAsString(), ErrorListDto.class);
        errorList.getErrors().get(0).equals(MALFORMED_DATA);
    }

    @Test
    public void sendVerificationCode_invalid_enabledUser() throws Exception {

        var user = new User("test@email.com", "password", null, true);
        user = userRepository.save(user);
        var response = sendVerificationCode(new EmailDto(user.getEmail()));
        assertTrue(response.getStatus() == HttpStatus.BAD_REQUEST.value());

        var errorList = objectMapper.readValue(response.getContentAsString(), ErrorListDto.class);
        errorList.getErrors().get(0).equals(ServiceErrorMessages.USER_IS_NOT_DISABLED);

        //check that no email was send
        var messages = testSmtpServer.getMessages();
        assertEquals(0, messages.length);
    }

    @Test
    public void sendVerificationCode_invalid_noEmail() throws Exception {

        var response = sendVerificationCode(new EmailDto());
        assertTrue(response.getStatus() == HttpStatus.BAD_REQUEST.value());

        var errorList = objectMapper.readValue(response.getContentAsString(), ErrorListDto.class);
        errorList.getErrors().get(0).equals(ServiceErrorMessages.USER_NOT_FOUND);

        //check that no email was send
        var messages = testSmtpServer.getMessages();
        assertEquals(0, messages.length);
    }

    @Test
    public void changePassword_valid() throws Exception {

        var oldPassword = "password";
        var newPassword = "newPassword";

        var user = new User("test@email.com", encoder.encode(oldPassword), null, true);
        user = userRepository.save(user);

        var dto = new PasswordChangeDto(oldPassword, newPassword, newPassword);

        var response = changePassword(dto, tokenService.createToken(user));
        assertTrue(response.getStatus() == HttpStatus.OK.value());

        var encodedPassword = userRepository.findById(user.getId()).get().getPassword();
        assertTrue(encoder.matches(newPassword, encodedPassword));
    }

    @Test
    public void changePassword_invalid() throws Exception {

        var oldPassword = "password";
        var newPassword = "newPassword";

        var user = new User("test@email.com", encoder.encode(oldPassword), null, true);
        user = userRepository.save(user);

        var dto = new PasswordChangeDto(oldPassword + "dfdf", newPassword, newPassword);

        var response = changePassword(dto, tokenService.createToken(user));
        assertTrue(response.getStatus() == HttpStatus.BAD_REQUEST.value());

        var errorList = objectMapper.readValue(response.getContentAsString(), ErrorListDto.class);
        Asserter.assertContainsError(errorList.getErrors(), OLD_PASSWORD_NOT_MATCHING);
    }

    @Test
    public void changeEmail_invalid_dtoNull() throws Exception {

        var user = new User("test@email.com", "password", null, true);
        user = userRepository.save(user);

        var response = changeEmail(null, tokenService.createToken(user));
        assertTrue(response.getStatus() == HttpStatus.BAD_REQUEST.value());

        var errorList = objectMapper.readValue(response.getContentAsString(), ErrorListDto.class);
        Asserter.assertContainsError(errorList.getErrors(), MALFORMED_DATA);
    }

    @Test
    public void changeEmail_invalid_EmailNull() throws Exception {

        var user = new User("test@email.com", "password", null, true);
        user = userRepository.save(user);

        var response = changeEmail(new EmailDto(null), tokenService.createToken(user));
        assertTrue(response.getStatus() == HttpStatus.BAD_REQUEST.value());

        var errorList = objectMapper.readValue(response.getContentAsString(), ErrorListDto.class);
        Asserter.assertContainsError(errorList.getErrors(), MALFORMED_DATA);
    }

    @Test
    public void changeEmail_valid() throws Exception {

        var user = new User("test@email.com", "password", null, true);
        user = userRepository.save(user);

        String newEmail = "new@email.com";

        String token = tokenService.createToken(user);

        var response = changeEmail(new EmailDto(newEmail), token);
        assertTrue(response.getStatus() == HttpStatus.OK.value());

        var userDto = objectMapper.readValue(response.getContentAsString(), UserDto.class);
        assertEquals(newEmail, userDto.email);
        assertEquals(user.getUsername(), userDto.username);

        //check that authorization header is set and contains the token
        String authorizationHeader = response.getHeader("authorization");
        assertNotNull(authorizationHeader);

        //Note: we have to recreate the token since it depends on the email
        token = tokenService.createToken(userRepository.findById(user.getId()).get());
        assertTrue(authorizationHeader.contains(token));
    }

    @Test
    public void changeUsername_invalid_dtoNull() throws Exception {
        var user = new User("test@email.com", "password", null, true);
        user = userRepository.save(user);

        var response = changeUsername(null, tokenService.createToken(user));
        assertTrue(response.getStatus() == HttpStatus.BAD_REQUEST.value());

        var errorList = objectMapper.readValue(response.getContentAsString(), ErrorListDto.class);
        Asserter.assertContainsError(errorList.getErrors(), MALFORMED_DATA);
    }

    @Test
    public void changeUsername_invalid_spaces() throws Exception {
        var user = new User("test@email.com", "password", null, true);
        user = userRepository.save(user);

        var response = changeUsername(new UsernameDto(" "), tokenService.createToken(user));
        assertTrue(response.getStatus() == HttpStatus.BAD_REQUEST.value());

        var errorList = objectMapper.readValue(response.getContentAsString(), ErrorListDto.class);
        Asserter.assertContainsError(errorList.getErrors(), USERNAME_INVALID);
    }

    @Test
    public void changeUsername_invalid_alreadyExists() throws Exception {
        var user = new User("test@email.com", "password", null, true);
        user = userRepository.save(user);

        var user2 = new User("test2@email.com", "password", "test2", true);
        user2 = userRepository.save(user2);

        var response = changeUsername(new UsernameDto(user2.getUsername()), tokenService.createToken(user));
        assertTrue(response.getStatus() == HttpStatus.BAD_REQUEST.value());

        var errorList = objectMapper.readValue(response.getContentAsString(), ErrorListDto.class);
        Asserter.assertContainsError(errorList.getErrors(), USERNAME_ALREADY_EXISTS);
    }

    @Test
    public void changeUsername_invalid_userNotEnabled() throws Exception {
        var user = new User("test@email.com", "password", null, false);
        user = userRepository.save(user);

        var response = changeUsername(new UsernameDto("test"), tokenService.createToken(user));
        assertTrue(response.getStatus() == HttpStatus.BAD_REQUEST.value());

        var errorList = objectMapper.readValue(response.getContentAsString(), ErrorListDto.class);
        Asserter.assertContainsError(errorList.getErrors(), USER_IS_DISABLED);
    }


    @Test
    public void changeUsername_valid() throws Exception {
        var user = new User("test@email.com", "password", null, true);
        user = userRepository.save(user);

        String token = tokenService.createToken(user);

        var response = changeUsername(new UsernameDto("test"), token);
        assertTrue(response.getStatus() == HttpStatus.OK.value());

        var userDto = objectMapper.readValue(response.getContentAsString(), UserDto.class);
        assertEquals(user.getEmail(), userDto.email);
        assertEquals("test", userDto.username);

        //check that authorization header is set and contains the token
        String authorizationHeader = response.getHeader("authorization");
        assertNotNull(authorizationHeader);
        assertTrue(authorizationHeader.contains(token));
    }

    @Test
    public void deleteUser_invalid_passwordWrong() throws Exception {
        var user = modelFactory.createUser("test@email.com", true, false);
        var rawPassword = "password";
        user.setPassword(encoder.encode(rawPassword));
        user = userRepository.save(user);

        String token = tokenService.createToken(user);

        var response = deleteUser(new PasswordDto("wrongPassword"), token);
        assertTrue(response.getStatus() == HttpStatus.BAD_REQUEST.value());

        var errors = objectMapper.readValue(response.getContentAsString(), ErrorListDto.class);
        Asserter.assertContainsError(errors.getErrors(), PASSWORD_WRONG);
    }

    @Test
    public void deleteUser_invalid_dtoNull() throws Exception {
        var user = modelFactory.createUser("test@email.com", true, false);
        var rawPassword = "password";
        user.setPassword(encoder.encode(rawPassword));
        user = userRepository.save(user);

        String token = tokenService.createToken(user);

        var response = deleteUser(null, token);
        assertTrue(response.getStatus() == HttpStatus.BAD_REQUEST.value());

        var errors = objectMapper.readValue(response.getContentAsString(), ErrorListDto.class);
        Asserter.assertContainsError(errors.getErrors(), MALFORMED_DATA);
    }

    @Test
    public void deleteUser_invalid_passwordNull() throws Exception {
        var user = modelFactory.createUser("test@email.com", true, false);
        var rawPassword = "password";
        user.setPassword(encoder.encode(rawPassword));
        user = userRepository.save(user);

        String token = tokenService.createToken(user);

        var response = deleteUser(new PasswordDto(null), token);
        assertTrue(response.getStatus() == HttpStatus.BAD_REQUEST.value());

        var errors = objectMapper.readValue(response.getContentAsString(), ErrorListDto.class);
        Asserter.assertContainsError(errors.getErrors(), MALFORMED_DATA);
    }

    @Test
    public void deleteUser_valid() throws Exception {
        var user = modelFactory.createUser("test@email.com", true, false);
        var rawPassword = "password";
        user.setPassword(encoder.encode(rawPassword));
        user = userRepository.saveAndFlush(user);

        String token = tokenService.createToken(user);

        var response = deleteUser(new PasswordDto(rawPassword), token);
        assertTrue(response.getStatus() == HttpStatus.OK.value());

        //check that user is deleted
        assertEquals(0, userRepository.findAll().size());

    }

    @Test
    public void changeUsername_valid_nullAllowed() throws Exception {
        var user = new User("test@email.com", "password", null, true);
        user = userRepository.save(user);

        var response = changeUsername(new UsernameDto(null), tokenService.createToken(user));
        assertTrue(response.getStatus() == HttpStatus.OK.value());
    }

    @Test
    public void changeUsername_valid_nullAllowedForMultipleUsers() throws Exception {
        var user = userRepository.save(new User("test@email.com", "password", "test", true));
        var user2 = userRepository.save(new User("test2@email.com", "password", null, true));

        var response = changeUsername(new UsernameDto(null), tokenService.createToken(user));
        assertTrue(response.getStatus() == HttpStatus.OK.value());
    }


    private MockHttpServletResponse confirmUser(TokenDto tokenDto) throws Exception {
        var writer = new StringWriter();
        objectMapper.writeValue(writer, tokenDto);
        return mvc.perform(post("/public/confirmUser").secure(true).contentType("application/json")
                .content(writer.toString())).andReturn().getResponse();
    }


    private MockHttpServletResponse login(LoginDto loginDto) throws Exception {
        var writer = new StringWriter();
        objectMapper.writeValue(writer, loginDto);
        return mvc.perform(post("/public/login").secure(true).contentType("application/json")
                .content(writer.toString())).andReturn().getResponse();
    }


    private MockHttpServletResponse registerUser(RegisterUserDto registerUserDto) throws Exception {
        var writer = new StringWriter();
        objectMapper.writeValue(writer, registerUserDto);
        return mvc.perform(post("/public/register").secure(true).contentType("application/json")
                .content(writer.toString())).andReturn().getResponse();
    }

    private MockHttpServletResponse sendVerificationCode(EmailDto emailDto) throws Exception {
        var writer = new StringWriter();
        if (emailDto != null)
            objectMapper.writeValue(writer, emailDto);
        return mvc.perform(post("/public/sendVerificationCode").secure(true).contentType("application/json")
                .content(writer.toString())).andReturn().getResponse();
    }

    private MockHttpServletResponse changeEmail(EmailDto dto, String token) throws Exception {
        var header = JWTTokenService.createTokenHeader(token);
        var writer = new StringWriter();
        objectMapper.writeValue(writer, dto);
        return mvc.perform(post("/user/changeEmail")
                .header(header.getValue0(), header.getValue1())
                .secure(true)
                .contentType("application/json")
                .content(writer.toString())).andReturn().getResponse();
    }

    private MockHttpServletResponse changePassword(PasswordChangeDto dto, String token) throws Exception {
        var header = JWTTokenService.createTokenHeader(token);
        var writer = new StringWriter();
        objectMapper.writeValue(writer, dto);
        return mvc.perform(post("/user/changePassword")
                .header(header.getValue0(), header.getValue1())
                .secure(true)
                .contentType("application/json")
                .content(writer.toString())).andReturn().getResponse();
    }

    private MockHttpServletResponse changeUsername(UsernameDto dto, String token) throws Exception {
        var header = JWTTokenService.createTokenHeader(token);
        var writer = new StringWriter();
        objectMapper.writeValue(writer, dto);
        return mvc.perform(post("/user/changeUsername")
                .header(header.getValue0(), header.getValue1())
                .secure(true)
                .contentType("application/json")
                .content(writer.toString())).andReturn().getResponse();
    }

    private MockHttpServletResponse deleteUser(PasswordDto dto, String token) throws Exception {
        var header = JWTTokenService.createTokenHeader(token);
        var writer = new StringWriter();
        objectMapper.writeValue(writer, dto);
        return mvc.perform(post("/user/deleteUser")
                .header(header.getValue0(), header.getValue1())
                .secure(true)
                .contentType("application/json")
                .content(writer.toString())).andReturn().getResponse();
    }
}