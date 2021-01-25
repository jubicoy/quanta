package fi.jubic.quanta.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fi.jubic.easymapper.annotations.EasyId;
import fi.jubic.easyvalue.EasyValue;
import fi.jubic.quanta.db.tables.records.WorkerDefinitionRecord;

import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static fi.jubic.quanta.db.tables.WorkerDefinition.WORKER_DEFINITION;

@EasyValue
@JsonDeserialize(builder = WorkerDef.Builder.class)
public abstract class WorkerDef {
    @EasyId
    public abstract Long getId();

    public abstract WorkerType getType();

    public abstract String getName();

    public abstract String getDescription();

    @Nullable
    public abstract List<WorkerParameter> getParameters();

    public abstract List<WorkerDefColumn> getColumns();

    @Nullable
    public abstract Instant getDeletedAt();

    public abstract Builder toBuilder();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_WorkerDef.Builder {
        @Override
        public Builder defaults(Builder builder) {
            return builder
                    .setParameters(Collections.emptyList())
                    .setColumns(Collections.emptyList());
        }
    }

    public static final WorkerDefRecordMapper<WorkerDefinitionRecord> mapper
            = WorkerDefRecordMapper.builder(WORKER_DEFINITION)
            .setIdAccessor(WORKER_DEFINITION.ID)
            .setTypeAccessor(WORKER_DEFINITION.TYPE, WorkerType::name, WorkerType::valueOf)
            .setNameAccessor(WORKER_DEFINITION.NAME)
            .setDescriptionAccessor(WORKER_DEFINITION.DESCRIPTION)
            .setDeletedAtAccessor(
                    WORKER_DEFINITION.DELETED_AT,
                    Timestamp::from,
                    Timestamp::toInstant
            )
            .build();
}
