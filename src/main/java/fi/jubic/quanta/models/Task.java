package fi.jubic.quanta.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fi.jubic.easymapper.annotations.EasyId;
import fi.jubic.easyvalue.EasyValue;
import fi.jubic.quanta.db.tables.records.TaskRecord;
import fi.jubic.quanta.util.Json;

import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static fi.jubic.quanta.db.tables.Task.TASK;

@EasyValue
@JsonDeserialize(builder = Task.Builder.class)
public abstract class Task {
    @EasyId
    public abstract Long getId();

    public abstract String getName();

    @Nullable
    public abstract WorkerDef getWorkerDef();

    public abstract Map<String, Object> getConfig();

    public abstract List<ColumnSelector> getColumnSelectors();

    public abstract List<OutputColumn> getOutputColumns();

    @Nullable
    public abstract String getCronTrigger();

    @Nullable
    public abstract Long getTaskTrigger();

    public abstract TaskType getTaskType();

    @Nullable
    public abstract Instant getDeletedAt();

    @Nullable
    public abstract Map<String, Parameter> getAdditionalParams();

    public abstract Builder toBuilder();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_Task.Builder {
        @Override
        public Builder defaults(Builder builder) {
            return builder
                    .setWorkerDef(null)
                    .setConfig(Collections.emptyMap())
                    .setColumnSelectors(Collections.emptyList())
                    .setOutputColumns(Collections.emptyList())
                    .setAdditionalParams(Collections.emptyMap());
        }
    }

    public static final TaskRecordMapper<TaskRecord> mapper = TaskRecordMapper.builder(TASK)
            .setIdAccessor(TASK.ID)
            .setNameAccessor(TASK.NAME)
            .setWorkerDefAccessor(TASK.WORKER_DEF_ID, WorkerDef::getId)
            .setConfigAccessor(TASK.CONFIG, Json::writeConfig, Json::extractConfig)
            .setCronTriggerAccessor(TASK.CRON_TRIGGER)
            .setTaskTriggerAccessor(TASK.TASK_TRIGGER)
            .setTaskTypeAccessor(TASK.TASK_TYPE, TaskType::toString, TaskType::parse)
            .setDeletedAtAccessor(TASK.DELETED_AT, Timestamp::from, Timestamp::toInstant)
            .setAdditionalParamsAccessor(
                    TASK.ADDITIONAL_PARAMS,
                    Parameter::writeParameters,
                    Parameter::extractParameters
            )
            .build();
}