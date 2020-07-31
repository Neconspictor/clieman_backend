package de.necon.dateman_backend.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.necon.dateman_backend.model.Sex;
import de.necon.dateman_backend.util.Json;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class SexTest {

    @Autowired
    Json json;

    @TestConfiguration
    public static class Config {
        @Bean
        Json json(@Autowired ObjectMapper objectMapper) {
            return new Json(objectMapper);
        }
    }

    /**
     * Asserts that sex enum is serialized in lower case.
     */
    @Test
    public void serialization_valid_lowercase() throws IOException {
        String serialized = json.serialize(Sex.DIVERSE);
        assertEquals(serialized.toLowerCase(), serialized);
    }

    /**
     * Asserts that lowercase serialization are accepted for derserializing.
     */
    @Test
    public void deserialization_valid_lowercase() throws IOException {
        String serialized = json.serialize(Sex.DIVERSE).toLowerCase();
        Sex sex = json.deserialize(serialized, Sex.class);
    }
}
