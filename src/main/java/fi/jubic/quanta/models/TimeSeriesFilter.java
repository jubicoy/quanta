package fi.jubic.quanta.models;

import fi.jubic.easyvalue.EasyValue;

@EasyValue
public abstract class TimeSeriesFilter {
    public abstract Column getColumn();

    public abstract String getFilterCondition();

    public abstract String getFullFilterString();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_TimeSeriesFilter.Builder {

    }
}
