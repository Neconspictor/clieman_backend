package de.necon.dateman_backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.necon.dateman_backend.events.SuccessfulAuthenticationEvent;
import de.necon.dateman_backend.network.ExceptionToMessageMapper;
import de.necon.dateman_backend.network.LoginDto;
import de.necon.dateman_backend.network.LoginResponseDto;
import de.necon.dateman_backend.repository.UserRepository;
import de.necon.dateman_backend.service.JWTTokenService;
import de.necon.dateman_backend.util.ResponseWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static de.necon.dateman_backend.config.ServiceErrorMessages.INVALID_LOGIN;

public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final ResponseWriter responseWriter;

    private final ExceptionToMessageMapper exceptionToMessageMapper;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    private final JWTTokenService tokenService;

    public JWTAuthenticationFilter(ObjectMapper objectMapper,
                                   UserRepository userRepository,
                                   ResponseWriter responseWriter,
                                   ExceptionToMessageMapper exceptionToMessageMapper,
                                   JWTTokenService tokenService) {
        super();
        this.objectMapper = objectMapper;
        this.userRepository = userRepository;
        this.responseWriter = responseWriter;
        this.exceptionToMessageMapper = exceptionToMessageMapper;
        this.tokenService = tokenService;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest req,
                                                HttpServletResponse res) throws AuthenticationException {
        try {

            String requestBody = new String(req.getInputStream().readAllBytes());
            LoginDto loginDto = objectMapper.readValue(requestBody, LoginDto.class);

            return this.getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDto.getPrincipal(),
                            loginDto.getPassword(),
                            new ArrayList<>())
            );
        } catch (IOException e) {
            throw new BadCredentialsException(e.getMessage(), e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest req,
                                            HttpServletResponse res,
                                            FilterChain chain,
                                            Authentication auth) throws IOException, ServletException {

        String email = ((User)auth.getPrincipal()).getUsername(); // User class of userdetails!
        var optionalUser = userRepository.findByEmail(email);

        if (!optionalUser.isPresent()) throw new ServletException("Couldn't find user by this email: " + email);
        var user = optionalUser.get();


        eventPublisher.publishEvent(new SuccessfulAuthenticationEvent(user, res));

        LoginResponseDto body = new LoginResponseDto(user.getEmail(), user.getUsername());
        responseWriter.writeOkRequest(body, res);

        super.successfulAuthentication(req, res, chain, auth);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request,
                                    HttpServletResponse response,
                                    AuthenticationException failed)
            throws IOException, ServletException {

        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        responseWriter.writeJSONErrors(List.of(INVALID_LOGIN,
                exceptionToMessageMapper.mapExceptionToMessageCode(failed)), response);
    }


}