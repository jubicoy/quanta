package fi.jubic.quanta.models;

import fi.jubic.easyvalue.EasyValue;

import javax.annotation.Nullable;

@EasyValue
public abstract class TimeSeriesGroupSelector {
    public abstract Column getColumn();

    @Nullable
    public abstract String getAlias();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_TimeSeriesGroupSelector.Builder {

    }
}
