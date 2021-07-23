package fi.jubic.quanta.models;

import fi.jubic.easyvalue.EasyValue;

import javax.annotation.Nullable;
import java.util.Objects;

@EasyValue
public abstract class TimeSeriesQuerySelector {
    public boolean getIsGrouping() {
        return Objects.equals(
                getModifier(),
                TimeSeriesModifier.group_by
        );
    }

    @Nullable
    public abstract String getFilterCondition();

    public boolean getIsFilter() {
        boolean isFilter = Objects.equals(
                getModifier(),
                TimeSeriesModifier.where
        );
        if (isFilter && getFilterCondition() == null) {
            throw new IllegalArgumentException("Invalid filter: missing condition value");
        }
        return isFilter;
    }

    public boolean getIsDataSelector() {
        return !getIsFilter() && !getIsGrouping();
    }

    @Nullable
    public abstract String getAlias();

    @Nullable
    public abstract TimeSeriesModifier getModifier();

    public QueryType getType() {
        if (getDataSeriesColumnSelector() != null) {
            return QueryType.series;
        }
        else if (getSeriesResultColumnSelector() != null) {
            return QueryType.result;
        }
        else if (getSeriesResultOutputColumnSelector() != null) {
            return QueryType.result_output;
        }
        throw new IllegalStateException();
    }

    public SeriesSelector getSelector() {
        if (getDataSeriesColumnSelector() != null) {
            return getDataSeriesColumnSelector();
        }
        else if (getSeriesResultColumnSelector() != null) {
            return getSeriesResultColumnSelector();
        }
        else if (getSeriesResultOutputColumnSelector() != null) {
            return getSeriesResultOutputColumnSelector();
        }
        throw new IllegalStateException();
    }

    @Nullable
    public abstract DataSeriesColumnSelector getDataSeriesColumnSelector();

    @Nullable
    public abstract SeriesResultColumnSelector getSeriesResultColumnSelector();

    @Nullable
    public abstract SeriesResultOutputColumnSelector getSeriesResultOutputColumnSelector();

    public abstract Builder toBuilder();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_TimeSeriesQuerySelector.Builder {
        @Override
        public TimeSeriesQuerySelector build() {
            if (
                    getDataSeriesColumnSelector() == null
                    && getSeriesResultColumnSelector() == null
                    && getSeriesResultOutputColumnSelector() == null
            ) {
                throw new IllegalArgumentException("Missing any DataSeries, SeriesResult, "
                        + "or SeriesResultOutput.");
            }

            return super.build();
        }
    }

    @Override
    public String toString() {
        String format;
        if (getModifier() != null) {
            if (getModifier().equals(TimeSeriesModifier.where)) {
                return String.format(
                        "where(%s%s)",
                        getSelector().toString(),
                        getFilterCondition()
                );
            }
            format = getModifier().toString() + "(%s)";
        }
        else {
            format = "%s";
        }

        return String.format(
                format,
                getSelector().toString()
        );
    }
}
