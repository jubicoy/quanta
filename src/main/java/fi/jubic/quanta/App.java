package fi.jubic.quanta;

import fi.jubic.easyschedule.StartupScheduler;
import fi.jubic.easyschedule.TaskScheduler;
import fi.jubic.easyschedule.liquibase.LiquibaseTask;
import fi.jubic.quanta.auth.AdminAuthenticationTask;
import fi.jubic.quanta.config.Configuration;
import fi.jubic.quanta.models.QuantaAuthenticator;
import fi.jubic.quanta.models.User;
import fi.jubic.quanta.scheduled.ScheduledTask;
import fi.jubic.snoozy.Application;
import fi.jubic.snoozy.AuthenticatedApplication;
import fi.jubic.snoozy.MethodAccess;
import fi.jubic.snoozy.Snoozy;
import fi.jubic.snoozy.StaticFiles;
import fi.jubic.snoozy.auth.Authentication;
import fi.jubic.snoozy.auth.implementation.DefaultAuthorizer;
import fi.jubic.snoozy.auth.implementation.HeaderParser;
import fi.jubic.snoozy.filters.UrlRewrite;
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
    ScheduledTask scheduledTask;

    @Inject
    @Resources
    Set<Object> resources;

    @Inject
    App() {
    }

    @Override
    public Set<Object> getSingletons() {
        return Stream.concat(
                resources.stream(),
                Snoozy.builtins().stream()
        ).collect(Collectors.toSet());
    }

    @Override
    public Set<StaticFiles> getStaticFiles() {
        return Collections.singleton(
                StaticFiles.builder()
                        .setPrefix("static")
                        .setClassLoader(Application.class.getClassLoader())
                        .setMethodAccess(MethodAccess.anonymous())
                        .setRewrite(
                                UrlRewrite.builder()
                                        .setFrom("^\\/(?!(((api|assets).*)|.*\\.(html|js)$)).*$")
                                        .setTo("/index.html")
                                        .build()
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


    public Configuration getConfiguration() {
        return this.configuration;
    }

    public static void main(String[] args) {
        App app = DaggerAppComponent.create()
                .getApp();
        Configuration configuration = app.getConfiguration();

        TaskScheduler taskScheduler = new StartupScheduler()
                .registerStartupTask(
                        new LiquibaseTask(
                                configuration.getJooqConfiguration(),
                                "migrations.xml"
                        )
                )
                .registerStartupTask(
                        app.adminAuthenticationTask
                )
                .registerStartupTask(
                        app.scheduledTask
                );

        taskScheduler.start();

        new UndertowServer().start(app, configuration);

    }
}
