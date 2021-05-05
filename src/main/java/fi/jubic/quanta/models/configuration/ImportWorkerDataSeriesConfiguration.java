package fi.jubic.quanta.models.configuration;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fi.jubic.easyvalue.EasyValue;
import fi.jubic.quanta.models.DataSeriesConfiguration;
import fi.jubic.quanta.models.Parameter;

import javax.annotation.Nullable;
import java.util.List;

@EasyValue
@JsonTypeName("IMPORT_WORKER")
@JsonDeserialize(builder = ImportWorkerDataSeriesConfiguration.Builder.class)
public abstract class ImportWorkerDataSeriesConfiguration extends DataSeriesConfiguration {

    public abstract List<Parameter> getParameters();

    public abstract Builder toBuilder();

    @Override
    public <T> T visit(FunctionVisitor<T> visitor) {
        return visitor.onImportWorker(this);
    }

    @Override
    public void visit(ConsumerVisitor visitor) {
        visitor.onImportWorker(this);
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder extends EasyValue_ImportWorkerDataSeriesConfiguration.Builder {
    }
}

