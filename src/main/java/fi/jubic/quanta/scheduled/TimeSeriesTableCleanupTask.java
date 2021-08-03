package fi.jubic.quanta.scheduled;

import fi.jubic.quanta.controller.DataController;

import javax.inject.Inject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TimeSeriesTableCleanupTask implements fi.jubic.easyschedule.Task {
    private final DataController dataController;
    private final ExecutorService internalExecutor;

    @Inject
    public TimeSeriesTableCleanupTask(DataController dataController) {
        this.dataController = dataController;
        this.internalExecutor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void run() {
        internalExecutor.submit(dataController::cleanupDeletedTables);
    }
}
