package fi.jubic.quanta.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fi.jubic.easyvalue.EasyValue;

import java.time.Instant;

@EasyValue
@JsonDeserialize(builder = Delta.Builder.class)
public abstract class Delta {
    public abstract Instant getTime();

    public abstract Double getSeverity();

    public abstract Builder toBuilder();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_Delta.Builder {

    }
}
