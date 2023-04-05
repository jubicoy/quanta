package fi.jubic.quanta.models;

import jakarta.ws.rs.QueryParam;

import javax.annotation.Nullable;
import java.util.Optional;

public class ExternalClientQuery {
    @QueryParam("user")
    @Nullable
    private Long userId;

    public ExternalClientQuery() {

    }

    public ExternalClientQuery(@Nullable Long userId) {
        this.userId = userId;
    }

    public ExternalClientQuery withUser(Long userId) {
        return new ExternalClientQuery(userId);
    }


    public Optional<Long> getUser() {
        return Optional.ofNullable(userId);
    }

}
