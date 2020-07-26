package de.necon.dateman_backend.controller;

import de.necon.dateman_backend.model.Client;
import de.necon.dateman_backend.model.User;
import de.necon.dateman_backend.service.ClientService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping("/clients/getAll")
    List<Client> getClients() {
        var user = (User)SecurityContextHolder.getContext().getAuthentication().getDetails();
        var result =  clientService.getClientsOfUser(user);
        /*result.forEach(c->{
            c.setUser(null);
        });*/
        return result;
    }

    /*@PostMapping("/clients/add")
    Client addClient(@RequestBody Client client) {
        var user = (User)SecurityContextHolder.getContext().getAuthentication().getDetails();
        client = clientService.transform(client);
        client =  clientService.addClient(client);

        return client;
    }*/
}