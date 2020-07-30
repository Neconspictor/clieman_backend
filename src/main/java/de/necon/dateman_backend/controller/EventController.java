package de.necon.dateman_backend.controller;

import de.necon.dateman_backend.model.Event;
import de.necon.dateman_backend.model.User;
import de.necon.dateman_backend.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
public class EventController {

    private final EventService eventService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping("/events/getAll")
    List<Event> getEvents() {
        var user = (User)SecurityContextHolder.getContext().getAuthentication().getDetails();
        return  eventService.getEventsOfUser(user);
    }

    @PostMapping("/events/add")
    Event addEvent(@Valid @RequestBody Event event) {
        var user = (User)SecurityContextHolder.getContext().getAuthentication().getDetails();
        event.setUser(user);
        return eventService.addEvent(event);
    }

    @PostMapping("/events/remove")
    void removeEvent(@Valid @RequestBody Event event) {
        var user = (User)SecurityContextHolder.getContext().getAuthentication().getDetails();
        event.setUser(user);
        eventService.removeEvent(event);
    }

    @PostMapping("/events/update")
    void updateEvent(@Valid @RequestBody Event event) {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getDetails();
        event.setUser(user);
        eventService.updateEvent(event);
    }
}