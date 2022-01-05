package fi.jubic.quanta.fi.jubic.quanta.integration;

import fi.jubic.easyconfig.ConfigMapper;
import fi.jubic.easyconfig.providers.StaticEnvProvider;
import fi.jubic.quanta.App;
import fi.jubic.quanta.AppComponent;
import fi.jubic.quanta.DaggerAppComponent;
import fi.jubic.quanta.config.Configuration;
import fi.jubic.quanta.models.LoginRequest;
import fi.jubic.snoozy.test.UriBuilder;
import org.testcontainers.containers.JdbcDatabaseContainer;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.function.Supplier;

import static fi.jubic.snoozy.test.TestUtil.requestJson;
import static fi.jubic.snoozy.test.TestUtil.responseJsonNode;

public class TestUtil {
    static App application(Configuration configuration) {
        App app = DaggerAppComponent.builder()
                .appModule(new AppComponent.AppModule(() -> configuration))
                .build()
                .getApp();

        App.startServices(app);

        return app;
    }

    static Configuration configuration(JdbcDatabaseContainer db) {
        return new ConfigMapper(
                new StaticEnvProvider()
                        .with("JOOQ_URL", db.getJdbcUrl())
                        .with("JOOQ_USER", db.getUsername())
                        .with("JOOQ_PASSWORD", db.getPassword())
                        .with("JOOQ_DIALECT", "POSTGRES")
                        .with("ADMIN_USERNAME", "admin")
                        .with("ADMIN_PASSWORD", "admin")
        ).read(Configuration.class);
    }

    static Supplier<String[]> auth(
            HttpClient client,
            UriBuilder uriBuilder,
            String username,
            String password
    ) throws Exception {
        var response = client.send(
                HttpRequest
                        .newBuilder(
                                uriBuilder.build("/api/login")
                        )
                        .header("Content-Type", "application/json")
                        .POST(
                                requestJson(
                                        LoginRequest.builder()
                                                .setUsername(username)
                                                .setPassword(password)
                                                .build()
                                )
                        )
                        .build(),
                responseJsonNode()
        ).body();

        String token = response.get("token").asText();

        return () -> new String[] { "Authorization", token };
    }
}
