package fi.jubic.quanta.models.configuration;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fi.jubic.easyvalue.EasyValue;
import fi.jubic.quanta.models.DataConnectionConfiguration;

@EasyValue
@JsonTypeName("CSV")
@JsonDeserialize(builder = CsvDataConnectionConfiguration.Builder.class)
public abstract class CsvDataConnectionConfiguration extends DataConnectionConfiguration {
    public abstract String getPath();

    public abstract String getInitialSyncFileName();

    public abstract Builder toBuilder();

    @Override
    public <T> T visit(FunctionVisitor<T> visitor) {
        return visitor.onCsv(this);
    }

    @Override
    public void visit(ConsumerVisitor visitor) {
        visitor.onCsv(this);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_CsvDataConnectionConfiguration.Builder {

    }
}
