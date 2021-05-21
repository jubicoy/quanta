package fi.jubic.quanta.models.configuration;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fi.jubic.easyvalue.EasyValue;
import fi.jubic.quanta.models.DataConnectionConfiguration;

@EasyValue
@JsonTypeName("IMPORT_WORKER")
@JsonDeserialize(builder = ImportWorkerDataConnectionConfiguration.Builder.class)
public abstract class ImportWorkerDataConnectionConfiguration extends DataConnectionConfiguration {

    public abstract Long getWorkerDefId();

    public abstract Builder toBuilder();

    @Override
    public <T> T visit(FunctionVisitor<T> visitor) {
        return visitor.onImportWorker(this);
    }

    @Override
    public void visit(ConsumerVisitor visitor) {
        visitor.onImportWorker(this);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_ImportWorkerDataConnectionConfiguration.Builder {

    }
}
