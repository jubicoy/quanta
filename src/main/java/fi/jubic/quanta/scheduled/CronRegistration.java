package fi.jubic.quanta.scheduled;

import fi.jubic.easyschedule.Task;

public class CronRegistration {
    private final String cron;
    private final Task task;
    private final String taskName;

    private CronRegistration(String cron, Task task, String taskName) {
        this.cron = cron;
        this.task = task;
        this.taskName = taskName;
    }

    public static CronRegistration of(
            String cron,
            Task task,
            String taskName
    ) {
        return new CronRegistration(cron, task, taskName);
    }

    public String getCron() {
        return cron;
    }

    public String getTaskName() {
        return taskName;
    }

    public Task getTask() {
        return task;
    }

}
