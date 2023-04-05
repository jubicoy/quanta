package fi.jubic.quanta.models;

import jakarta.ws.rs.QueryParam;

import javax.annotation.Nullable;
import java.util.Optional;

public class SeriesResultQuery {
    @QueryParam("invocation")
    @Nullable
    private Long invocationId;

    @QueryParam("invocationStatus")
    @Nullable
    private String invocationStatus;

    public SeriesResultQuery() {

    }

    private SeriesResultQuery(
            @Nullable Long invocationId,
            @Nullable String invocationStatus
    ) {
        this.invocationId = invocationId;
        this.invocationStatus = invocationStatus;
    }

    public SeriesResultQuery withInvocationId(Long invocationId) {
        return new SeriesResultQuery(
                invocationId,
                this.invocationStatus
        );
    }

    public SeriesResultQuery withInvocationStatus(String invocationStatus) {
        return new SeriesResultQuery(
                this.invocationId,
                invocationStatus
        );
    }

    public Optional<Long> getInvocationId() {
        return Optional.ofNullable(invocationId);
    }

    public Optional<String> getInvocationStatus() {
        return Optional.ofNullable(invocationStatus);
    }
}
