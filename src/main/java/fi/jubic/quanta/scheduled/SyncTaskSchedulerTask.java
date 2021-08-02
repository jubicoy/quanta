package fi.jubic.quanta.scheduled;

import fi.jubic.quanta.controller.DataController;
import fi.jubic.quanta.controller.TaskController;
import fi.jubic.quanta.models.ColumnSelector;
import fi.jubic.quanta.models.DataSeries;
import fi.jubic.quanta.models.Invocation;
import fi.jubic.quanta.models.InvocationQuery;
import fi.jubic.quanta.models.InvocationStatus;
import fi.jubic.quanta.models.TaskType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SyncTaskSchedulerTask implements fi.jubic.easyschedule.Task {
    private static final Logger LOGGER = LoggerFactory.getLogger(SyncTaskSchedulerTask.class);

    private final DataController dataController;
    private final TaskController taskController;
    private final ExecutorService internalExecutor;

    private final Object lock = new Object();

    @Inject
    public SyncTaskSchedulerTask(
            DataController dataController,
            TaskController taskController
    ) {
        this.dataController = dataController;
        this.taskController = taskController;
        this.internalExecutor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void run() {
        tryRunNextInvocation();
    }

    private Optional<Invocation> getNextInternalInvocation() {
        return taskController
                .searchInvocations(
                        new InvocationQuery()
                                .withStatus(InvocationStatus.Pending)
                )
                .stream()
                .filter(invocation -> invocation.getTask().getTaskType() == TaskType.sync)
                .filter(invocation -> invocation.getTask().getWorkerDef() == null)
                .min(Comparator.comparing(Invocation::getId));
    }

    private void tryRunNextInvocation() {
        synchronized (lock) {
            getNextInternalInvocation().ifPresent(invocation -> {
                taskController.updateInvocationStatus(invocation, InvocationStatus.Running);
                Optional<DataSeries> optionalDataSeries = invocation.getColumnSelectors()
                        .stream()
                        .findFirst()
                        .map(ColumnSelector::getSeries);

                if (optionalDataSeries.isEmpty()) {
                    LOGGER.error("No data series present for invocation {}", invocation.getId());
                    taskController.updateInvocationStatus(invocation, InvocationStatus.Error);
                    return;
                }

                DataSeries dataSeries = optionalDataSeries.get();

                internalExecutor.submit(() -> {
                    try {
                        dataController.sync(dataSeries, invocation.getTask());
                        taskController.updateInvocationStatus(invocation, InvocationStatus.Completed);
                    }
                    catch (RuntimeException exception) {
                        LOGGER.error(
                                "Sync failed",
                                exception
                        );
                        taskController.updateInvocationStatus(invocation, InvocationStatus.Error);
                    }

                    tryRunNextInvocation();
                });
            });
        }
    }
}
