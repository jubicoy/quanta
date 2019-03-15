package fi.jubic.quanta.models;

import fi.jubic.easyvalue.EasyValue;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

@EasyValue
public abstract class TimeSeriesSelector {
    @Nullable
    public abstract DataSeries getDataSeries();

    @Nullable
    public abstract SeriesResult getSeriesResult();

    @Nullable
    public abstract SeriesResult getSeriesResultOutput();

    public boolean isRawData() {
        boolean hasNoAgg = getColumnSelectors()
                .stream()
                .noneMatch(columnSelect -> columnSelect.getModifier() != null);
        return (getGroupings().size() == 0 && hasNoAgg);
    }

    public boolean isRawWorkerData() {
        boolean hasNoAgg = getResultOutputColumnSelectors()
                .stream()
                .noneMatch(columnSelect -> columnSelect.getModifier() != null);
        return (getResultOutputGroupings().size() == 0 && hasNoAgg);
    }

    // Columns selected for grouping (Ordered by priority)
    public abstract List<TimeSeriesGroupSelector> getGroupings();

    // Columns selected with aggregation (or not)
    public abstract List<TimeSeriesColumnSelector> getColumnSelectors();

    // Filters of columns
    public abstract List<TimeSeriesFilter> getFilters();

    // Result Output Columns selected for grouping (Ordered by priority)
    public abstract List<TimeSeriesResultOutputGroupSelector> getResultOutputGroupings();

    // Result Output Columns selected with aggregation (or not)
    public abstract List<TimeSeriesResultOutputColumnSelector> getResultOutputColumnSelectors();

    // Filters of columns
    public abstract List<TimeSeriesResultOutputFilter> getResultOutputFilters();

    public static Builder builder() {
        return new Builder()
                .setColumnSelectors(Collections.emptyList())
                .setGroupings(Collections.emptyList())
                .setFilters(Collections.emptyList())
                .setResultOutputColumnSelectors(Collections.emptyList())
                .setResultOutputGroupings(Collections.emptyList())
                .setResultOutputFilters(Collections.emptyList());
    }

    public static class Builder extends EasyValue_TimeSeriesSelector.Builder {
        @Override
        public TimeSeriesSelector build() {
            // Check data
            if (
                    getColumnSelectors().size() <= 0
                    && getResultOutputColumnSelectors().size() <= 0
            ) {
                throw new IllegalArgumentException("No data source is selected.");
            }

            if (
                    getDataSeries() == null
                    && getSeriesResult() == null
                    && getSeriesResultOutput() == null
            ) {
                throw new IllegalArgumentException(
                        "DataSeries / SeriesResult / SeriesResultOutput is missing."
                );
            }
            else if (
                    getDataSeries() != null
                    && getSeriesResult() != null
                    && getSeriesResultOutput() != null
            ) {
                throw new IllegalArgumentException("Invalid selection.");
            }

            return super.build();
        }
    }
}
