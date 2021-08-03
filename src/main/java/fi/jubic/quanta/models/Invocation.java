package fi.jubic.quanta.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fi.jubic.easymapper.annotations.EasyId;
import fi.jubic.easyvalue.EasyValue;
import fi.jubic.quanta.db.tables.records.InvocationRecord;
import fi.jubic.quanta.util.DateUtil;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static fi.jubic.quanta.db.tables.Invocation.INVOCATION;

@EasyValue
@JsonDeserialize(builder = Invocation.Builder.class)
public abstract class Invocation {
    @EasyId
    public abstract Long getId();

    public abstract Long getInvocationNumber();

    public abstract InvocationStatus getStatus();

    public abstract Task getTask();

    @Nullable
    public abstract Worker getWorker();

    @Nullable
    public abstract Instant getStartTime();

    @Nullable
    public abstract Instant getEndTime();

    public abstract List<ColumnSelector> getColumnSelectors();

    public abstract List<OutputColumn> getOutputColumns();

    public abstract List<SeriesResult> getSeriesResults();

    public abstract List<Anomaly> getDetectionResults();

    @Nullable
    public abstract List<Parameter> getParameters();

    @Nullable
    public abstract Instant getDeletedAt();

    public abstract Builder toBuilder();

    public static Builder builder() {
        return new Builder();
    }

    public static Invocation invoke(Task task) {
        return Invocation.builder()
                .setId(0L)
                .setInvocationNumber(0L)
                .setTask(task)
                .setWorker(null)
                .setColumnSelectors(task.getColumnSelectors())
                .setOutputColumns(task.getOutputColumns())
                .setParameters(task.getParameters())
                .setStatus(InvocationStatus.Pending)
                .setStartTime(null)
                .setEndTime(null)
                .setDeletedAt(null)
                .build();
    }

    public static class Builder extends EasyValue_Invocation.Builder {
        @Override
        public Builder defaults(Builder builder) {
            return builder
                    .setWorker(null)
                    .setColumnSelectors(Collections.emptyList())
                    .setOutputColumns(Collections.emptyList())
                    .setSeriesResults(Collections.emptyList())
                    .setDetectionResults(Collections.emptyList());
        }
    }

    public static final InvocationRecordMapper<InvocationRecord> mapper
            = InvocationRecordMapper.builder(INVOCATION)
            .setIdAccessor(INVOCATION.ID)
            .setInvocationNumberAccessor(INVOCATION.INVOCATION_NUMBER)
            .setStatusAccessor(INVOCATION.STATUS, InvocationStatus::name, InvocationStatus::valueOf)
            .setTaskAccessor(INVOCATION.TASK_ID, Task::getId)
            .setWorkerAccessor(INVOCATION.WORKER_ID, Worker::getId)
            .setStartTimeAccessor(
                    INVOCATION.STARTING_TIME,
                    DateUtil::toLocalDateTime,
                    DateUtil::toInstant
            )
            .setEndTimeAccessor(
                    INVOCATION.ENDING_TIME,
                    DateUtil::toLocalDateTime,
                    DateUtil::toInstant
            )
            .setDeletedAtAccessor(
                    INVOCATION.DELETED_AT,
                    DateUtil::toLocalDateTime,
                    DateUtil::toInstant
            )
            .build();
}
