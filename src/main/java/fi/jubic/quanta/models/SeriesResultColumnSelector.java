package fi.jubic.quanta.models;

import fi.jubic.easyvalue.EasyValue;

import javax.annotation.Nullable;

@EasyValue
public abstract class SeriesResultColumnSelector implements SeriesSelector {
    public abstract String getName();

    public abstract String getColumnName();

    public abstract Long getInvocationNumber();

    public abstract boolean getIsLatest();

    @Nullable
    public abstract String getAlias();

    public abstract Builder toBuilder();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_SeriesResultColumnSelector.Builder {
        @Override
        public Builder defaults(Builder builder) {
            return builder.setIsLatest(false);
        }

        @Override
        public SeriesResultColumnSelector build() {
            if (!getIsLatest() && getInvocationNumber() < 1) {
                throw new IllegalArgumentException(
                        "Selector is missing reference to Invocation"
                );
            }
            return super.build();
        }
    }

    @Override
    public String toString() {
        return String.format(
                "result:%s.%d.%s",
                getName(),
                getInvocationNumber(),
                getColumnName()
        );
    }


}
