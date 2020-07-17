package de.necon.dateman_backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.necon.dateman_backend.repository.UserRepository;
import de.necon.dateman_backend.security.JWTAuthenticationFilter;
import de.necon.dateman_backend.security.JWTAuthorizationFilter;
import de.necon.dateman_backend.security.MyBasicAuthenticationEntryPoint;
import de.necon.dateman_backend.service.UserDetailsServiceImpl;
import de.necon.dateman_backend.util.ResponseWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
@EnableWebSecurity
public class WebSecurity extends WebSecurityConfigurerAdapter {

    private final MyBasicAuthenticationEntryPoint authenticationEntryPoint;

    private final UserDetailsServiceImpl userDetailsService;

    private final ObjectMapper objectMapper;
    private final ResponseWriter responseWriter;
    private final UserRepository userRepository;

    //private final UserController userController;

    public WebSecurity(MyBasicAuthenticationEntryPoint authenticationEntryPoint,
                       UserDetailsServiceImpl userDetailsService, ObjectMapper objectMapper, ResponseWriter responseWriter, UserRepository userRepository) {
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.userDetailsService = userDetailsService;
        this.objectMapper = objectMapper;
        this.responseWriter = responseWriter;
        //this.userController = userController;
        this.userRepository = userRepository;
    }


    @Bean
    public JWTAuthenticationFilter authenticationFilter() throws Exception {
        JWTAuthenticationFilter authenticationFilter
                = new JWTAuthenticationFilter(objectMapper, userRepository, responseWriter);
        authenticationFilter.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/login", "POST"));
        authenticationFilter.setAuthenticationManager(authenticationManagerBean());
        return authenticationFilter;
    }

    /*@Bean("authenticationManager")
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        //return super.authenticationManagerBean();
        return new CustomAuthenticationManager();
    }*/

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        //.antMatchers("/login").permitAll()
        //.antMatchers(HttpMethod.POST, SIGN_UP_URL).permitAll()

        http.cors().and().csrf().disable().authorizeRequests()
        //http.cors().and().authorizeRequests()
                .antMatchers("/login").permitAll()
                .antMatchers("/register").permitAll()
                .anyRequest().authenticated()


                //.and()
                //.formLogin()
                //.loginProcessingUrl("/login")
                //.usernameParameter("username")
                //.passwordParameter("password")
                //.successHandler(this::loginSuccessHandler)
                //.failureHandler(this::loginFailureHandler)

                .and()
                .addFilterBefore(
                        authenticationFilter(), JWTAuthenticationFilter.class)
                .addFilter(new JWTAuthorizationFilter(authenticationManager()))
                //.logout()

                //.logout()
                //.logoutUrl("/logout")
                //.logoutSuccessHandler(this::logoutSuccessHandler)
                //.invalidateHttpSession(true)

                //.and()
                //.addFilter(new JWTAuthenticationFilter(authenticationManager()))
                //.addFilter(new JWTAuthorizationFilter(authenticationManager()))
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

    private void logoutSuccessHandler(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        response.setStatus(HttpStatus.OK.value());
        objectMapper.writeValue(response.getWriter(), "Bye!");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}