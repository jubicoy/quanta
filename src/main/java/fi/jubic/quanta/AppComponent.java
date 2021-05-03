package fi.jubic.quanta;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import fi.jubic.easyconfig.ConfigMapper;
import fi.jubic.quanta.config.Configuration;
import fi.jubic.quanta.external.ExternalModule;
import fi.jubic.quanta.resources.ResourceModule;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

import javax.inject.Singleton;
import java.util.Properties;

@Singleton
@Component(modules = {
        AppComponent.AppModule.class,
        ExternalModule.class,
        ResourceModule.class
})
public interface AppComponent {
    App getApp();

    @Module
    class AppModule {
        @Provides
        @Singleton
        static Configuration provideConfiguration() {
            return new ConfigMapper().read(Configuration.class);
        }

        @Provides
        static Scheduler provideScheduler() {
            StdSchedulerFactory schedulerFactory = new StdSchedulerFactory();
            Properties properties = new Properties();
            properties.setProperty("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore");
            properties.setProperty("org.quartz.threadPool.threadCount", Integer.toString(2));
            try {
                schedulerFactory.initialize(properties);
                return schedulerFactory.getScheduler();
            }
            catch (SchedulerException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
