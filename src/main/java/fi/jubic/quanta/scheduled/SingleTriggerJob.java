package fi.jubic.quanta.scheduled;

import fi.jubic.easyschedule.Task;

import java.time.Instant;

public class SingleTriggerJob {
    private final Instant startAt;
    private final Task task;
    private final String jobName;

    private SingleTriggerJob(
            Instant startAt,
            Task task,
            String jobName
    ) {
        this.startAt = startAt;
        this.task = task;
        this.jobName = jobName;
    }

    public static SingleTriggerJob of(
            Instant startAt,
            Task task,
            String name
    ) {
        return new SingleTriggerJob(startAt, task, name);
    }

    public Instant getStartAt() {
        return startAt;
    }

    public Task getTask() {
        return task;
    }

    public String getJobName() {
        return jobName;
    }

}
