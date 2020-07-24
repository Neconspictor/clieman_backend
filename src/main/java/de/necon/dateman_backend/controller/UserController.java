package de.necon.dateman_backend.controller;

import de.necon.dateman_backend.exception.ServiceError;
import de.necon.dateman_backend.network.RegisterResponseDto;
import de.necon.dateman_backend.network.RegisterUserDto;
import de.necon.dateman_backend.network.TokenDto;
import de.necon.dateman_backend.service.OnRegistrationCompleteEvent;
import de.necon.dateman_backend.model.User;
import de.necon.dateman_backend.service.UserService;
import de.necon.dateman_backend.util.ResponseWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static de.necon.dateman_backend.config.ServiceErrorMessages.*;

@RestController
public class UserController {

    private final ResponseWriter responseWriter;
    private final UserService userService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private Environment env;

    public UserController(ResponseWriter responseWriter,
                          UserService userService) {
        this.responseWriter = responseWriter;
        this.userService = userService;
    }

    @PostMapping("/public/register")
    public RegisterResponseDto register(@RequestBody RegisterUserDto userDto, final HttpServletResponse response) throws IOException {

        User savedUser = null;
        try {
            savedUser = userService.registerNewUserAccount(userDto);
            eventPublisher.publishEvent(new OnRegistrationCompleteEvent(savedUser));

        } catch(ServiceError e) {
            responseWriter.writeJSONErrors(e.getErrors(), response);
        }

        if (savedUser == null) return null;
        var responseMessage = new RegisterResponseDto(savedUser.getEmail(), savedUser.getUsername());
        return responseMessage;
    }

    @PostMapping("/public/confirmUser")
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
}