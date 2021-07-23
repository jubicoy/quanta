package fi.jubic.quanta.models;

import fi.jubic.easyvalue.EasyValue;

import javax.annotation.Nullable;
import java.util.Objects;

@EasyValue
public abstract class AnomalyQuerySelector {
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

    public boolean getIsAggregate() {
        if (getModifier() == null) return false;
        return Objects.equals(
                getModifier(),
                TimeSeriesModifier.aggregation(getModifier().name())
        );
    }

    public boolean getIsDataSelector() {
        return !getIsGrouping()
                && !getIsFilter();
    }

    @Nullable
    public abstract String getAlias();

    @Nullable
    public abstract TimeSeriesModifier getModifier();

    public AnomalyResultSelector getSelector() {
        if (getAnomalyResultSelector() != null) {
            return getAnomalyResultSelector();
        }
        throw new IllegalStateException();
    }

    @Nullable
    public abstract AnomalyResultSelector getAnomalyResultSelector();

    public abstract Builder toBuilder();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_AnomalyQuerySelector.Builder {
        @Override
        public AnomalyQuerySelector build() {
            if (getAnomalyResultSelector() == null) {
                throw new IllegalArgumentException("Missing data selector");
            }

            return super.build();
        }
    }
}
