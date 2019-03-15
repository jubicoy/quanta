package fi.jubic.quanta.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.jubic.easymapper.MappingException;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class Json {
    public static <T> Optional<T> read(String json, Class<T> className) {
        try {
            return Optional.of(new ObjectMapper().readValue(json, className));
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public static Optional<String> writeAsString(Object object) {
        try {
            return Optional.of(new ObjectMapper().writeValueAsString(object));
        }
        catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    // Throws EasyMapper Mapping Exception
    public static Map<String, Object> extractConfig(String json) {
        try {
            return new ObjectMapper().readValue(json, new TypeReference<Map<String, Object>>() {});
        }
        catch (IOException e) {
            throw new MappingException(e);
        }
    }

    // Throws EasyMapper Mapping Exception
    public static String writeConfig(Map<String, Object> config) {
        try {
            return new ObjectMapper().writeValueAsString(config);
        }
        catch (JsonProcessingException e) {
            throw new MappingException(e);
        }
    }
}
