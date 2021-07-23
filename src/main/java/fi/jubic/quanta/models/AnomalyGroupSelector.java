package fi.jubic.quanta.models;

import fi.jubic.easyvalue.EasyValue;

import java.util.List;
import java.util.stream.Collectors;

@EasyValue
public abstract class AnomalyGroupSelector {

    public abstract String getColumnName();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_AnomalyGroupSelector.Builder {
    }

    public static List<AnomalyGroupSelector> parse(
            List<AnomalyQuerySelector> selectors
    ) {
        return selectors
                .stream()
                .filter(AnomalyQuerySelector::getIsGrouping)
                .map(selector -> AnomalyGroupSelector.builder()
                        .setColumnName(
                                selector.getSelector().getColumnName()
                        )
                        .build()
                )
                .collect(Collectors.toList());
    }
}
