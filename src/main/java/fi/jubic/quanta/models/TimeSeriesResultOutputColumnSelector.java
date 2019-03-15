package fi.jubic.quanta.models;

import fi.jubic.easyvalue.EasyValue;

import javax.annotation.Nullable;

@EasyValue
public abstract class TimeSeriesResultOutputColumnSelector {
    public abstract OutputColumn getOutputColumn();

    @Nullable
    public abstract String getAlias();

    @Nullable
    public abstract TimeSeriesModifier getModifier();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_TimeSeriesResultOutputColumnSelector.Builder {

    }
}
