package fi.jubic.quanta.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fi.jubic.easymapper.annotations.EasyId;
import fi.jubic.easyvalue.EasyValue;
import fi.jubic.quanta.db.tables.records.WorkerParameterRecord;

import javax.annotation.Nullable;

import static fi.jubic.quanta.db.tables.WorkerParameter.WORKER_PARAMETER;

@EasyValue
@JsonDeserialize(builder = WorkerParameter.Builder.class)
public abstract class WorkerParameter {
    @EasyId
    public abstract Long getId();

    public abstract String getName();

    public abstract String getDescription();

    public abstract String getType();

    public abstract boolean getNullable();

    @Nullable
    public abstract String getDefaultValue();

    public abstract WorkerParameter.Builder toBuilder();

    public static WorkerParameter.Builder builder() {
        return new WorkerParameter.Builder();
    }

    public static class Builder extends EasyValue_WorkerParameter.Builder {
        @Override
        public WorkerParameter.Builder defaults(WorkerParameter.Builder builder) {
            return builder.setId(-1L);
        }
    }

    public static final WorkerParameterRecordMapper<WorkerParameterRecord> mapper =
            WorkerParameterRecordMapper
                    .builder(WORKER_PARAMETER)
                    .setIdAccessor(WORKER_PARAMETER.ID)
                    .setNameAccessor(WORKER_PARAMETER.NAME)
                    .setDescriptionAccessor(WORKER_PARAMETER.DESCRIPTION)
                    .setTypeAccessor(WORKER_PARAMETER.TYPE)
                    .setDefaultValueAccessor(WORKER_PARAMETER.DEFAULT_VALUE)
                    .setNullableAccessor(WORKER_PARAMETER.NULLABLE)
                    .build();
}
