package fi.jubic.quanta.models;

import javax.annotation.Nullable;
import javax.ws.rs.QueryParam;
import java.util.Optional;

public class WorkerDefQuery {
    @QueryParam("nameLike")
    @Nullable
    private String nameLike;

    @QueryParam("notDeleted")
    @Nullable
    private Boolean notDeleted;

    public WorkerDefQuery() {

    }

    private WorkerDefQuery(
            @Nullable String nameLike,
            @Nullable Boolean notDeleted
    ) {
        this.nameLike = nameLike;
        this.notDeleted = notDeleted;
    }

    public WorkerDefQuery withNameLike(String nameLike) {
        return new WorkerDefQuery(
                nameLike,
                this.notDeleted
        );
    }

    public WorkerDefQuery withNotDeleted(Boolean notDeleted) {
        return new WorkerDefQuery(
                this.nameLike,
                notDeleted
        );
    }

    public Optional<String> getNameLike() {
        return Optional.ofNullable(nameLike);
    }

    public Optional<Boolean> getNotDeleted() {
        return Optional.ofNullable(notDeleted);
    }
}
