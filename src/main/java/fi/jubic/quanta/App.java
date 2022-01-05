package fi.jubic.quanta;

import fi.jubic.easyschedule.InMemoryScheduler;
import fi.jubic.easyschedule.TaskScheduler;
import fi.jubic.quanta.auth.AdminAuthenticationTask;
import fi.jubic.quanta.config.Configuration;
import fi.jubic.quanta.exception.InputExceptionMapper;
import fi.jubic.quanta.models.QuantaAuthenticator;
import fi.jubic.quanta.models.User;
import fi.jubic.quanta.scheduled.CronSchedulerTask;
import fi.jubic.quanta.scheduled.SyncTaskSchedulerTask;
import fi.jubic.quanta.scheduled.TimeSeriesTableCleanupTask;
import fi.jubic.snoozy.Application;
import fi.jubic.snoozy.AuthenticatedApplication;
import fi.jubic.snoozy.MethodAccess;
import fi.jubic.snoozy.Snoozy;
import fi.jubic.snoozy.auth.Authentication;
import fi.jubic.snoozy.auth.implementation.DefaultAuthorizer;
import fi.jubic.snoozy.auth.implementation.HeaderParser;
import fi.jubic.snoozy.filters.UrlRewrite;
import fi.jubic.snoozy.staticfiles.StaticFiles;
import fi.jubic.snoozy.undertow.UndertowServer;

import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationPath("api")
public class App implements AuthenticatedApplication<User> {
    @Inject
    Configuration configuration;

    @Inject
    QuantaAuthenticator authenticator;

    @Inject
    AdminAuthenticationTask adminAuthenticationTask;

    @Inject
    CronSchedulerTask cronSchedulerTask;

    @Inject
    SyncTaskSchedulerTask syncTaskSchedulerTask;

    @Inject
    TimeSeriesTableCleanupTask timeSeriesTableCleanupTask;

    @Inject
    @Resources
    Set<Object> resources;

    @Inject
    App() {
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> objects = Stream.concat(
                resources.stream(),
                Snoozy.builtins().stream()
        ).collect(Collectors.toSet());
        objects.add(new InputExceptionMapper());

        return objects;
    }

    @Override
    public Set<StaticFiles> getStaticFiles() {
        return Collections.singleton(
                StaticFiles.builder()
                        .setPrefix("static")
                        .setClassLoader(Application.class.getClassLoader())
                        .setMethodAccess(MethodAccess.anonymous())
                        .setRewrite(
                                UrlRewrite.of(
                                        "^\\/(?!(((api|assets).*)|.*\\.(html|js)$)).*$",
                                        "/index.html"
                                )
                        )
                        .build()
        );
    }

    @Override
    public Authentication<User> getAuthentication() {
        return Authentication.<User>builder()
                .setAuthenticator(authenticator)
                .setAuthorizer(new DefaultAuthorizer<>())
                .setTokenParser(HeaderParser.of("Authorization"))
                .setUserClass(User.class)
                .build();
    }

    public static void startServices(App app) {
        TaskScheduler taskScheduler = new InMemoryScheduler(1)
                .registerStartupTask(
                        app.adminAuthenticationTask
                )
                .registerTask("0/10 * * ? * * *", app.cronSchedulerTask)
                .registerTask("0/10 * * ? * * *", app.syncTaskSchedulerTask)
                .registerTask("0 0 0/2 ? * * *", app.timeSeriesTableCleanupTask);

        taskScheduler.start();
        app.authenticator.reloadExternalClients();
    }

    public static void main(String[] args) {
        App app = DaggerAppComponent.create()
                .getApp();

        startServices(app);

        new UndertowServer().start(app, app.configuration);
    }
}
