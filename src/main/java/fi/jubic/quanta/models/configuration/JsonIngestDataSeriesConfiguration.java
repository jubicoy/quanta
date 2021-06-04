package fi.jubic.quanta.models.configuration;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fi.jubic.easyvalue.EasyValue;
import fi.jubic.quanta.models.DataSeriesConfiguration;

import javax.annotation.Nullable;
import java.util.List;

@EasyValue
@JsonTypeName("JSON_INGEST")
@JsonDeserialize(builder = JsonIngestDataSeriesConfiguration.Builder.class)
public abstract class JsonIngestDataSeriesConfiguration extends DataSeriesConfiguration {
    @Nullable
    public abstract Object getSampleJsonDocument();

    public abstract boolean getIsCollections();

    public abstract List<String> getPaths();

    public abstract Builder toBuilder();

    @Override
    public <T> T visit(FunctionVisitor<T> visitor) {
        return visitor.onJson(this);
    }

    @Override
    public void visit(ConsumerVisitor visitor) {
        visitor.onJson(this);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_JsonIngestDataSeriesConfiguration.Builder {
        @Override
        public Builder defaults(Builder builder) {
            return builder
                    .setIsCollections(false);
        }

        @Override
        public JsonIngestDataSeriesConfiguration build() {
            if (getPaths().size() <= 0) {
                throw new IllegalArgumentException("JSON paths are missing.");
            }
            return super.build();
        }
    }
}
