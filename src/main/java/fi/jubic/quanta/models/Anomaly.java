package fi.jubic.quanta.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import fi.jubic.easymapper.annotations.EasyId;
import fi.jubic.easyvalue.EasyValue;
import fi.jubic.quanta.db.tables.records.AnomalyRecord;
import fi.jubic.quanta.util.DateUtil;
import org.jooq.JSONB;

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

    public abstract Map<String, Object> getSample();

    public abstract String getClassification();

    public abstract Double getProbability();

    public abstract Map<String, Object> getMetadata();

    public abstract Builder toBuilder();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_Anomaly.Builder {
        @Override
        public Builder defaults(Builder builder) {
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
                    Anomaly::mapToJsonb,
                    Anomaly::mapFromJsonb
            )
            .setClassificationAccessor(ANOMALY.CLASSIFICATION)
            .setProbabilityAccessor(ANOMALY.PROBABILITY)
            .setMetadataAccessor(
                    ANOMALY.METADATA,
                    Anomaly::mapToJsonb,
                    Anomaly::mapFromJsonb
            )
            .build();

    private static final ObjectMapper valueMapper = new ObjectMapper();

    static {
        valueMapper.registerModule(new JSR310Module());
    }

    private static JSONB mapToJsonb(Map<String, Object> map) {
        try {
            String objectToString = valueMapper.writeValueAsString(map);
            return JSONB.jsonb(objectToString);
        }
        catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Map<String, Object> mapFromJsonb(JSONB jsonb) {
        try {
            return valueMapper.readValue(jsonb.data(), new TypeReference<Map<String, Object>>() {});
        }
        catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}
