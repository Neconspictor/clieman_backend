package de.necon.dateman_backend.controller;

import de.necon.dateman_backend.model.User;
import de.necon.dateman_backend.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
public class UserController {
    private final UserRepository repository;

   // @Resource(name="authenticationManager")
   // private AuthenticationManager authManager;

    public UserController(UserRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/users")
    List<User> all() {
        return repository.findAll();
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
}
