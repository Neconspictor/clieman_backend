package de.necon.clieman_backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.necon.clieman_backend.network.ExceptionToMessageMapper;
import de.necon.clieman_backend.repository.UserRepository;
import de.necon.clieman_backend.security.JWTAuthenticationFilter;
import de.necon.clieman_backend.security.JWTAuthorizationFilter;
import de.necon.clieman_backend.security.MyBasicAuthenticationEntryPoint;
import de.necon.clieman_backend.service.JWTTokenService;
import de.necon.clieman_backend.service.UserDetailsServiceImpl;
import de.necon.clieman_backend.util.ResponseWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class WebSecurity {

    private final MyBasicAuthenticationEntryPoint authenticationEntryPoint;

    private final UserDetailsServiceImpl userDetailsService;

    private final ObjectMapper objectMapper;
    private final ResponseWriter responseWriter;
    private final UserRepository userRepository;
    private final ExceptionToMessageMapper exceptionToMessageMapper;

    private final Environment env;


    public WebSecurity(MyBasicAuthenticationEntryPoint authenticationEntryPoint,
                       UserDetailsServiceImpl userDetailsService,
                       ObjectMapper objectMapper,
                       ResponseWriter responseWriter,
                       UserRepository userRepository,
                       ExceptionToMessageMapper exceptionToMessageMapper,
                       Environment env) {
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.userDetailsService = userDetailsService;
        this.objectMapper = objectMapper;
        this.responseWriter = responseWriter;
        this.userRepository = userRepository;
        this.exceptionToMessageMapper = exceptionToMessageMapper;
        this.env = env;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public JWTAuthenticationFilter authenticationFilter(AuthenticationManager authenticationManager) {
        JWTAuthenticationFilter authenticationFilter
                = new JWTAuthenticationFilter(objectMapper,
                userRepository,
                responseWriter,
                exceptionToMessageMapper,
                jwtTokenService());

        authenticationFilter.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/public/login", "POST"));
        authenticationFilter.setAuthenticationManager(authenticationManager);
        authenticationFilter.setFilterProcessesUrl("/public/login");
        return authenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/public/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        authenticationFilter(authenticationManager), JWTAuthenticationFilter.class)
                .addFilter(new JWTAuthorizationFilter(authenticationManager, userRepository, jwtTokenService()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint));

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();
        String allowedOrigin = env.getProperty("app.cors.allowed-origin", "http://localhost:8080");
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigin));
        configuration.setAllowCredentials(true);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        configuration.setAllowedHeaders(Arrays.asList("X-Requested-With","Origin","Content-Type","Accept","Authorization"));

        // This allow us to expose the headers
        configuration.setExposedHeaders(Arrays.asList("Access-Control-Allow-Headers", "Authorization, x-xsrf-token, Access-Control-Allow-Headers, Origin, Accept, X-Requested-With, " +
                "Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers"));


        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JWTTokenService jwtTokenService() {
        var service = new  JWTTokenService(userRepository, env);
        return service;
    }
}
