package fi.jubic.quanta.scheduled;

import fi.jubic.easyschedule.Task;
import fi.jubic.easyschedule.TaskSchedulerException;
import fi.jubic.quanta.controller.DataController;
import fi.jubic.quanta.controller.SchedulerController;
import fi.jubic.quanta.controller.TaskController;
import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.Date;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public class ScheduledTask implements Task {
    private final SchedulerController schedulerController;
    private final DataController dataController;
    private final TaskController taskController;
    private final Scheduler scheduler;

    @Inject
    ScheduledTask(
            SchedulerController schedulerController,
            DataController dataController,
            TaskController taskController,
            Scheduler scheduler
    ) {
        this.schedulerController = schedulerController;
        this.dataController = dataController;
        this.taskController = taskController;
        this.scheduler = scheduler;
    }

    public void run() {
        Map<String, CronRegistration> cronTasksWithNames = taskController.getCronTasksWithNames();
        Map<String, SingleTriggerJob> singleTriggerJobMap = Stream.concat(
                dataController.getSeriesTablesDeleteJobs()
                        .entrySet()
                        .stream(),
                taskController.getLatestRunningOrPendingDataSyncJobs(Instant.now().plusSeconds(10))
                        .entrySet()
                        .stream()
        ).collect(
                Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                )
        );

        try {
            scheduler.setJobFactory(
                    schedulerController.createInstanceJobFactory(
                            cronTasksWithNames,
                            singleTriggerJobMap
                    )
            );

            cronTasksWithNames.forEach(
                    (name, registration) -> {
                        try {
                            scheduler.scheduleJob(
                                    JobBuilder.newJob(Job.class)
                                            .withIdentity(name)
                                            .build(),
                                    TriggerBuilder.newTrigger()
                                            .withSchedule(
                                                    CronScheduleBuilder.cronSchedule(
                                                            registration.getCron()
                                                    )
                                            )
                                            .build()
                            );
                        }
                        catch (SchedulerException exception) {
                            throw new TaskSchedulerException(exception);
                        }
                    }
            );

            singleTriggerJobMap.forEach(
                    (name, registration) -> {
                        try {
                            scheduler.scheduleJob(
                                    JobBuilder.newJob(Job.class)
                                            .withIdentity(name)
                                            .build(),
                                    TriggerBuilder.newTrigger()
                                            .startAt(Date.from(registration.getStartAt()))
                                            .build()
                            );
                        }
                        catch (SchedulerException exception) {
                            throw new TaskSchedulerException(exception);
                        }
                    }
            );

            scheduler.start();
        }
        catch (SchedulerException e) {
            e.printStackTrace();
        }
    }
}
