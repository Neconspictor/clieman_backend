package de.necon.dateman_backend.controller;

import de.necon.dateman_backend.model.Client;
import de.necon.dateman_backend.model.User;
import de.necon.dateman_backend.service.ClientService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
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
        return  clientService.getClientsOfUser(user);
    }

    @PostMapping("/clients/add")
    Client addClient(@Valid @RequestBody Client client) {
        var user = (User)SecurityContextHolder.getContext().getAuthentication().getDetails();
        client.getId().setUser(user);
        client =  clientService.addClient(client);
        return client;
    }

    @PostMapping("/clients/remove")
    void removeClient(@Valid @RequestBody Client client) {
        var user = (User)SecurityContextHolder.getContext().getAuthentication().getDetails();
        client.getId().setUser(user);
        clientService.removeClient(client);
    }

    @PostMapping("/clients/update")
    Client updateClient(@Valid @RequestBody Client client) {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getDetails();
        client.getId().setUser(user);
        clientService.updateClient(client);
        return client;
    }
}