package fi.jubic.quanta.config;

import fi.jubic.easyconfig.annotations.ConfigProperty;
import fi.jubic.easyconfig.extensions.LiquibaseExtension;
import fi.jubic.easyconfig.jooq.JooqConfiguration;
import fi.jubic.easyconfig.snoozy.SnoozyServerConfiguration;
import fi.jubic.quanta.auth.Admin;
import fi.jubic.snoozy.ServerConfiguration;
import fi.jubic.snoozy.ServerConfigurator;

public class Configuration implements ServerConfigurator {
    private final ServerConfiguration serverConfiguration;
    private final JooqConfiguration jooqConfiguration;
    private String filePath;
    private String jdbcPath;
    private Integer persistOldSeriesTables;
    private Admin admin;

    public Configuration(
            @ConfigProperty("SERVER_")
                    SnoozyServerConfiguration serverConfiguration,
            @LiquibaseExtension(migrations = "migrations.xml")
            @ConfigProperty("")
                    JooqConfiguration jooqConfiguration,
            @ConfigProperty(value = "SERVER_FILE_PATH", defaultValue = "/tmp")
                    String filePath,
            @ConfigProperty(value = "SERVER_JDBC_PATH", defaultValue = "/tmp")
                    String jdbcPath,
            @ConfigProperty(value = "SERVER_PERSIST_OLD_SERIES_TABLES", defaultValue = "3600")
                    Integer persistOldSeriesTables,
            @ConfigProperty(value = "ADMIN_")
                    Admin admin
    ) {
        this.serverConfiguration = serverConfiguration;
        this.jooqConfiguration = jooqConfiguration;
        setFilePath(filePath);
        setJdbcPath(jdbcPath);
        setPersistOldSeriesTables(persistOldSeriesTables);
        setAdmin(admin);
    }

    @Override
    public ServerConfiguration getServerConfiguration() {
        return this.serverConfiguration;
    }

    public JooqConfiguration getJooqConfiguration() {
        return jooqConfiguration;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        if (filePath.endsWith("/")) {
            this.filePath = filePath.substring(0, filePath.length() - 1);
        }
        else {
            this.filePath = filePath;
        }
    }

    public String getJdbcPath() {
        return jdbcPath;
    }

    public void setJdbcPath(String jdbcPath) {
        if (jdbcPath.endsWith("/")) {
            this.jdbcPath = jdbcPath.substring(0, jdbcPath.length() - 1);
        }
        else {
            this.jdbcPath = jdbcPath;
        }
    }

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public Integer getPersistOldSeriesTables() {
        return persistOldSeriesTables;
    }

    public void setPersistOldSeriesTables(Integer persistOldSeriesTables) {
        this.persistOldSeriesTables = persistOldSeriesTables;
    }
}
