package fi.jubic.quanta.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fi.jubic.easyvalue.EasyValue;

import javax.annotation.Nullable;
import java.util.List;

@EasyValue
@JsonDeserialize(builder = ImportWorkerDataSample.Builder.class)
public abstract class ImportWorkerDataSample {
    public abstract List<WorkerDefColumn> getColumns();

    public abstract List<List<String>> getData();

    public abstract boolean getErrorFlag();

    @Nullable
    public abstract String getMessage();

    public abstract Builder toBuilder();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_ImportWorkerDataSample.Builder {

    }
}
