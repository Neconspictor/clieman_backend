package de.necon.clieman_backend.unit.util;

import de.necon.clieman_backend.util.Asserter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class AsserterUnitTest {

    @Test
    public void containsError_valid() {
        List<String> errors = List.of("error1", "error2", "error3");

        Asserter.assertContainsError(errors, "error2");
    }

    @Test
    public void containsError_throwsIfNotContaining() {
        List<String> errors = List.of("error1", "error2", "error3");

        Asserter.assertException(AssertionError.class).isThrownBy(()->{
            Asserter.assertContainsError(errors, "error4");
        });

    }
}
