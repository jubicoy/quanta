package fi.jubic.quanta.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fi.jubic.easyvalue.EasyValue;

import java.time.Instant;
import java.util.Map;

@EasyValue
@JsonDeserialize(builder = Measurement.Builder.class)
public abstract class Measurement {
    public abstract Instant getTime();

    public abstract Map<String, Object> getValues();

    public abstract Builder toBuilder();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_Measurement.Builder {

    }
}
