package fi.jubic.quanta.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fi.jubic.easyvalue.EasyValue;

import javax.annotation.Nullable;
import java.util.List;

@EasyValue
@JsonDeserialize(builder = QueryResult.Builder.class)
public abstract class QueryResult {
    @Nullable
    public abstract Long getDataSeriesId();

    @Nullable
    public abstract Long getSeriesResultId();

    public abstract List<String> getQueryFilters();

    public abstract List<Measurement> getMeasurements();

    public abstract Builder toBuilder();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_QueryResult.Builder {
        @Override
        public QueryResult build() {
            if (getDataSeriesId() == null && getSeriesResultId() == null) {
                throw new IllegalArgumentException("No series ID provided");
            }
            return super.build();
        }
    }
}
