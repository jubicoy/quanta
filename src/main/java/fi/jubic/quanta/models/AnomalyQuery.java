package fi.jubic.quanta.models;

import jakarta.ws.rs.QueryParam;

import javax.annotation.Nullable;
import java.util.Optional;

public class AnomalyQuery {
    @QueryParam("invocation")
    @Nullable
    private Long invocationId;

    public AnomalyQuery() {

    }

    private AnomalyQuery(
            Long invocationId
    ) {
        this.invocationId = invocationId;
    }

    public AnomalyQuery withInvocationId(Long invocationId) {
        return new AnomalyQuery(
                invocationId
        );
    }

    public Optional<Long> getInvocationId() {
        return Optional.ofNullable(invocationId);
    }
}
