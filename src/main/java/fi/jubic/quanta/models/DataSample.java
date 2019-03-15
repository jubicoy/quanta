package fi.jubic.quanta.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fi.jubic.easyvalue.EasyValue;

import java.util.List;

@EasyValue
@JsonDeserialize(builder = DataSample.Builder.class)
public abstract class DataSample {
    public abstract DataSeries getDataSeries();

    public abstract List<List<String>> getData();

    public abstract Builder toBuilder();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_DataSample.Builder {

    }
}
