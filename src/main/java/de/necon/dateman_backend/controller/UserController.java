package de.necon.dateman_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.necon.dateman_backend.model.User;
import de.necon.dateman_backend.repository.UserRepository;
import de.necon.dateman_backend.util.ResponseWriter;
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

   // @Resource(name="authenticationManager")
   // private AuthenticationManager authManager;

    private final ObjectMapper objectMapper;
    private final PasswordEncoder encoder;

    public UserController(UserRepository repository, ResponseWriter responseWriter, ObjectMapper objectMapper, PasswordEncoder encoder) {
        this.repository = repository;
        this.responseWriter = responseWriter;
        this.objectMapper = objectMapper;
        this.encoder = encoder;
    }

    @GetMapping("/users")
    List<User> all() {
        return repository.findAll();
    }

    @PostMapping("/register")
    public User register(@RequestBody @Valid final User user, final HttpServletResponse response) throws IOException {

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

        return repository.saveAndFlush(user);
    }

    /*@RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login(@RequestParam("username") final String username, @RequestParam("password") final String password, final HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authReq =
                new UsernamePasswordAuthenticationToken(username, password);
        Authentication auth = authManager.authenticate(authReq);
        SecurityContext sc = SecurityContextHolder.getContext();
        sc.setAuthentication(auth);
        //HttpSession session = request.getSession(true);
        //session.setAttribute("SPRING_SECURITY_CONTEXT", sc);

        if (auth.isAuthenticated()) {
            return "You're logged in!";
        } else {
            return "You're NOT logged in!";
        }

    }*/

    public static class LoginRequest {
        public String username;
        public String password;
    }
}
