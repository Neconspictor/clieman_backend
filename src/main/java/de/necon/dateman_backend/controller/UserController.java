package de.necon.dateman_backend.controller;

import de.necon.dateman_backend.events.OnSendVerificationCodeEvent;
import de.necon.dateman_backend.events.SuccessfulAuthenticationEvent;
import de.necon.dateman_backend.network.*;
import de.necon.dateman_backend.model.User;
import de.necon.dateman_backend.service.UserService;
import de.necon.dateman_backend.util.ResponseWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.Environment;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;

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
    public UserDto register(@Valid @RequestBody RegisterUserDto userDto, final HttpServletResponse response) throws IOException {

        User savedUser  = userService.registerNewUserAccount(userDto.getEmail(),
                userDto.getPassword(), userDto.getUsername());
        eventPublisher.publishEvent(new OnSendVerificationCodeEvent(savedUser));

        //if (savedUser == null) return null;
        var responseMessage = new UserDto(savedUser.getEmail(), savedUser.getUsername());
        return responseMessage;
    }

    @PostMapping("/public/confirmUser")
    public void confirmUser(@Valid @RequestBody TokenDto tokenDto, final HttpServletResponse response) throws IOException {
        userService.verifyUserAccount(tokenDto.getToken());
    }

    @PostMapping("/public/sendVerificationCode")
    public void sendVerificationCode(@Valid @RequestBody EmailDto emailDto, final HttpServletResponse response) throws IOException {

        var user = userService.getDisabledUserByEmail(emailDto.getEmail());
        eventPublisher.publishEvent(new OnSendVerificationCodeEvent(user));
    }


    @PostMapping("/user/changeEmail")
    public UserDto changeEmail(@Valid @RequestBody EmailDto dto, final HttpServletResponse response) throws IOException {

        var user = (User) SecurityContextHolder.getContext().getAuthentication().getDetails();

        user = userService.changeEmail(user, dto.getEmail());
        eventPublisher.publishEvent(new SuccessfulAuthenticationEvent(user, response));
        return new UserDto(user.getEmail(), user.getUsername());
    }


    @PostMapping("/user/changePassword")
    public void changePassword(@Valid @RequestBody PasswordChangeDto dto, final HttpServletResponse response) throws IOException {

        var user = (User) SecurityContextHolder.getContext().getAuthentication().getDetails();
        userService.changePassword(user, dto.getOldPassword(), dto.getNewPassword(), dto.getConfirmationPassword());
    }

    @PostMapping("/user/changeUsername")
    public UserDto changeUsername(@Valid @RequestBody UsernameDto dto, final HttpServletResponse response) throws IOException {

        var user = (User) SecurityContextHolder.getContext().getAuthentication().getDetails();
        user =  userService.changeUsername(user, dto.getUsername());
        eventPublisher.publishEvent(new SuccessfulAuthenticationEvent(user, response));
        return new UserDto(user.getEmail(), user.getUsername());
    }
}