package fi.jubic.quanta.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fi.jubic.easyvalue.EasyValue;

import java.util.List;

@EasyValue
@JsonDeserialize(builder = SeriesResultData.Builder.class)
public abstract class SeriesResultData {
    public abstract SeriesResult getSeriesResult();

    public abstract List<Measurement> getMeasurements();

    public abstract Builder toBuilder();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_SeriesResultData.Builder {

    }
}
