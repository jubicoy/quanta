package fi.jubic.quanta.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fi.jubic.easymapper.annotations.EasyId;
import fi.jubic.easyvalue.EasyValue;
import fi.jubic.quanta.db.tables.records.WorkerDefinitionRecord;
import fi.jubic.quanta.util.DateUtil;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

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

    public static WorkerDef ref(Long workerDefId) {
        return builder()
                .setId(workerDefId)
                .setType(WorkerType.Sync)
                .setName("")
                .setDescription("")
                .build();
    }

    public static class Builder extends EasyValue_WorkerDef.Builder {
        @Override
        public Builder defaults(Builder builder) {
            return builder
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
                    DateUtil::toLocalDateTime,
                    DateUtil::toInstant
            )
            .build();

    public boolean isEqual(WorkerDef def) {
        boolean isColumnsEqual = IntStream.range(0, def.getColumns().size())
                .allMatch(i -> def.getColumns().get(i).isEqual(
                        getColumns().get(i)
                ));

        boolean isParameterEqual = getParameters() == null && def.getParameters() == null;

        if (getParameters() != null && def.getParameters() != null) {
            if (getParameters().size() != def.getParameters().size()) {
                isParameterEqual = false;
            }
            else {
                isParameterEqual = IntStream.range(0, def.getParameters().size())
                        .allMatch(i -> def.getParameters().get(i).isEqual(
                                getParameters().get(i)
                        ));
            }
        }

        return Objects.deepEquals(getType(), def.getType())
                && Objects.equals(getName(), def.getName())
                && Objects.equals(getDescription(), def.getDescription())
                && isColumnsEqual
                && isParameterEqual;
    }
}
