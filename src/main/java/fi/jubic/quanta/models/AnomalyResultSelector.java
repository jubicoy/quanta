package fi.jubic.quanta.models;

import fi.jubic.easyvalue.EasyValue;

import javax.ws.rs.NotFoundException;
import java.util.List;

@EasyValue
public abstract class AnomalyResultSelector {
    public abstract String getTaskName();

    public abstract String getColumnName();

    public abstract Long getInvocationNumber();

    public abstract boolean getIsLatest();

    public abstract Builder toBuilder();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_AnomalyResultSelector.Builder {
        @Override
        public Builder defaults(Builder builder) {
            return builder
                    .setIsLatest(false)
                    .setColumnName("*");
        }

        @Override
        public AnomalyResultSelector build() {
            var result = super.build();
            if (!result.getIsLatest() && result.getInvocationNumber() < 1) {
                throw new IllegalArgumentException(
                        "Selector is missing reference to Invocation"
                );
            }
            return result;
        }
    }

    public static AnomalyResultSelector getAnomalyResult(
            List<AnomalyQuerySelector> selectors
    ) {
        return selectors.stream()
                .filter(AnomalyQuerySelector::getIsDataSelector)
                .findFirst()
                .orElseThrow(NotFoundException::new)
                .getAnomalyResultSelector();
    }
}
