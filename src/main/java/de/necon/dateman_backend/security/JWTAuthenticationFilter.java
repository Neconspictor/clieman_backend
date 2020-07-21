package de.necon.dateman_backend.security;

import com.auth0.jwt.JWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.necon.dateman_backend.config.SecurityConstants;
import de.necon.dateman_backend.network.ExceptionToMessageMapper;
import de.necon.dateman_backend.network.LoginDto;
import de.necon.dateman_backend.repository.UserRepository;
import de.necon.dateman_backend.util.ResponseWriter;
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
import java.util.Date;
import java.util.List;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;
import static de.necon.dateman_backend.config.SecurityConstants.*;
import static de.necon.dateman_backend.config.ServiceErrorMessages.INVALID_LOGIN;

public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final ResponseWriter responseWriter;

    private final ExceptionToMessageMapper exceptionToMessageMapper;

    public JWTAuthenticationFilter(ObjectMapper objectMapper,
                                   UserRepository userRepository,
                                   ResponseWriter responseWriter,
                                   ExceptionToMessageMapper exceptionToMessageMapper) {
        this.objectMapper = objectMapper;
        this.userRepository = userRepository;
        this.responseWriter = responseWriter;
        this.exceptionToMessageMapper = exceptionToMessageMapper;
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

        final String secret = SecurityConstants.getSecret();
        String email = ((User)auth.getPrincipal()).getUsername(); // User class of userdetails!
        var optionalUser = userRepository.findByEmail(email);

        if (!optionalUser.isPresent()) throw new ServletException("Couldn't find user by this email: " + email);
        var user = optionalUser.get();


        String token = JWT.create()
                .withSubject(email)
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(HMAC512(secret.getBytes()));
        res.addHeader(HEADER_STRING, TOKEN_PREFIX + token);

        LoginResponse body = new LoginResponse(user.getEmail(), user.getUsername());
        objectMapper.writeValue(res.getWriter(), body);

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


    private static class LoginResponse {
        public final String email;
        public final String username;


        public LoginResponse(String email, String username) {
            this.email = email;
            this.username = username;
        }
    }
}