package fi.jubic.quanta.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fi.jubic.easymapper.annotations.EasyId;
import fi.jubic.easyvalue.EasyValue;
import fi.jubic.quanta.db.tables.records.InvocationParameterRecord;
import fi.jubic.quanta.db.tables.records.TaskParameterRecord;

import static fi.jubic.quanta.db.tables.InvocationParameter.INVOCATION_PARAMETER;
import static fi.jubic.quanta.db.tables.TaskParameter.TASK_PARAMETER;

@EasyValue
@JsonDeserialize(builder = Parameter.Builder.class)
public abstract class Parameter {
    @EasyId
    public abstract Long getId();

    public abstract String getName();

    public abstract String getValue();

    public abstract Builder toBuilder();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_Parameter.Builder {
        @Override
        public Parameter.Builder defaults(Parameter.Builder builder) {
            return builder.setId(-1L);
        }
    }

    public static final ParameterRecordMapper<TaskParameterRecord> taskParameterRecordMapper =
            ParameterRecordMapper
                    .builder(TASK_PARAMETER)
                    .setIdAccessor(TASK_PARAMETER.ID)
                    .setNameAccessor(TASK_PARAMETER.NAME)
                    .setValueAccessor(TASK_PARAMETER.VALUE)
                    .build();

    public static final ParameterRecordMapper<InvocationParameterRecord>
            invocationParameterRecordMapper =
            ParameterRecordMapper
                    .builder(INVOCATION_PARAMETER)
                    .setIdAccessor(INVOCATION_PARAMETER.ID)
                    .setNameAccessor(INVOCATION_PARAMETER.NAME)
                    .setValueAccessor(INVOCATION_PARAMETER.VALUE)
                    .build();
}
