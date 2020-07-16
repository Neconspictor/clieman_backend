package de.necon.dateman_backend.security;

import com.auth0.jwt.JWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.necon.dateman_backend.config.SecurityConstants;
import de.necon.dateman_backend.model.LoginRequest;
import de.necon.dateman_backend.util.Asserter;
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
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import javax.crypto.Mac;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;
import static de.necon.dateman_backend.config.SecurityConstants.*;

public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final ObjectMapper objectMapper;

    public JWTAuthenticationFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest req,
                                                HttpServletResponse res) throws AuthenticationException {
        try {

            String requestBody = new String(req.getInputStream().readAllBytes());
            LoginRequest loginRequest = objectMapper.readValue(requestBody, LoginRequest.class);

            /*JsonFactory factory = new JsonFactory();

            ObjectMapper mapper = new ObjectMapper(factory);
            String json = new String(req.getInputStream().readAllBytes());
            JsonNode rootNode = mapper.readTree(json);

            String principal = null;
            String password = null;
            if (rootNode.get("username") != null) {
                principal = rootNode.get("username").asText();
            }

            if (rootNode.get("password") != null) {
                password = rootNode.get("password").asText();
            }*/

            return this.getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.username,
                            loginRequest.password,
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

        final String HMAC_SHA512 = "HmacSHA512";
        Mac sha512Hmac;
        try {
            sha512Hmac = Mac.getInstance( HMAC_SHA512);
        } catch (NoSuchAlgorithmException e) {
            throw new ServletException(e);
        }

        final String secret = SecurityConstants.getSecret();

        String token = JWT.create()
                .withSubject(((User)auth.getPrincipal()).getUsername()) // User class of userdetails!
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(HMAC512(secret.getBytes()));
        res.addHeader(HEADER_STRING, TOKEN_PREFIX + token);
    }
}