package de.necon.clieman_backend.controller;

import de.necon.clieman_backend.model.Client;
import de.necon.clieman_backend.model.User;
import de.necon.clieman_backend.service.ClientService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.List;

@RestController
@Transactional
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