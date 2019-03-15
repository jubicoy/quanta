package fi.jubic.quanta.models;

import fi.jubic.easyvalue.EasyValue;

@EasyValue
public abstract class TimeSeriesResultOutputFilter {
    public abstract OutputColumn getOutputColumn();

    public abstract String getFilterCondition();

    public abstract String getFullFilterString();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_TimeSeriesResultOutputFilter.Builder {

    }
}
