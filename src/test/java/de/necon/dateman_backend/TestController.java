package de.necon.dateman_backend;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * This controller is used only for testing purposes.
 */
@RestController
public class TestController {

    @RequestMapping(path = "/test", method = RequestMethod.GET)
    public String privateTestEndpoint() {
        return "private test endpoint.";
    }

    @RequestMapping(path = "/public/test", method = RequestMethod.GET)
    public String publicTestEndpoint() {
        return "public test endpoint.";
    }
}