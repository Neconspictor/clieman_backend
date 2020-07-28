package de.necon.dateman_backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * This controller is used only for testing purposes.
 */
@RestController
public class TestController {

    @RequestMapping(path = "/test", method = RequestMethod.GET)
    public PrivateResponseDto privateTestEndpoint() {
        return new PrivateResponseDto();
    }

    @RequestMapping(path = "/public/test", method = RequestMethod.GET)
    public String publicTestEndpoint() {
        return "public test endpoint.";
    }

    private static class PrivateResponseDto {
        public String msg;

        public PrivateResponseDto() {
            this.msg = "private endpoint";
        }
    }
}