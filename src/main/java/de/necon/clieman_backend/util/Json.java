package de.necon.clieman_backend.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.StringWriter;

public class Json {

    private final ObjectMapper mapper;

    public Json(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public <T> String serialize(T t) throws IOException {
        StringWriter writer = new StringWriter();
        mapper.writeValue(writer, t);
        return writer.toString();
    }

    public <T> T deserialize(String serialized, Class<T> tClass) throws JsonProcessingException {
        return mapper.readValue(serialized, tClass);
    }
}
