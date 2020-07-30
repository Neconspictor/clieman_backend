package de.necon.dateman_backend.controller;

import de.necon.dateman_backend.events.DtoEvent;
import de.necon.dateman_backend.events.OnSendVerificationCodeEvent;
import de.necon.dateman_backend.events.SuccessfulAuthenticationEvent;
import de.necon.dateman_backend.exception.ServiceError;
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
    public UserDto register(@RequestBody RegisterUserDto userDto, final HttpServletResponse response) throws IOException {

        eventPublisher.publishEvent(new DtoEvent(userDto));
        User savedUser  = userService.registerNewUserAccount(userDto.getEmail(),
                userDto.getPassword(), userDto.getUsername());
        eventPublisher.publishEvent(new OnSendVerificationCodeEvent(savedUser));

        //if (savedUser == null) return null;
        var responseMessage = new UserDto(savedUser.getEmail(), savedUser.getUsername());
        return responseMessage;
    }

    @PostMapping("/public/confirmUser")
    public void confirmUser(@RequestBody TokenDto tokenDto, final HttpServletResponse response) throws IOException {
        eventPublisher.publishEvent(new DtoEvent(tokenDto));
        userService.verifyUserAccount(tokenDto.getToken());
    }

    @PostMapping("/public/sendVerificationCode")
    public void sendVerificationCode(@RequestBody EmailDto emailDto, final HttpServletResponse response) throws IOException {
        eventPublisher.publishEvent(emailDto);
        var user = userService.getDisabledUserByEmail(emailDto.getEmail());
        eventPublisher.publishEvent(new OnSendVerificationCodeEvent(user));
    }


    @PostMapping("/user/changeEmail")
    public UserDto changeEmail(@RequestBody EmailDto dto, final HttpServletResponse response) throws IOException {

        var user = (User) SecurityContextHolder.getContext().getAuthentication().getDetails();

        eventPublisher.publishEvent(new DtoEvent(dto));
        user = userService.changeEmail(user, dto.getEmail());
        eventPublisher.publishEvent(new SuccessfulAuthenticationEvent(user, response));
        return new UserDto(user.getEmail(), user.getUsername());
    }


    @PostMapping("/user/changePassword")
    public void changePassword(@RequestBody PasswordChangeDto dto, final HttpServletResponse response) throws IOException {

        var user = (User) SecurityContextHolder.getContext().getAuthentication().getDetails();
        eventPublisher.publishEvent(new DtoEvent(dto));
        userService.changePassword(user, dto.getOldPassword(), dto.getNewPassword(), dto.getConfirmationPassword());
    }
}