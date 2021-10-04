package fi.jubic.quanta.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fi.jubic.easymapper.annotations.EasyId;
import fi.jubic.easyvalue.EasyValue;
import fi.jubic.quanta.db.tables.records.WorkerDefinitionColumnRecord;

import javax.annotation.Nullable;

import java.util.Objects;

import static fi.jubic.quanta.db.tables.WorkerDefinitionColumn.WORKER_DEFINITION_COLUMN;

@EasyValue
@JsonDeserialize(builder = WorkerDefColumn.Builder.class)
public abstract class WorkerDefColumn {
    @EasyId
    public abstract Long getId();

    public abstract String getName();

    @Nullable
    public abstract Type getValueType();

    public abstract String getDescription();

    public abstract WorkerDefColumnType getColumnType();

    public abstract String getSeriesKey();

    public abstract Integer getIndex();

    public abstract Builder toBuilder();

    public static Builder builder() {
        Class<?> className = null;
        try {
            className = Class.forName("java.lang.String");
        }
        catch (ClassNotFoundException exception) {
            exception.printStackTrace();
        }
        return new Builder()
                .setId(-1L)
                .setName("")
                .setValueType(
                        Type.builder()
                                .setClassName(className)
                                .setFormat(null)
                                .setNullable(false)
                                .build()
                )
                .setColumnType(WorkerDefColumnType.input)
                .setIndex(0)
                .setDescription("");
    }

    public static class Builder extends EasyValue_WorkerDefColumn.Builder {
        @Override
        public Builder defaults(Builder builder) {
            return builder
                    .setId(-1L)
                    .setSeriesKey("");
        }
    }

    public static final WorkerDefColumnRecordMapper<WorkerDefinitionColumnRecord>
            workerDefColumnMapper = WorkerDefColumnRecordMapper
            .builder(WORKER_DEFINITION_COLUMN)
            .setIdAccessor(WORKER_DEFINITION_COLUMN.ID)
            .setNameAccessor(WORKER_DEFINITION_COLUMN.NAME)
            .setValueTypeAccessor(new Type.TypeAccessor<>(
                    WORKER_DEFINITION_COLUMN.CLASS,
                    WORKER_DEFINITION_COLUMN.FORMAT,
                    WORKER_DEFINITION_COLUMN.NULLABLE
            ))
            .setColumnTypeAccessor(
                    WORKER_DEFINITION_COLUMN.COLUMN_TYPE,
                    WorkerDefColumnType::toString,
                    WorkerDefColumnType::parse
            )
            .setSeriesKeyAccessor(WORKER_DEFINITION_COLUMN.SERIES_KEY)
            .setIndexAccessor(WORKER_DEFINITION_COLUMN.INDEX)
            .setDescriptionAccessor(WORKER_DEFINITION_COLUMN.DESCRIPTION)
            .build();

    public boolean isEqual(WorkerDefColumn defCol) {
        return Objects.equals(getName(), defCol.getName())
                && Objects.equals(getDescription(), defCol.getDescription())
                && Objects.deepEquals(getValueType(), defCol.getValueType())
                && Objects.deepEquals(getColumnType(), defCol.getColumnType())
                && Objects.equals(getIndex(), defCol.getIndex());
    }
}
