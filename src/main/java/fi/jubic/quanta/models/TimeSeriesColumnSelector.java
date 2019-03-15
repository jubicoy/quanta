package fi.jubic.quanta.models;

import fi.jubic.easyvalue.EasyValue;

import javax.annotation.Nullable;

@EasyValue
public abstract class TimeSeriesColumnSelector {
    public abstract Column getColumn();

    @Nullable
    public abstract String getAlias();

    @Nullable
    public abstract TimeSeriesModifier getModifier();

    public static TimeSeriesColumnSelector.Builder builder() {
        return new TimeSeriesColumnSelector.Builder();
    }

    public static class Builder extends EasyValue_TimeSeriesColumnSelector.Builder {

    }
}
