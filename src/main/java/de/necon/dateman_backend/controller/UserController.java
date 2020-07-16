package de.necon.dateman_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.necon.dateman_backend.email.EmailServiceImpl;
import de.necon.dateman_backend.model.User;
import de.necon.dateman_backend.repository.UserRepository;
import de.necon.dateman_backend.util.ResponseWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class UserController {
    private final UserRepository repository;

    private final ResponseWriter responseWriter;
    private final EmailServiceImpl emailService;

   // @Resource(name="authenticationManager")
   // private AuthenticationManager authManager;

    private final ObjectMapper objectMapper;
    private final PasswordEncoder encoder;

    @Autowired
    private Environment env;

    public UserController(UserRepository repository,
                          ResponseWriter responseWriter,
                          EmailServiceImpl emailService,
                          ObjectMapper objectMapper,
                          PasswordEncoder encoder) {
        this.repository = repository;
        this.responseWriter = responseWriter;
        this.emailService = emailService;
        this.objectMapper = objectMapper;
        this.encoder = encoder;
    }

    @GetMapping("/users")
    List<User> all() {
        return repository.findAll();
    }

    @PostMapping("/register")
    public RegisterResponse register(@RequestBody @Valid final User user, final HttpServletResponse response) throws IOException {

        List<String> errors = new ArrayList<>();

        //check that the user isn't already registered
        if (repository.findByEmail(user.getEmail()).isPresent()) {
            errors.add(ServerMessageCodes.EMAIL_ALREADY_EXISTS);
        }

        if (repository.findByUsername(user.getUsername()).isPresent()) {
            errors.add(ServerMessageCodes.USERNAME_ALREADY_EXISTS);
        }

        if (user.getPassword().length() < User.MIN_PASSWORD_LENGTH) {
            errors.add(ServerMessageCodes.PASSWORD_TOO_SHORT);
        } else {
            user.setPassword(encoder.encode(user.getPassword()));
        }

        if (errors.size() > 0) {
            responseWriter.writeJSONErrors(errors, response);
            return null;
        }

        var savedUser = repository.saveAndFlush(user);
        var responseMessage = new RegisterResponse(savedUser.getEmail(), savedUser.getUsername());

        // if we have test environment we use a test email instead
        String testEmail = env.getProperty("dateman.test.email");
        String toEmail = savedUser.getEmail();
        if (testEmail != null) {
            toEmail = testEmail;
        }

        // send verification email to user
        emailService.sendSimpleMessage(toEmail,
                "Registered new user",
                "Registered the followng user: " + responseMessage.toString());

        return responseMessage;
    }

    public static class LoginRequest {
        public String username;
        public String password;
    }

    public static class RegisterResponse {
        public final String email;
        public final String username;

        public RegisterResponse(String email, String username) {
            this.email = email;
            this.username = username;
        }

        @Override
        public String toString() {
            return "{" + "email: " + email + ", username: " + username + "}";
        }
    }
}