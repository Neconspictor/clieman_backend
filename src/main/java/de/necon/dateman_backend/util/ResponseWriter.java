package de.necon.dateman_backend.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class ResponseWriter {

    private final ObjectMapper objectMapper;

    public ResponseWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    public void writeJSONErrors(final List<String> errors, final HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        var body = objectMapper.createObjectNode().set("errors", objectMapper.valueToTree(errors));
        objectMapper.writeValue(response.getWriter(), body);
    }
}
