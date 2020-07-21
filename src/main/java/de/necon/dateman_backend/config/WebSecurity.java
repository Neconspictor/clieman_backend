package de.necon.dateman_backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.necon.dateman_backend.network.ExceptionToMessageMapper;
import de.necon.dateman_backend.repository.UserRepository;
import de.necon.dateman_backend.security.JWTAuthenticationFilter;
import de.necon.dateman_backend.security.JWTAuthorizationFilter;
import de.necon.dateman_backend.security.MyBasicAuthenticationEntryPoint;
import de.necon.dateman_backend.service.UserDetailsServiceImpl;
import de.necon.dateman_backend.util.ResponseWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class WebSecurity extends WebSecurityConfigurerAdapter {

    private final MyBasicAuthenticationEntryPoint authenticationEntryPoint;

    private final UserDetailsServiceImpl userDetailsService;

    private final ObjectMapper objectMapper;
    private final ResponseWriter responseWriter;
    private final UserRepository userRepository;
    private final ExceptionToMessageMapper exceptionToMessageMapper;


    public WebSecurity(MyBasicAuthenticationEntryPoint authenticationEntryPoint,
                       UserDetailsServiceImpl userDetailsService,
                       ObjectMapper objectMapper,
                       ResponseWriter responseWriter,
                       UserRepository userRepository,
                       ExceptionToMessageMapper exceptionToMessageMapper) {
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.userDetailsService = userDetailsService;
        this.objectMapper = objectMapper;
        this.responseWriter = responseWriter;
        this.userRepository = userRepository;
        this.exceptionToMessageMapper = exceptionToMessageMapper;
    }


    @Bean
    public JWTAuthenticationFilter authenticationFilter() throws Exception {
        JWTAuthenticationFilter authenticationFilter
                = new JWTAuthenticationFilter(objectMapper, userRepository, responseWriter, exceptionToMessageMapper);
        authenticationFilter.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/login", "POST"));
        authenticationFilter.setAuthenticationManager(authenticationManagerBean());
        return authenticationFilter;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.cors().and().csrf().disable().authorizeRequests()
                .antMatchers("/login").permitAll()
                .antMatchers("/register").permitAll()
                .antMatchers("/confirmUser").permitAll()
                .anyRequest().authenticated()

                .and()
                .addFilterBefore(
                        authenticationFilter(), JWTAuthenticationFilter.class)
                .addFilter(new JWTAuthorizationFilter(authenticationManager()))

                // this disables session creation on Spring Security
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                .and()
                .exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint);
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", new CorsConfiguration().applyPermitDefaultValues());
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}