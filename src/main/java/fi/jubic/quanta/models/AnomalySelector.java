package fi.jubic.quanta.models;

import fi.jubic.easyvalue.EasyValue;

import java.util.Collections;
import java.util.List;

@EasyValue
public abstract class AnomalySelector {

    public abstract AnomalyResultSelector getAnomalyResultSelector();

    // Columns selected for grouping (Ordered by priority)
    public abstract List<AnomalyGroupSelector> getGroupings();

    //  Selected for aggregations
    public abstract List<AnomalyAggregationSelector> getAggregations();

    // Filters of columns
    public abstract List<AnomalyFilter> getFilters();

    public static Builder builder() {
        return new Builder()
                .setGroupings(Collections.emptyList())
                .setFilters(Collections.emptyList())
                .setAggregations(Collections.emptyList());
    }

    public static class Builder extends EasyValue_AnomalySelector.Builder {
    }

    public static AnomalySelector parse(
            List<AnomalyQuerySelector> selectors
    ) {
        return AnomalySelector.builder()
                .setAnomalyResultSelector(
                        AnomalyResultSelector.getAnomalyResult(selectors)
                )
                .setFilters(
                        AnomalyFilter.parse(selectors)
                )
                .setGroupings(
                        AnomalyGroupSelector.parse(selectors)
                )
                .setAggregations(
                        AnomalyAggregationSelector.parse(selectors)
                )
                .build();
    }
}
