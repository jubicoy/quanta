package fi.jubic.quanta.models;

import jakarta.ws.rs.QueryParam;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Optional;

public class TaskQuery {
    @QueryParam("connection")
    @Nullable
    private Long connectionId;

    @QueryParam("workerDef")
    @Nullable
    private Long workerDefId;

    @QueryParam("triggeredBy")
    @Nullable
    private Long triggeredById;

    @QueryParam("hasCronTrigger")
    @Nullable
    private Boolean hasCronTrigger;

    @QueryParam("dataSeriesId")
    @Nullable
    private Long dataSeriesId;

    @QueryParam("syncIntervalStartTime")
    @Nullable
    private Instant syncIntervalStartTime;

    @QueryParam("syncIntervalEndTime")
    @Nullable
    private Instant syncIntervalEndTime;

    @QueryParam("notDeleted")
    @Nullable
    private Boolean notDeleted;

    public TaskQuery() {

    }

    private TaskQuery(
            @Nullable Long connectionId,
            @Nullable Long workerDefId,
            @Nullable Long triggeredById,
            @Nullable Boolean hasCronTrigger,
            @Nullable Long dataSeriesId,
            @Nullable Instant syncIntervalStartTime,
            @Nullable Instant syncIntervalEndTime,
            @Nullable Boolean notDeleted
    ) {
        this.connectionId = connectionId;
        this.workerDefId = workerDefId;
        this.triggeredById = triggeredById;
        this.hasCronTrigger = hasCronTrigger;
        this.dataSeriesId = dataSeriesId;
        this.syncIntervalStartTime = syncIntervalStartTime;
        this.syncIntervalEndTime = syncIntervalEndTime;
        this.notDeleted = notDeleted;
    }

    public Optional<Long> getConnectionId() {
        return Optional.ofNullable(connectionId);
    }

    public Optional<Long> getWorkerDefId() {
        return Optional.ofNullable(workerDefId);
    }

    public Optional<Long> getTriggeredById() {
        return Optional.ofNullable(triggeredById);
    }

    public Optional<Boolean> getHasCronTrigger() {
        return Optional.ofNullable(hasCronTrigger);
    }

    public Optional<Long> getDataSeriesId() {
        return Optional.ofNullable(dataSeriesId);
    }

    public Optional<Instant> getSyncIntervalStartTime() {
        return Optional.ofNullable(syncIntervalStartTime);
    }

    public Optional<Instant> getSyncIntervalEndTime() {
        return Optional.ofNullable(syncIntervalEndTime);
    }

    public Optional<Boolean> getNotDeleted() {
        return Optional.ofNullable(notDeleted);
    }

    public TaskQuery withConnectionId(Long connectionId) {
        return new TaskQuery(
                connectionId,
                this.workerDefId,
                this.triggeredById,
                this.hasCronTrigger,
                this.dataSeriesId,
                this.syncIntervalStartTime,
                this.syncIntervalEndTime,
                this.notDeleted
        );
    }

    public TaskQuery withWorkerDefId(Long workerDefId) {
        return new TaskQuery(
                this.connectionId,
                workerDefId,
                this.triggeredById,
                this.hasCronTrigger,
                this.dataSeriesId,
                this.syncIntervalStartTime,
                this.syncIntervalEndTime,
                this.notDeleted
        );
    }

    public TaskQuery withTriggeredById(Long triggeredById) {
        return new TaskQuery(
                this.connectionId,
                this.workerDefId,
                triggeredById,
                this.hasCronTrigger,
                this.dataSeriesId,
                this.syncIntervalStartTime,
                this.syncIntervalEndTime,
                this.notDeleted
        );
    }

    public TaskQuery withHasCronTrigger(Boolean hasCronTrigger) {
        return new TaskQuery(
                this.connectionId,
                this.workerDefId,
                this.triggeredById,
                hasCronTrigger,
                this.dataSeriesId,
                this.syncIntervalStartTime,
                this.syncIntervalEndTime,
                this.notDeleted
        );
    }

    public TaskQuery withDataSeriesId(Long dataSeriesId) {
        return new TaskQuery(
                this.connectionId,
                this.workerDefId,
                this.triggeredById,
                this.hasCronTrigger,
                dataSeriesId,
                this.syncIntervalStartTime,
                this.syncIntervalEndTime,
                this.notDeleted
        );
    }

    public TaskQuery withSyncIntervalStartTime(Instant syncIntervalStartTime) {
        return new TaskQuery(
                this.connectionId,
                this.workerDefId,
                this.triggeredById,
                this.hasCronTrigger,
                this.dataSeriesId,
                syncIntervalStartTime,
                this.syncIntervalEndTime,
                this.notDeleted
        );
    }

    public TaskQuery withSyncIntervalEndTime(Instant syncIntervalEndTime) {
        return new TaskQuery(
                this.connectionId,
                this.workerDefId,
                this.triggeredById,
                this.hasCronTrigger,
                this.dataSeriesId,
                this.syncIntervalStartTime,
                syncIntervalEndTime,
                this.notDeleted
        );
    }


    public TaskQuery withNotDeleted(Boolean notDeleted) {
        return new TaskQuery(
                this.connectionId,
                this.workerDefId,
                this.triggeredById,
                this.hasCronTrigger,
                this.dataSeriesId,
                this.syncIntervalStartTime,
                this.syncIntervalEndTime,
                notDeleted
        );
    }
}
