package fi.jubic.quanta.models;

import org.quartz.Job;
import org.quartz.JobExecutionContext;

import javax.inject.Singleton;

@Singleton
public class TaskJob implements Job {
    private final fi.jubic.easyschedule.Task task;

    public TaskJob(fi.jubic.easyschedule.Task task) {
        this.task = task;
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        task.run();
    }
}
