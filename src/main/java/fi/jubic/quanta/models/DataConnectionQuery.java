package fi.jubic.quanta.models;

import jakarta.ws.rs.QueryParam;

import javax.annotation.Nullable;
import java.util.Optional;

public class DataConnectionQuery {
    @QueryParam("notDeleted")
    @Nullable
    private Boolean notDeleted;

    public DataConnectionQuery() {

    }

    private DataConnectionQuery(
            @Nullable Boolean notDeleted
    ) {
        this.notDeleted = notDeleted;
    }

    public Optional<Boolean> getNotDeleted() {
        return Optional.ofNullable(notDeleted);
    }

    public DataConnectionQuery withNotDeleted(Boolean notDeleted) {
        return new DataConnectionQuery(
                notDeleted
        );
    }
}
