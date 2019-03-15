package fi.jubic.quanta.models;

import fi.jubic.easyvalue.EasyValue;

import javax.annotation.Nullable;
import java.util.List;

@EasyValue
public abstract class TimeSeriesResultOutputSelector {
    @Nullable
    public abstract SeriesResult getSeriesResult();

    public boolean isRawData() {
        boolean hasNoAgg = getResultOutputColumnSelectors()
                .stream()
                .noneMatch(columnSelector -> columnSelector.getModifier() != null);
        return (getResultOutputGroupings().size() == 0 && hasNoAgg);
    }

    // Result Output Columns selected for grouping (Ordered by priority)
    public abstract List<TimeSeriesResultOutputGroupSelector> getResultOutputGroupings();

    // Result Output Columns selected with aggregation (or not)
    public abstract List<TimeSeriesResultOutputColumnSelector> getResultOutputColumnSelectors();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_TimeSeriesResultOutputSelector.Builder {
        @Override
        public TimeSeriesResultOutputSelector build() {
            // Check data
            if (getResultOutputColumnSelectors().size() <= 0) {
                throw new IllegalArgumentException("No data source is selected.");
            }
            else if (getSeriesResult() == null) {
                throw new IllegalArgumentException("SeriesResult is missing.");
            }

            return super.build();
        }
    }
}
