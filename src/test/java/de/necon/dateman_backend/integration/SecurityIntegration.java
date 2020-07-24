package de.necon.dateman_backend.integration;

import de.necon.dateman_backend.BaseControllerTest;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class SecurityIntegration extends BaseControllerTest {

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