package fi.jubic.quanta.models;

import fi.jubic.easyvalue.EasyValue;

import javax.annotation.Nullable;

@EasyValue
public abstract class DataSeriesColumnSelector implements SeriesSelector {
    public abstract String getName();

    public abstract String getColumnName();

    @Nullable
    public abstract String getAlias();

    @Nullable
    public Long getInvocationNumber() {
        return null;
    }

    public abstract Builder toBuilder();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_DataSeriesColumnSelector.Builder {

    }

    @Override
    public String toString() {
        return String.format(
                "series:%s.%s",
                getName(),
                getColumnName()
        );
    }
}
