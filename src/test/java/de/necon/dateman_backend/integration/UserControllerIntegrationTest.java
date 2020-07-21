package de.necon.dateman_backend.integration;

import com.icegreen.greenmail.store.FolderException;
import de.necon.dateman_backend.config.ServiceErrorMessages;
import de.necon.dateman_backend.network.ErrorListDto;
import de.necon.dateman_backend.network.LoginDto;
import de.necon.dateman_backend.repository.UserRepository;
import de.necon.dateman_backend.repository.VerificationTokenRepository;
import de.necon.dateman_backend.extensions.TestSmtpServer;
import de.necon.dateman_backend.model.User;
import de.necon.dateman_backend.network.RegisterUserDto;
import de.necon.dateman_backend.network.TokenDto;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.io.StringWriter;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ActiveProfiles("test")
public class UserControllerIntegrationTest extends BaseControllerIntegrationTest {

    @Autowired PasswordEncoder encoder;

    @Autowired
    Environment env;

    @Autowired
    UserRepository userRepository;

    @Autowired
    VerificationTokenRepository tokenRepository;

    private static TestSmtpServer testSmtpServer;

    private User disabledUser;
    private static final String disabledUserPassword = "password";
    private User enabledUser;
    private static final String enabledUserPassword = "password2";
    private static final String wrongPassword = "wrong password";
    private static final String notExistingUser = "not@existing.com";

    private static int MAIL_PORT;


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
    @Override
    public void setup() throws FolderException {

        super.setup();

        disabledUser = new User("test@email.com",
                encoder.encode(disabledUserPassword), "test", false);
        enabledUser = new User("test2@email.com",
                encoder.encode(enabledUserPassword), null, true);

        userRepository.deleteAll();
        tokenRepository.deleteAll();
        testSmtpServer.reset();
    }

    @Test
    public void login_enabledUserSucceedsToLogin() throws Exception {

        userRepository.saveAndFlush(enabledUser);
        var response = login(new LoginDto(enabledUser.getEmail(), enabledUserPassword));
        assertTrue(response.getStatus() == HttpStatus.OK.value());
    }

    @Test
    public void login_disabledUserFailsToLogin() throws Exception {

        userRepository.saveAndFlush(disabledUser);
        var response = login(new LoginDto(disabledUser.getEmail(), disabledUserPassword));
        assertTrue(response.getStatus() == HttpStatus.BAD_REQUEST.value());
        var errors = objectMapper.readValue(response.getContentAsString(), ErrorListDto.class).getErrors();
        assertTrue(errors.get(0).equals(ServiceErrorMessages.INVALID_LOGIN));
        assertTrue(errors.get(1).equals(ServiceErrorMessages.USER_IS_DISABLED));
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

        token.setExpiryDate(new Date());
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


    private MockHttpServletResponse confirmUser(TokenDto tokenDto) throws Exception {
        var writer = new StringWriter();
        objectMapper.writeValue(writer, tokenDto);
        return mvc.perform(post("/confirmUser").contentType("application/json")
                .content(writer.toString())).andReturn().getResponse();
    }


    private MockHttpServletResponse login(LoginDto loginDto) throws Exception {
        var writer = new StringWriter();
        objectMapper.writeValue(writer, loginDto);
        return mvc.perform(post("/login").contentType("application/json")
                .content(writer.toString())).andReturn().getResponse();
    }


    private MockHttpServletResponse registerUser(RegisterUserDto registerUserDto) throws Exception {
        var writer = new StringWriter();
        objectMapper.writeValue(writer, registerUserDto);
        return mvc.perform(post("/register").contentType("application/json")
                .content(writer.toString())).andReturn().getResponse();
    }
}