package fi.jubic.quanta.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fi.jubic.easymapper.annotations.EasyId;
import fi.jubic.easyvalue.EasyValue;
import fi.jubic.quanta.db.tables.records.WorkerDefinitionColumnRecord;

import javax.annotation.Nullable;

import static fi.jubic.quanta.db.tables.WorkerDefinitionColumn.WORKER_DEFINITION_COLUMN;

@EasyValue
@JsonDeserialize(builder = WorkerDefColumn.Builder.class)
public abstract class WorkerDefColumn {
    @EasyId
    public abstract Long getId();

    public abstract String getName();

    @Nullable
    public abstract Type getType();

    public abstract String getDescription();

    public abstract WorkerDefColumnType getColumnType();

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
                .setType(
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
    }

    public static final WorkerDefColumnRecordMapper<WorkerDefinitionColumnRecord>
            workerDefColumnMapper = WorkerDefColumnRecordMapper
            .builder(WORKER_DEFINITION_COLUMN)
            .setIdAccessor(WORKER_DEFINITION_COLUMN.ID)
            .setNameAccessor(WORKER_DEFINITION_COLUMN.NAME)
            .setTypeAccessor(new Type.TypeAccessor<>(
                    WORKER_DEFINITION_COLUMN.CLASS,
                    WORKER_DEFINITION_COLUMN.FORMAT,
                    WORKER_DEFINITION_COLUMN.NULLABLE
            ))
            .setColumnTypeAccessor(
                    WORKER_DEFINITION_COLUMN.COLUMN_TYPE,
                    WorkerDefColumnType::toString,
                    WorkerDefColumnType::parse
            )
            .setIndexAccessor(WORKER_DEFINITION_COLUMN.INDEX)
            .setDescriptionAccessor(WORKER_DEFINITION_COLUMN.DESCRIPTION)
            .build();
}
