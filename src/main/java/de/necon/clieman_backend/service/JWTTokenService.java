package de.necon.clieman_backend.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import de.necon.clieman_backend.config.SecurityConstants;
import de.necon.clieman_backend.exception.ServiceError;
import de.necon.clieman_backend.model.User;
import de.necon.clieman_backend.repository.UserRepository;
import org.javatuples.Pair;
import org.springframework.core.env.Environment;

import java.util.Date;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;
import static de.necon.clieman_backend.config.ServiceErrorMessages.TOKEN_IS_NOT_VALID;
import static de.necon.clieman_backend.config.ServiceErrorMessages.USER_NOT_FOUND;

public class JWTTokenService {

    public static final long EXPIRATION_TIME = 864_000_000; // 10 days
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";

    private String secret;
    private final UserRepository userRepository;

    public JWTTokenService(UserRepository userRepository,
            Environment env) {
        this.userRepository = userRepository;
        this.secret = SecurityConstants.getSecret(env);
    }

    public String createToken(User user) {

        return JWT.create()
                .withSubject(user.getEmail())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(HMAC512(secret.getBytes()));
    }

    public static Pair<String, String> createTokenHeader(String token) {
        return new Pair<>(HEADER_STRING, TOKEN_PREFIX + token);
    }

    public User getFromToken(String token) throws ServiceError {

        String email = null;

        try {
            email = JWT.require(Algorithm.HMAC512(secret.getBytes()))
                    .build()
                    .verify(token.replace(TOKEN_PREFIX, ""))
                    .getSubject();
        } catch (com.auth0.jwt.exceptions.JWTVerificationException e) {
            throw new ServiceError(TOKEN_IS_NOT_VALID);
        }

        var optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) throw new ServiceError(USER_NOT_FOUND);
        return optionalUser.get();
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getSecret() {
        return secret;
    }
}
