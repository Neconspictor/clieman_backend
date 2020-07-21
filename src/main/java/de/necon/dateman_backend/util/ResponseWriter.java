package de.necon.dateman_backend.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.necon.dateman_backend.network.ErrorListDto;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class ResponseWriter {

    private final ObjectMapper objectMapper;

    public ResponseWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void writeBadRequest(Object object, final HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        objectMapper.writeValue(response.getWriter(), object);
    }

    public void writeJSONErrors(final List<String> errors, final HttpServletResponse response) throws IOException {
        writeBadRequest(new ErrorListDto(errors), response);
    }
}
