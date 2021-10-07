package fi.jubic.quanta.models;

import javax.annotation.Nullable;
import javax.ws.rs.QueryParam;
import java.util.Optional;

public class WorkerDefQuery {
    @QueryParam("nameLike")
    @Nullable
    private String nameLike;

    @QueryParam("workerType")
    @Nullable
    private String workerType;

    @QueryParam("notDeleted")
    @Nullable
    private Boolean notDeleted;

    public WorkerDefQuery() {

    }

    private WorkerDefQuery(
            @Nullable String nameLike,
            @Nullable String workerType,
            @Nullable Boolean notDeleted
    ) {
        this.nameLike = nameLike;
        this.workerType = workerType;
        this.notDeleted = notDeleted;
    }

    public WorkerDefQuery withNameLike(String nameLike) {
        return new WorkerDefQuery(
                nameLike,
                this.workerType,
                this.notDeleted
        );
    }

    public WorkerDefQuery withWorkerType(String workerType) {
        return new WorkerDefQuery(
                this.nameLike,
                workerType,
                this.notDeleted
        );
    }

    public WorkerDefQuery withNotDeleted(Boolean notDeleted) {
        return new WorkerDefQuery(
                this.nameLike,
                this.workerType,
                notDeleted
        );
    }

    public Optional<String> getNameLike() {
        return Optional.ofNullable(nameLike);
    }

    public Optional<Boolean> getNotDeleted() {
        return Optional.ofNullable(notDeleted);
    }

    public Optional<String> getWorkerType() {
        return Optional.ofNullable(workerType);
    }
}
