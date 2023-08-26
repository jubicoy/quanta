package fi.jubic.quanta.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fi.jubic.easymapper.annotations.EasyId;
import fi.jubic.easymapper.jooq.JooqFieldAccessor;
import fi.jubic.easyvalue.EasyValue;
import fi.jubic.quanta.db.tables.records.TaskRecord;
import fi.jubic.quanta.util.DateUtil;
import fi.jubic.quanta.util.StringUtil;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static fi.jubic.quanta.db.tables.Task.TASK;

@EasyValue
@JsonDeserialize(builder = Task.Builder.class)
public abstract class Task {
    @EasyId
    public abstract Long getId();

    public abstract String getName();

    @Nullable
    public abstract WorkerDef getWorkerDef();

    public abstract List<ColumnSelector> getColumnSelectors();

    public abstract List<OutputColumn> getOutputColumns();

    @Nullable
    public abstract String getCronTrigger();

    @Nullable
    public abstract Long getTaskTrigger();

    public abstract TaskType getTaskType();

    @Nullable
    public abstract DataSeries getSeries();

    @Nullable
    public abstract Long getSyncIntervalOffset();

    @Nullable
    public abstract Instant getDeletedAt();

    @Nullable
    public abstract List<Parameter> getParameters();

    @Nullable
    public abstract Set<String> getTags();

    public abstract Builder toBuilder();

    public static Optional<Task> syncTask(DataSeries dataSeries) {
        if (dataSeries.getType().equals(DataConnectionType.JSON_INGEST)) {
            return Optional.empty();
        }

        String taskName = String.format(
                "sync-%s-%s",
                dataSeries.getName(),
                StringUtil.alphaNumericIdentifier(8)
        );

        if (dataSeries.getType().equals(DataConnectionType.IMPORT_WORKER)) {
            return Optional.of(
                    builder()
                            .setId(-1L)
                            .setName(taskName)
                            .setSeries(dataSeries)
                            .setWorkerDef(
                                    WorkerDef.ref(
                                            Objects.requireNonNull(dataSeries.getDataConnection())
                                                    .getImportWorkerConfiguration()
                                                    .getWorkerDefId()
                                    )
                            )
                            .setTaskType(TaskType.IMPORT)
                            .build()
            );
        }
        return Optional.of(
                builder()
                        .setId(-1L)
                        .setName(taskName)
                        .setTaskType(TaskType.sync)
                        .setSeries(dataSeries)
                        .build()
        );
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_Task.Builder {
        @Override
        public Builder defaults(Builder builder) {
            return builder
                    .setWorkerDef(null)
                    .setColumnSelectors(Collections.emptyList())
                    .setSeries(null)
                    .setOutputColumns(Collections.emptyList())
                    .setTags(Collections.emptySet());
        }
    }

    public static final TaskRecordMapper<TaskRecord> mapper = TaskRecordMapper.builder(TASK)
            .setIdAccessor(TASK.ID)
            .setNameAccessor(TASK.NAME)
            .setWorkerDefAccessor(TASK.WORKER_DEF_ID, WorkerDef::getId)
            .setCronTriggerAccessor(TASK.CRON_TRIGGER)
            .setTaskTriggerAccessor(TASK.TASK_TRIGGER)
            .setTaskTypeAccessor(TASK.TASK_TYPE, TaskType::toString, TaskType::parse)
            .setSyncIntervalOffsetAccessor(TASK.SYNC_INTERVAL_OFFSET)
            .setSeriesAccessor(
                    TASK.DATA_SERIES_ID,
                    DataSeries::getId
            )
            .setDeletedAtAccessor(
                    TASK.DELETED_AT,
                    DateUtil::toLocalDateTime,
                    DateUtil::toInstant
            )
            .setTagsAccessor(new JooqFieldAccessor.NoOpAccessor<>())
            .build();
}
