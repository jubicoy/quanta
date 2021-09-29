package fi.jubic.quanta.models;

import javax.annotation.Nullable;
import javax.ws.rs.QueryParam;
import java.util.Optional;

public class DataSeriesQuery {
    @QueryParam("notDeleted")
    @Nullable
    private Boolean notDeleted;

    public DataSeriesQuery() {

    }

    private DataSeriesQuery(
            @Nullable Boolean notDeleted
    ) {
        this.notDeleted = notDeleted;
    }

    public Optional<Boolean> getNotDeleted() {
        return Optional.ofNullable(notDeleted);
    }

    public DataSeriesQuery withNotDeleted(Boolean notDeleted) {
        return new DataSeriesQuery(
                notDeleted
        );
    }
}
