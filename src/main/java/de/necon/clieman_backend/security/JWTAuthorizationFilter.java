package de.necon.clieman_backend.security;

import de.necon.clieman_backend.exception.ServiceError;
import de.necon.clieman_backend.model.User;
import de.necon.clieman_backend.repository.UserRepository;
import de.necon.clieman_backend.service.JWTTokenService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

public class JWTAuthorizationFilter extends BasicAuthenticationFilter {

    private final UserRepository userRepository;
    private final JWTTokenService tokenService;


    public JWTAuthorizationFilter(AuthenticationManager authenticationManager,
                                  UserRepository userRepository, JWTTokenService tokenService) {
        super(authenticationManager);
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws IOException, ServletException {
        String header = req.getHeader(tokenService.HEADER_STRING);

        if (header == null || !header.startsWith(tokenService.TOKEN_PREFIX)) {
            chain.doFilter(req, res);
            return;
        }

        UsernamePasswordAuthenticationToken authentication = getAuthentication(req);

        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(req, res);
    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
        String token = request.getHeader(tokenService.HEADER_STRING);

        if (token == null) { return null; }

        try {
            User user = tokenService.getFromToken(token);

            var result = new UsernamePasswordAuthenticationToken(user.getEmail(), null, new ArrayList<>());
            result.setDetails(user);
            return result;
        }catch (ServiceError e) {
        }

        return null;
    }
}