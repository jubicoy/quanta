package fi.jubic.quanta.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fi.jubic.easymapper.annotations.EasyId;
import fi.jubic.easyvalue.EasyValue;
import fi.jubic.quanta.db.tables.records.InvocationOutputColumnRecord;
import fi.jubic.quanta.db.tables.records.TaskOutputColumnRecord;

import javax.annotation.Nullable;

import static fi.jubic.quanta.db.tables.InvocationOutputColumn.INVOCATION_OUTPUT_COLUMN;
import static fi.jubic.quanta.db.tables.TaskOutputColumn.TASK_OUTPUT_COLUMN;

@EasyValue
@JsonDeserialize(builder = OutputColumn.Builder.class)
public abstract class OutputColumn {
    @EasyId
    public abstract Long getId();

    public abstract Integer getIndex();

    @Nullable
    public abstract String getAlias();

    public abstract String getColumnName();

    public abstract Type getType();

    public abstract Builder toBuilder();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_OutputColumn.Builder {
        @Override
        public Builder defaults(Builder builder) {
            Class<?> className = null;
            try {
                className = Class.forName("java.lang.String");
            }
            catch (ClassNotFoundException exception) {
                exception.printStackTrace();
            }

            return new Builder()
                    .setId(-1L)
                    .setIndex(0)
                    .setAlias("")
                    .setColumnName("")
                    .setType(Type
                            .builder()
                            .setClassName(className)
                            .setFormat(null)
                            .setNullable(false)
                            .build()
                    );
        }
    }

    public static final OutputColumnRecordMapper<TaskOutputColumnRecord>
            taskOutputColumnMapper = OutputColumnRecordMapper.builder(TASK_OUTPUT_COLUMN)
            .setIdAccessor(TASK_OUTPUT_COLUMN.ID)
            .setIndexAccessor(TASK_OUTPUT_COLUMN.INDEX)
            .setAliasAccessor(TASK_OUTPUT_COLUMN.ALIAS)
            .setColumnNameAccessor(TASK_OUTPUT_COLUMN.COLUMN_NAME)
            .setTypeAccessor(new Type.TypeAccessor<>(
                    TASK_OUTPUT_COLUMN.CLASS,
                    TASK_OUTPUT_COLUMN.FORMAT,
                    TASK_OUTPUT_COLUMN.NULLABLE
            ))
            .build();

    public static final OutputColumnRecordMapper<InvocationOutputColumnRecord>
            invocationOutputColumnMapper = OutputColumnRecordMapper
            .builder(INVOCATION_OUTPUT_COLUMN)
            .setIdAccessor(INVOCATION_OUTPUT_COLUMN.ID)
            .setIndexAccessor(INVOCATION_OUTPUT_COLUMN.INDEX)
            .setAliasAccessor(INVOCATION_OUTPUT_COLUMN.ALIAS)
            .setColumnNameAccessor(INVOCATION_OUTPUT_COLUMN.COLUMN_NAME)
            .setTypeAccessor(new Type.TypeAccessor<>(
                    INVOCATION_OUTPUT_COLUMN.CLASS,
                    INVOCATION_OUTPUT_COLUMN.FORMAT,
                    INVOCATION_OUTPUT_COLUMN.NULLABLE
            ))
            .build();
}
