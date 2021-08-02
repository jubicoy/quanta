package fi.jubic.quanta.scheduled;

import fi.jubic.quanta.controller.DataController;

import javax.inject.Inject;

public class TimeSeriesTableCleanupTask implements fi.jubic.easyschedule.Task {
    private final DataController dataController;

    @Inject
    public TimeSeriesTableCleanupTask(DataController dataController) {
        this.dataController = dataController;
    }

    @Override
    public void run() {
        dataController.cleanupDeletedTables();
    }
}
