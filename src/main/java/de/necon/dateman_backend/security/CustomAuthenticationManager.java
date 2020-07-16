package de.necon.dateman_backend.security;

import de.necon.dateman_backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

public class CustomAuthenticationManager implements AuthenticationManager {

    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationManager.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        String principal = (String)authentication.getPrincipal();
        principal = principal == null ? "" : principal;
        String password = (String)authentication.getCredentials();
        password = password == null ? "" : password;


        logger.info("principal: " + principal);
        logger.info("password: " + password);

        var optionalUser = userRepository.findByEmail(principal);

        if (!optionalUser.isPresent())
            optionalUser = userRepository.findByUsername(principal);

        if (!optionalUser.isPresent()) throw new BadCredentialsException("Principal references no user.");

        var user = optionalUser.get();

        if (!encoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }

        return new UsernamePasswordAuthenticationToken(user.getEmail(), null, null);
    }
}
