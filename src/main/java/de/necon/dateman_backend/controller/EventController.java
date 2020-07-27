package de.necon.dateman_backend.controller;

import de.necon.dateman_backend.model.Client;
import de.necon.dateman_backend.model.Event;
import de.necon.dateman_backend.model.User;
import de.necon.dateman_backend.service.EventService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping("/events/getAll")
    List<Event> getEvents() {
        var user = (User)SecurityContextHolder.getContext().getAuthentication().getDetails();
        return  eventService.getEventsOfUser(user);
    }

    @PostMapping("/events/add")
    Event addEvent(@RequestBody Event event) {
        var user = (User)SecurityContextHolder.getContext().getAuthentication().getDetails();
        event.getId().setUser(user);
        return eventService.addEvent(event);
    }

    @PostMapping("/events/remove")
    void removeEvent(@RequestBody Event event) {
        var user = (User)SecurityContextHolder.getContext().getAuthentication().getDetails();
        event.getId().setUser(user);
        eventService.removeEvent(event);
    }

    @PostMapping("/events/update")
    void updateEvent(@RequestBody Event event) {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getDetails();
        event.getId().setUser(user);
        eventService.updateEvent(event);
    }
}