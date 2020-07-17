package de.necon.dateman_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.necon.dateman_backend.dto.RegisterUserDto;
import de.necon.dateman_backend.exception.ServerErrorList;
import de.necon.dateman_backend.service.OnRegistrationCompleteEvent;
import de.necon.dateman_backend.service.EmailServiceImpl;
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

@RestController
public class UserController {
    private final UserRepository repository;

    private final ResponseWriter responseWriter;
    private final EmailServiceImpl emailService;
    private final UserService userService;

   // @Resource(name="authenticationManager")
   // private AuthenticationManager authManager;

    private final ObjectMapper objectMapper;
    private final PasswordEncoder encoder;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private Environment env;

    public UserController(UserRepository repository,
                          ResponseWriter responseWriter,
                          EmailServiceImpl emailService,
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

        } catch(ServerErrorList e) {
            responseWriter.writeJSONErrors(e.getErrors(), response);
        }

        if (savedUser == null) return null;
        var responseMessage = new RegisterResponse(savedUser.getEmail(), savedUser.getUsername());
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