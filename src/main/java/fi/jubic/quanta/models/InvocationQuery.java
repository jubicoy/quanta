package fi.jubic.quanta.models;

import jakarta.ws.rs.QueryParam;

import javax.annotation.Nullable;
import java.util.Optional;

public class InvocationQuery {
    @QueryParam("task")
    @Nullable
    private Long taskId;

    @QueryParam("worker")
    @Nullable
    private Long workerId;

    @Nullable
    private String workerToken;

    @QueryParam("status")
    @Nullable
    private InvocationStatus status;

    public InvocationQuery() {

    }

    private InvocationQuery(
            @Nullable Long taskId,
            @Nullable Long workerId,
            @Nullable String workerToken,
            @Nullable InvocationStatus status
    ) {
        this.taskId = taskId;
        this.workerId = workerId;
        this.workerToken = workerToken;
        this.status = status;
    }

    public InvocationQuery withTaskId(Long taskId) {
        return new InvocationQuery(
                taskId,
                this.workerId,
                this.workerToken,
                this.status
        );
    }

    public InvocationQuery withWorker(Long workerId) {
        return new InvocationQuery(
                this.taskId,
                workerId,
                this.workerToken,
                this.status
        );
    }

    public InvocationQuery withWorkerToken(String workerToken) {
        return new InvocationQuery(
                this.taskId,
                this.workerId,
                workerToken,
                this.status
        );
    }

    public InvocationQuery withStatus(InvocationStatus status) {
        return new InvocationQuery(
                this.taskId,
                this.workerId,
                this.workerToken,
                status
        );
    }

    public Optional<Long> getTaskId() {
        return Optional.ofNullable(taskId);
    }

    public Optional<Long> getWorkerId() {
        return Optional.ofNullable(workerId);
    }

    public Optional<String> getWorkerToken() {
        return Optional.ofNullable(workerToken);
    }

    public Optional<InvocationStatus> getStatus() {
        return Optional.ofNullable(status);
    }
}
