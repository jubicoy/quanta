package fi.jubic.quanta.models;

import fi.jubic.easyvalue.EasyValue;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@EasyValue
public abstract class AnomalyAggregationSelector {
    public abstract String getColumnName();

    public abstract TimeSeriesModifier getModifier();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_AnomalyAggregationSelector.Builder {
    }

    public static List<AnomalyAggregationSelector> parse(
            List<AnomalyQuerySelector> selectors
    ) {
        return selectors
                .stream()
                .filter(AnomalyQuerySelector::getIsAggregate)
                .map(
                        selector -> AnomalyAggregationSelector.builder()
                                .setColumnName(
                                        Objects.requireNonNull(
                                                selector.getAnomalyResultSelector()
                                        ).getColumnName()
                                )
                                .setModifier(selector.getModifier())
                                .build()
                )
                .collect(Collectors.toList());
    }
}
