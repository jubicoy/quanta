package fi.jubic.quanta.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import fi.jubic.easymapper.annotations.EasyId;
import fi.jubic.easyvalue.EasyValue;
import fi.jubic.quanta.db.tables.records.AnomalyRecord;
import fi.jubic.quanta.util.DateUtil;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

import static fi.jubic.quanta.db.tables.Anomaly.ANOMALY;

@EasyValue
@JsonDeserialize(builder = Anomaly.Builder.class)
public abstract class Anomaly {
    @EasyId
    public abstract Long getId();

    public abstract Instant getStart();

    public abstract Instant getEnd();

    public abstract Measurement getSample();

    public abstract String getClassification();

    public abstract Double getProbability();

    public abstract Map<String, Object> getMetadata();

    public abstract Builder toBuilder();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_Anomaly.Builder {
        @Override
        public Anomaly.Builder defaults(Anomaly.Builder builder) {
            return builder
                    .setId(0L)
                    .setMetadata(Collections.emptyMap());
        }
    }

    public static final AnomalyRecordMapper<AnomalyRecord> mapper = AnomalyRecordMapper
            .builder(ANOMALY)
            .setIdAccessor(ANOMALY.ID)
            .setStartAccessor(
                    ANOMALY.STARTING_TIME,
                    DateUtil::toLocalDateTime,
                    DateUtil::toInstant
            )
            .setEndAccessor(
                    ANOMALY.ENDING_TIME,
                    DateUtil::toLocalDateTime,
                    DateUtil::toInstant
            )
            .setSampleAccessor(
                    ANOMALY.SAMPLE,
                    Anomaly::sampleToString,
                    Anomaly::sampleFromString
            )
            .setClassificationAccessor(ANOMALY.CLASSIFICATION)
            .setProbabilityAccessor(ANOMALY.PROBABILITY)
            .setMetadataAccessor(
                    ANOMALY.METADATA,
                    Anomaly::metadataToString,
                    Anomaly::metadataFromString
            )
            .build();

    private static final ObjectMapper valueMapper = new ObjectMapper();

    static {
        valueMapper.registerModule(new JSR310Module());
    }

    private static String sampleToString(Measurement sample) {
        try {
            return valueMapper.writeValueAsString(sample);
        }
        catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Measurement sampleFromString(String string) {
        try {
            return valueMapper.readValue(string, Measurement.class);
        }
        catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String metadataToString(Map<String, Object> metadata) {
        try {
            return valueMapper.writeValueAsString(metadata);
        }
        catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Map<String, Object> metadataFromString(String string) {
        try {
            return valueMapper.readValue(string, Map.class);
        }
        catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}
