package de.necon.dateman_backend.integration;

import de.necon.dateman_backend.BaseControllerTest;
import de.necon.dateman_backend.model.User;
import de.necon.dateman_backend.repository.UserRepository;
import de.necon.dateman_backend.service.JWTTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class JWTTokenTest extends BaseControllerTest {

    @Autowired
    private JWTTokenService tokenService;

    @Autowired
    UserRepository userRepository;

    @TestConfiguration
    public static class Config {

        @Bean
        public JWTTokenService tokenService(@Autowired UserRepository userRepository) {
            return new JWTTokenService(userRepository);
        }
    }

    @Test
    public void shouldNotAllowAccessToUnauthenticatedUsers() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/test").secure(true))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldGenerateAuthToken() throws Exception {
        User user = new User("test@mail.com", "password", "tester", true);
        userRepository.saveAndFlush(user);
        String token = tokenService.createToken(user);
        assertNotNull(token);

        var tokenHeader = tokenService.createTokenHeader(token);

        mvc.perform(MockMvcRequestBuilders.get("/test")
                .header(tokenHeader.getValue0(), tokenHeader.getValue1()).secure(true))
                .andExpect(status().isOk());
    }
}
