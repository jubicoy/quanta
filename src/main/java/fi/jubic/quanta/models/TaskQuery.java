package fi.jubic.quanta.models;

import javax.annotation.Nullable;
import javax.ws.rs.QueryParam;
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
            @Nullable Boolean notDeleted
    ) {
        this.connectionId = connectionId;
        this.workerDefId = workerDefId;
        this.triggeredById = triggeredById;
        this.hasCronTrigger = hasCronTrigger;
        this.dataSeriesId = dataSeriesId;
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
                notDeleted
        );
    }
}
