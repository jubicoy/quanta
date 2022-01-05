package fi.jubic.quanta;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import fi.jubic.easyconfig.ConfigMapper;
import fi.jubic.quanta.config.Configuration;
import fi.jubic.quanta.external.ExternalModule;
import fi.jubic.quanta.resources.ResourceModule;

import javax.inject.Singleton;
import java.util.function.Supplier;

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
        private final Supplier<Configuration> provider;

        public AppModule() {
            this.provider = () -> new ConfigMapper().read(Configuration.class);
        }

        public AppModule(Supplier<Configuration> provider) {
            this.provider = provider;
        }

        @Provides
        @Singleton
        public Configuration provideConfiguration() {
            return provider.get();
        }
    }
}
