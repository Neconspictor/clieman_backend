package de.necon.dateman_backend.config;

import de.necon.dateman_backend.util.Asserter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;

@Configuration
public class SecurityConstants {
    public static final String SECRET_ENV_VARIABLE = "dateman.DATEMAN_BACKEND_JWT_SECRET";
    public static final long EXPIRATION_TIME = 864_000_000; // 10 days
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";

    private static String secret;

    @Autowired
    Environment env;


    @PostConstruct
    public void init() {
        secret = Asserter.AssertNotNull(env.getProperty(SECRET_ENV_VARIABLE), SECRET_ENV_VARIABLE + " property not set!");
    }

    public static String getSecret()  {
        return secret;
    }
}
