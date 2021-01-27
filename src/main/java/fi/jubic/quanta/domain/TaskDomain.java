package fi.jubic.quanta.domain;

import fi.jubic.quanta.exception.ApplicationException;
import fi.jubic.quanta.exception.InputException;
import fi.jubic.quanta.models.Anomaly;
import fi.jubic.quanta.models.Invocation;
import fi.jubic.quanta.models.InvocationStatus;
import fi.jubic.quanta.models.SeriesResult;
import fi.jubic.quanta.models.Task;
import fi.jubic.quanta.models.Worker;
import org.quartz.CronExpression;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public class TaskDomain {
    private static final String NAMING_PATTERN_REGEX = "^[a-zA-Z0-9-_]+$";

    @Inject
    TaskDomain() {

    }

    public Task create(Task task) {
        // Validate Task name
        if (!Pattern.matches(NAMING_PATTERN_REGEX, task.getName())) {
            throw new InputException("Task's name is invalid");
        }

        if (Objects.nonNull(task.getCronTrigger())) {
            if (!CronExpression.isValidExpression(task.getCronTrigger())) {
                throw new InputException("Cron expression is invalid ");
            }
        }
        return task;
    }

    public Task update(Task oldTask, Task newTask) {
        if (Objects.equals(oldTask, newTask)) {
            return oldTask;
        }

        return oldTask.toBuilder()
                .setName(newTask.getName())
                .setCronTrigger(newTask.getCronTrigger())
                .setTaskTrigger(newTask.getTaskTrigger())
                .setConfig(newTask.getConfig())
                .setParameters(newTask.getParameters())
                .build();
    }

    public Invocation createInvocation(Task task, List<Worker> workers) {
        Worker worker = workers.stream()
                // TODO: Select most suited worker.
                .findFirst()
                .orElseThrow(
                        () -> new ApplicationException(
                                "Could not invoke task: No available Workers."
                        )
                );

        return Invocation.builder()
                .setId(0L)
                .setInvocationNumber(0L)
                .setTask(task)
                .setWorker(worker)
                .setConfig(task.getConfig())
                .setColumnSelectors(task.getColumnSelectors())
                .setOutputColumns(task.getOutputColumns())
                .setParameters(task.getParameters())
                .setStatus(InvocationStatus.Pending)
                .setStartTime(null)
                .setEndTime(null)
                .setDeletedAt(null)
                .build();
    }

    public Invocation createInvocationWithoutWorker(Task task) {
        return Invocation.builder()
                .setId(0L)
                .setInvocationNumber(0L)
                .setTask(task)
                .setWorker(null)
                .setConfig(task.getConfig())
                .setColumnSelectors(task.getColumnSelectors())
                .setOutputColumns(task.getOutputColumns())
                .setStatus(InvocationStatus.Pending)
                .setStartTime(null)
                .setEndTime(null)
                .setDeletedAt(null)
                .build();
    }

    public Invocation updateInvocationStatus(
            Invocation invocation,
            InvocationStatus status
    ) {
        switch (invocation.getStatus()) {
            case Pending:
                if (status != InvocationStatus.Running) {
                    throw new ApplicationException(
                            String.format(
                                    "Invalid status [%s], valid statuses [%s]",
                                    status,
                                    InvocationStatus.Running
                            )
                    );
                }
                return invocation.toBuilder()
                        .setStartTime(Instant.now())
                        .setStatus(status)
                        .build();

            case Running:
                switch (status) {
                    case Completed:
                    case Error:
                        return invocation.toBuilder()
                                .setEndTime(Instant.now())
                                .setStatus(status)
                                .build();
                    default:
                        throw new ApplicationException(
                                String.format(
                                        "Invalid status [%s], valid statuses [%s]",
                                        status,
                                        Stream
                                                .of(
                                                        InvocationStatus.Running,
                                                        InvocationStatus.Error
                                                )
                                                .map(Object::toString)
                                                .collect(Collectors.joining(", "))
                                )
                        );
                }

            default:
                throw new ApplicationException("Cannot change status of a complete Invocation");
        }
    }

    public SeriesResult createSeriesResult(
            Invocation invocation
    ) {
        return SeriesResult.builder()
                .setId(0L)
                .setInvocation(invocation)
                .setTableName(
                        String.format(
                                "result_%s",
                                UUID.randomUUID()
                                        .toString()
                                        .replace('-', '_')
                        )
                )
                .build();
    }

    public Anomaly createAnomaly(
            Anomaly anomaly,
            Invocation invocation
    ) {
        return anomaly.toBuilder()
                .build();
    }

    public Task delete(Task task) {
        return task.toBuilder()
                .setDeletedAt(Instant.now())
                .build();
    }
}
