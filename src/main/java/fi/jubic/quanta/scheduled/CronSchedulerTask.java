package fi.jubic.quanta.scheduled;

import fi.jubic.quanta.controller.TaskController;
import fi.jubic.quanta.models.Invocation;
import fi.jubic.quanta.models.InvocationQuery;
import fi.jubic.quanta.models.InvocationStatus;
import fi.jubic.quanta.models.Task;
import fi.jubic.quanta.models.TaskQuery;
import org.quartz.CronExpression;

import javax.inject.Inject;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class CronSchedulerTask implements fi.jubic.easyschedule.Task {
    private static final Set<InvocationStatus> ACTIVE_STATUSES = Set.of(
            InvocationStatus.Pending,
            InvocationStatus.Running
    );

    private final TaskController taskController;

    @Inject
    public CronSchedulerTask(TaskController taskController) {
        this.taskController = taskController;
    }

    @Override
    public void run() {
        List<Task> cronTasks = taskController.search(new TaskQuery().withHasCronTrigger(true));

        cronTasks.stream()
                .filter(task -> task.getCronTrigger() != null)
                .filter(task -> {
                    CronExpression expression;
                    try {
                        expression = new CronExpression(task.getCronTrigger());
                    }
                    catch (ParseException e) {
                        return false;
                    }

                    Instant nextRun = expression
                            .getNextValidTimeAfter(
                                    Date.from(
                                            Instant.now().minus(5, ChronoUnit.MINUTES)
                                    )
                            )
                            .toInstant();

                    if (nextRun.isAfter(Instant.now())) {
                        return false;
                    }

                    Optional<Invocation> optionalInvocation = taskController
                            .searchInvocations(new InvocationQuery().withTaskId(task.getId()))
                            .stream()
                            .max(Comparator.comparing(Invocation::getInvocationNumber));

                    if (optionalInvocation.isEmpty()) {
                        return true;
                    }

                    Invocation invocation = optionalInvocation.get();
                    if (ACTIVE_STATUSES.contains(invocation.getStatus())) {
                        return false;
                    }

                    return Optional.ofNullable(invocation.getStartTime())
                            .filter(start -> start.isBefore(nextRun))
                            .isPresent();
                })
                .forEach(task -> taskController.invoke(task.getId()));
    }
}
