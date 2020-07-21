package de.necon.dateman_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.necon.dateman_backend.exception.ServiceError;
import de.necon.dateman_backend.network.RegisterUserDto;
import de.necon.dateman_backend.network.TokenDto;
import de.necon.dateman_backend.service.EmailService;
import de.necon.dateman_backend.service.OnRegistrationCompleteEvent;
import de.necon.dateman_backend.model.User;
import de.necon.dateman_backend.repository.UserRepository;
import de.necon.dateman_backend.service.UserService;
import de.necon.dateman_backend.util.ResponseWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static de.necon.dateman_backend.config.ServiceErrorMessages.*;

@RestController
public class UserController {
    private final UserRepository repository;

    private final ResponseWriter responseWriter;
    private final EmailService emailService;
    private final UserService userService;

    private final ObjectMapper objectMapper;
    private final PasswordEncoder encoder;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private Environment env;

    public UserController(UserRepository repository,
                          ResponseWriter responseWriter,
                          EmailService emailService,
                          UserService userService, ObjectMapper objectMapper,
                          PasswordEncoder encoder) {
        this.repository = repository;
        this.responseWriter = responseWriter;
        this.emailService = emailService;
        this.userService = userService;
        this.objectMapper = objectMapper;
        this.encoder = encoder;
    }

    @GetMapping("/users")
    List<User> users() {
        return repository.findAll();
    }

    @PostMapping("/register")
    public RegisterResponse register(@RequestBody RegisterUserDto userDto, final HttpServletResponse response) throws IOException {

        User savedUser = null;
        try {
            savedUser = userService.registerNewUserAccount(userDto);
            eventPublisher.publishEvent(new OnRegistrationCompleteEvent(savedUser));

        } catch(ServiceError e) {
            responseWriter.writeJSONErrors(e.getErrors(), response);
        }

        if (savedUser == null) return null;
        var responseMessage = new RegisterResponse(savedUser.getEmail(), savedUser.getUsername());
        return responseMessage;
    }

    @PostMapping("/confirmUser")
    public void confirmUser(@RequestBody TokenDto tokenDto, final HttpServletResponse response) throws IOException {

        var token = tokenDto.getToken();

        if (token == null) {
            responseWriter.writeJSONErrors(List.of(NO_TOKEN), response);
            return;
        }

        try {
            userService.verifyUserAccount(tokenDto.getToken());
        } catch(ServiceError e) {
            responseWriter.writeJSONErrors(e.getErrors(), response);
        }
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