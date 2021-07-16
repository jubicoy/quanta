package fi.jubic.quanta.models;

import fi.jubic.easyvalue.EasyValue;

@EasyValue
public abstract class SeriesResultSelector {
    public abstract String getTaskName();

    public abstract Long getInvocationNumber();

    public abstract boolean getIsLatest();

    public abstract Builder toBuilder();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_SeriesResultSelector.Builder {
        @Override
        public Builder defaults(Builder builder) {
            return builder.setIsLatest(false);
        }

        @Override
        public SeriesResultSelector build() {
            if (!getIsLatest() && getInvocationNumber() < 1) {
                throw new IllegalArgumentException(
                        "Selector is missing reference to Invocation"
                );
            }
            return super.build();
        }
    }
}
