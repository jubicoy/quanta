package fi.jubic.quanta.controller;

import fi.jubic.quanta.models.Task;
import fi.jubic.quanta.scheduled.CronRegistration;
import fi.jubic.quanta.scheduled.InstanceJobFactory;
import fi.jubic.quanta.scheduled.RunnableScheduledTask;
import fi.jubic.quanta.scheduled.SingleTriggerJob;
import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public class SchedulerController {
    private final Scheduler scheduler;

    @Inject
    SchedulerController(
            Scheduler scheduler
    ) {
        this.scheduler = scheduler;
    }

    public InstanceJobFactory createInstanceJobFactory(
            Map<String, CronRegistration> cronTasksWithNames,
            Map<String, SingleTriggerJob> singleTriggerJobMap
    ) {
        Map<String, RunnableScheduledTask> runnableCronTasks = cronTasksWithNames.entrySet()
                .stream()
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                pair -> new RunnableScheduledTask(
                                        pair.getValue().getTask()
                                )
                        )
                );

        Map<String, RunnableScheduledTask> runnableSingleTriggerJobs = singleTriggerJobMap
                .entrySet()
                .stream()
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                pair -> new RunnableScheduledTask(
                                        pair.getValue().getTask()
                                )
                        )
                );

        return new InstanceJobFactory(
                Stream.concat(
                        runnableCronTasks.entrySet().stream(),
                        runnableSingleTriggerJobs.entrySet().stream()
                ).collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue
                        )
                )
        );
    }

    public void scheduleTask(
            Map<String, CronRegistration> cronTasksWithNames,
            Map<String, SingleTriggerJob> singleTriggerJobMap,
            Task createdTask
    ) {
        try {
            scheduler.setJobFactory(
                    createInstanceJobFactory(
                            cronTasksWithNames,
                            singleTriggerJobMap
                    )
            );

            scheduler.scheduleJob(
                    JobBuilder.newJob(Job.class)
                            .withIdentity(createdTask.getName())
                            .build(),
                    TriggerBuilder.newTrigger()
                            .withSchedule(
                                    CronScheduleBuilder.cronSchedule(
                                            Objects.requireNonNull(
                                                    createdTask.getCronTrigger()
                                            )
                                    )
                            )
                            .build()
            );

            if (!scheduler.isStarted()) {
                scheduler.start();
            }
        }
        catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    public void deleteTask(
            Map<String, CronRegistration> cronTasksWithNames,
            Map<String, SingleTriggerJob> singleTriggerJobMap,
            Task existingTask
    ) {
        try {
            // Check that scheduled task exists
            if (!scheduler.checkExists(new JobKey(existingTask.getName()))) {
                return;
            }
            // Delete job and update JobFactory
            scheduler.deleteJob(
                    new JobKey(existingTask.getName())
            );
            scheduler.setJobFactory(
                    createInstanceJobFactory(
                            cronTasksWithNames,
                            singleTriggerJobMap
                    )
            );
        }
        catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    public void deleteTask(
            Task task
    ) {
        try {
            // Check that scheduled task exists
            if (!scheduler.checkExists(new JobKey(task.getName()))) {
                return;
            }

            scheduler.deleteJob(
                    new JobKey(task.getName())
            );
        }
        catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    public void scheduleSingleTriggerJob(
            Map<String, CronRegistration> cronTasksWithNames,
            Map<String, SingleTriggerJob> singleTriggerJobMap,
            SingleTriggerJob singleTriggerJob
    ) {
        try {
            // If there is job with same name -> return
            // Happens when longer duration data sync task is manually invoked before previous one
            // is finished causing multiple delete jobs with same name
            if (scheduler.checkExists(new JobKey(singleTriggerJob.getJobName()))) {
                return;
            }

            scheduler.setJobFactory(
                    createInstanceJobFactory(
                            cronTasksWithNames,
                            singleTriggerJobMap
                    )
            );

            scheduler.scheduleJob(
                    JobBuilder.newJob(Job.class)
                            .withIdentity(singleTriggerJob.getJobName())
                            .build(),
                    TriggerBuilder.newTrigger()
                            .startAt(Date.from(singleTriggerJob.getStartAt()))
                            .build()
            );

            if (!scheduler.isStarted()) {
                scheduler.start();
            }
        }
        catch (SchedulerException e) {
            e.printStackTrace();
        }
    }
}
