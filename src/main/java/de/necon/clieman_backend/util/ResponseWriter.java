package de.necon.clieman_backend.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.necon.clieman_backend.network.ErrorListDto;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Utility for writing responses to a http request.
 */
public class ResponseWriter {

    private final ObjectMapper objectMapper;

    public ResponseWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    public void writeOkRequest(Object object, final HttpServletResponse response) throws IOException {
        response.addHeader("Content-type", "application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(response.getWriter(), object);
    }

    /**
     * Writes an object as an JSON object and sets the http status to BAD REQUEST (400)
     * @param object The response, that will be written in JSON format.
     * @param response The http servlet response used for writing.
     * @throws IOException If an unexpected io error occurs.
     */
    public void writeBadRequest(Object object, final HttpServletResponse response) throws IOException {
        response.addHeader("Content-type", "application/json");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        objectMapper.writeValue(response.getWriter(), object);
    }

    /**
     * Writes a list of error messages as a JSON object to a http response and sets the http response status to
     * BAD REQUEST (400)
     * @param errors The list of error messages.
     * @param response The http servlet response used for writing.
     * @throws IOException If an unexpected io error occurs.
     */
    public void writeJSONErrors(final List<String> errors, final HttpServletResponse response) throws IOException {
        writeBadRequest(new ErrorListDto(errors), response);
    }
}
