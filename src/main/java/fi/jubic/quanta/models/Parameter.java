package fi.jubic.quanta.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fi.jubic.easymapper.MappingException;
import fi.jubic.easyvalue.EasyValue;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Map;

@EasyValue
@JsonDeserialize(builder = Parameter.Builder.class)
public abstract class Parameter {
    public abstract String getDescription();

    public abstract boolean isNullable();

    @Nullable
    public abstract String getCondition();

    @Nullable
    public abstract String getValue();

    public abstract Builder toBuilder();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_Parameter.Builder {
    }

    public static Map<String, Parameter> extractParameters(String paramJson) {
        try {
            return new ObjectMapper().readValue(
                    paramJson,
                    new TypeReference<Map<String, Parameter>>() {}
                    );
        }
        catch (IOException e) {
            throw new MappingException(e);
        }
    }

    public static String writeParameters(Map<String, Parameter> params) {
        try {
            return new ObjectMapper().writeValueAsString(params);
        }
        catch (JsonProcessingException e) {
            throw new MappingException(e);
        }
    }
}
