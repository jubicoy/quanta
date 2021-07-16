package fi.jubic.quanta.config;

import fi.jubic.easyconfig.annotations.ConfigProperty;
import fi.jubic.easyconfig.jdbc.PooledJdbcConfiguration;
import fi.jubic.easyconfig.jooq.JooqConfiguration;
import fi.jubic.easyconfig.jooq.JooqSettings;
import fi.jubic.quanta.util.TimeConverterProvider;
import org.jooq.Configuration;

public class EnhancedJooqConfiguration extends JooqConfiguration {
    private final org.jooq.Configuration enhancedConfiguration;

    public EnhancedJooqConfiguration(
            @ConfigProperty("JOOQ_") PooledJdbcConfiguration jdbcConfiguration,
            @ConfigProperty("JOOQ_") JooqSettings jooqSettings,
            @ConfigProperty("JOOQ_DIALECT") String dialect
    ) {
        super(jdbcConfiguration, jooqSettings, dialect);

        this.enhancedConfiguration = super.getConfiguration().set(new TimeConverterProvider());
    }

    @Override
    public Configuration getConfiguration() {
        return this.enhancedConfiguration;
    }
}
