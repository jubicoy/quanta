package fi.jubic.quanta.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import fi.jubic.quanta.models.configuration.CsvDataConnectionConfiguration;
import fi.jubic.quanta.models.configuration.JdbcDataConnectionConfiguration;
import fi.jubic.quanta.models.configuration.JsonIngestDataConnectionConfiguration;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(CsvDataConnectionConfiguration.class),
        @JsonSubTypes.Type(JdbcDataConnectionConfiguration.class),
        @JsonSubTypes.Type(JsonIngestDataConnectionConfiguration.class)
})
public abstract class DataConnectionConfiguration {
    @JsonIgnore
    public abstract <T> T visit(FunctionVisitor<T> visitor);

    @JsonIgnore
    public abstract void visit(ConsumerVisitor visitor);

    public interface FunctionVisitor<T> {
        T onCsv(CsvDataConnectionConfiguration csvConfiguration);

        T onJdbc(JdbcDataConnectionConfiguration jdbcConfiguration);

        T onJson(JsonIngestDataConnectionConfiguration jsonConfiguration);
    }

    public abstract static class DefaultFunctionVisitor<T> implements FunctionVisitor<T> {
        @Override
        public T onCsv(CsvDataConnectionConfiguration csvConfiguration) {
            return otherwise(csvConfiguration);
        }

        @Override
        public T onJdbc(JdbcDataConnectionConfiguration jdbcConfiguration) {
            return otherwise(jdbcConfiguration);
        }

        @Override
        public T onJson(JsonIngestDataConnectionConfiguration jsonConfiguration) {
            return otherwise(jsonConfiguration);
        }

        public abstract T otherwise(DataConnectionConfiguration configuration);
    }

    public interface ConsumerVisitor {
        void onCsv(CsvDataConnectionConfiguration csvConfiguration);

        void onJdbc(JdbcDataConnectionConfiguration jdbcConfiguration);

        void onJson(JsonIngestDataConnectionConfiguration jsonConfiguration);
    }

    public abstract static class DefaultConsumerVisitor implements ConsumerVisitor {
        @Override
        public void onCsv(CsvDataConnectionConfiguration csvConfiguration) {
            otherwise(csvConfiguration);
        }

        @Override
        public void onJdbc(JdbcDataConnectionConfiguration jdbcConfiguration) {
            otherwise(jdbcConfiguration);
        }

        @Override
        public void onJson(JsonIngestDataConnectionConfiguration jsonConfiguration) {
            otherwise(jsonConfiguration);
        }

        public abstract void otherwise(DataConnectionConfiguration configuration);
    }
}
