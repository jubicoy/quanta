package fi.jubic.quanta.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fi.jubic.easymapper.annotations.EasyId;
import fi.jubic.easyvalue.EasyValue;
import fi.jubic.quanta.db.tables.records.DetectionResultRecord;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static fi.jubic.quanta.db.tables.DetectionResult.DETECTION_RESULT;

@EasyValue
@JsonDeserialize(builder = Anomaly.Builder.class)
public abstract class Anomaly {
    @EasyId
    public abstract Long getId();

    public abstract Instant getStart();

    public abstract Instant getEnd();

    public abstract List<Measurement> getValues();

    public abstract String getClassification();

    public abstract Double getProbability();

    public abstract Delta getDeltaMax();

    public abstract Builder toBuilder();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_Anomaly.Builder {

    }

    public static final AnomalyRecordMapper<DetectionResultRecord> mapper = AnomalyRecordMapper
            .builder(DETECTION_RESULT)
            .setIdAccessor(DETECTION_RESULT.ID)
            .setStartAccessor(
                    DETECTION_RESULT.STARTING_TIME,
                    Timestamp::from,
                    Timestamp::toInstant
            )
            .setEndAccessor(
                    DETECTION_RESULT.ENDING_TIME,
                    Timestamp::from,
                    Timestamp::toInstant
            )
            .setValuesAccessor(
                    DETECTION_RESULT.VALUES,
                    Anomaly::valuesToString,
                    Anomaly::valuesFromString
            )
            .setClassificationAccessor(DETECTION_RESULT.CLASSIFICATION)
            .setProbabilityAccessor(DETECTION_RESULT.PROBABILITY)
            .setDeltaMaxAccessor(
                    DETECTION_RESULT.DELTA_MAX,
                    Anomaly::deltaToString,
                    Anomaly::deltaFromString
            )
            .build();

    private static final ObjectMapper valueMapper = new ObjectMapper();

    private static String valuesToString(List<Measurement> values) {
        try {
            return valueMapper.writeValueAsString(values);
        }
        catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private static List<Measurement> valuesFromString(String string) {
        try {
            return Arrays.asList(
                    valueMapper.readValue(string, Measurement[].class)
            );
        }
        catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String deltaToString(Delta delta) {
        try {
            return valueMapper.writeValueAsString(delta);
        }
        catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Delta deltaFromString(String string) {
        try {
            return valueMapper.readValue(string, Delta.class);
        }
        catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}
