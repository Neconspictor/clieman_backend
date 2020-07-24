package de.necon.dateman_backend.integration;

import de.necon.dateman_backend.BaseControllerTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class SecurityTest {

    @Autowired
    MockMvc mvc;

    @Test
    public void privateEndpointsNeedAuthentication() throws Exception {
        mvc.perform(get("/test").secure(true)).andExpect(status().isUnauthorized());
    }

    @Test
    public void publicEndpointsNeedNoAuthentication() throws Exception {
        mvc.perform(get("/public/test").secure(true)).andExpect(status().isOk());
    }

    @Test
    public void httpIsRedirected() throws Exception {
        mvc.perform(get("/public/test").secure(false)).andExpect(status().isFound());
    }
}