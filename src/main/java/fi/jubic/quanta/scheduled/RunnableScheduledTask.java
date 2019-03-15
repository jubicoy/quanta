package fi.jubic.quanta.scheduled;

import fi.jubic.easyschedule.Task;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

public class RunnableScheduledTask implements Job {
    private final Task task;

    public RunnableScheduledTask(Task task) {
        this.task = task;
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        this.task.run();
    }
}
