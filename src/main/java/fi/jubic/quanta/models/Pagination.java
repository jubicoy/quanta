package fi.jubic.quanta.models;

import jakarta.ws.rs.QueryParam;

import javax.annotation.Nullable;
import java.util.Optional;

public class Pagination {
    @QueryParam("limit")
    @Nullable
    private Integer limit;

    @QueryParam("offset")
    @Nullable
    private Integer offset;

    public Pagination() {

    }

    public Pagination(
            @Nullable Integer limit,
            @Nullable Integer offset
    ) {
        this.limit = limit;
        this.offset = offset;
    }

    public Optional<Integer> getLimit() {
        return Optional.ofNullable(limit);
    }

    public Optional<Integer> getOffset() {
        return Optional.ofNullable(offset);
    }
}
