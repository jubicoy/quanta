package fi.jubic.quanta.models;

import jakarta.ws.rs.QueryParam;

import javax.annotation.Nullable;
import java.util.Optional;

public class WorkerQuery {
    @QueryParam("workerDef")
    @Nullable
    private Long workerDefId;

    @QueryParam("status")
    @Nullable
    private WorkerStatus status;

    @QueryParam("notDeleted")
    @Nullable
    private Boolean notDeleted;

    public WorkerQuery() {

    }

    private WorkerQuery(
            @Nullable Long workerDefId,
            @Nullable WorkerStatus status,
            @Nullable Boolean notDeleted
    ) {
        this.workerDefId = workerDefId;
        this.status = status;
        this.notDeleted = notDeleted;
    }

    public WorkerQuery withWorkerDefId(Long workerDefId) {
        return new WorkerQuery(
                workerDefId,
                this.status,
                this.notDeleted
        );
    }

    public WorkerQuery withStatus(WorkerStatus status) {
        return new WorkerQuery(
                this.workerDefId,
                status,
                this.notDeleted
        );
    }

    public WorkerQuery withNotDeleted(Boolean notDeleted) {
        return new WorkerQuery(
                this.workerDefId,
                this.status,
                notDeleted
        );
    }

    public Optional<Long> getWorkerDefId() {
        return Optional.ofNullable(workerDefId);
    }

    public Optional<WorkerStatus> getStatus() {
        return Optional.ofNullable(status);
    }

    public Optional<Boolean> getNotDeleted() {
        return Optional.ofNullable(notDeleted);
    }
}
