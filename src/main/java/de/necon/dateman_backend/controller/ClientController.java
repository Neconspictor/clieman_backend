package de.necon.dateman_backend.controller;

import de.necon.dateman_backend.model.Client;
import de.necon.dateman_backend.model.User;
import de.necon.dateman_backend.service.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class ClientController {

    private final UserService userService;

    public ClientController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/clients")
    List<Client> getClients() {
        var user = (User)SecurityContextHolder.getContext().getAuthentication().getDetails();
        var result =  userService.getClientsOfUser(user);
        result.forEach(c->{
            c.setUser(null);
        });
        return result;
    }
}