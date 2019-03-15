package fi.jubic.quanta.models.configuration;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fi.jubic.easyvalue.EasyValue;
import fi.jubic.quanta.models.DataConnectionConfiguration;

@EasyValue
@JsonTypeName("JSON_INGEST")
@JsonDeserialize(builder = JsonIngestDataConnectionConfiguration.Builder.class)
public abstract class JsonIngestDataConnectionConfiguration extends DataConnectionConfiguration {
    public abstract String getToken();

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

    public static class Builder extends EasyValue_JsonIngestDataConnectionConfiguration.Builder {

    }
}
