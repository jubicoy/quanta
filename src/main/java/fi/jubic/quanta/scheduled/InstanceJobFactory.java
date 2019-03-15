package fi.jubic.quanta.scheduled;

import org.quartz.Job;
import org.quartz.Scheduler;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;

import java.util.Map;

public class InstanceJobFactory implements JobFactory {
    private final Map<String, RunnableScheduledTask> taskMap;

    public InstanceJobFactory(Map<String, RunnableScheduledTask> taskMap) {
        this.taskMap = taskMap;
    }

    @Override
    public Job newJob(TriggerFiredBundle triggerFiredBundle, Scheduler scheduler) {
        return taskMap.get(triggerFiredBundle.getJobDetail().getKey().getName());
    }
}
