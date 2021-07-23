package fi.jubic.quanta.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fi.jubic.easyvalue.EasyValue;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@EasyValue
@JsonDeserialize(builder = AnomalyResult.Builder.class)
public abstract class AnomalyResult {
    public abstract List<Anomaly> getAnomalies();

    public abstract List<Map<String, Object>> getAggregationAnomalies();

    public abstract Builder toBuilder();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_AnomalyResult.Builder {

        @Override
        public Builder defaults(Builder builder) {
            return builder
                    .setAnomalies(Collections.emptyList())
                    .setAggregationAnomalies(Collections.emptyList());
        }
    }
}
