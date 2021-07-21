package fi.jubic.quanta.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import fi.jubic.quanta.models.configuration.CsvDataSeriesConfiguration;
import fi.jubic.quanta.models.configuration.ImportWorkerDataSeriesConfiguration;
import fi.jubic.quanta.models.configuration.JdbcDataSeriesConfiguration;
import fi.jubic.quanta.models.configuration.JsonIngestDataSeriesConfiguration;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(CsvDataSeriesConfiguration.class),
        @JsonSubTypes.Type(JdbcDataSeriesConfiguration.class),
        @JsonSubTypes.Type(JsonIngestDataSeriesConfiguration.class),
        @JsonSubTypes.Type(ImportWorkerDataSeriesConfiguration.class)
})
public abstract class DataSeriesConfiguration {
    @JsonIgnore
    public abstract <T> T visit(FunctionVisitor<T> visitor);

    @JsonIgnore
    public abstract void visit(ConsumerVisitor visitor);

    public interface FunctionVisitor<T> {
        T onCsv(CsvDataSeriesConfiguration csvConfiguration);

        T onJdbc(JdbcDataSeriesConfiguration jdbcConfiguration);

        T onJson(JsonIngestDataSeriesConfiguration ignored);

        T onImportWorker(ImportWorkerDataSeriesConfiguration importWorkerConfiguration);
    }

    public abstract static class DefaultFunctionVisitor<T> implements FunctionVisitor<T> {
        @Override
        public T onCsv(CsvDataSeriesConfiguration csvConfiguration) {
            return otherwise(csvConfiguration);
        }

        @Override
        public T onJdbc(JdbcDataSeriesConfiguration jdbcConfiguration) {
            return otherwise(jdbcConfiguration);
        }

        @Override
        public T onJson(JsonIngestDataSeriesConfiguration jsonConfiguration) {
            return otherwise(jsonConfiguration);
        }

        @Override
        public T onImportWorker(ImportWorkerDataSeriesConfiguration importWorkerConfiguration) {
            return otherwise(importWorkerConfiguration);
        }

        public abstract T otherwise(DataSeriesConfiguration configuration);
    }

    public interface ConsumerVisitor {
        void onCsv(CsvDataSeriesConfiguration csvConfiguration);

        void onJdbc(JdbcDataSeriesConfiguration jdbcConfiguration);

        void onJson(JsonIngestDataSeriesConfiguration jsonConfiguration);

        void onImportWorker(ImportWorkerDataSeriesConfiguration importWorkerConfiguration);
    }

    public abstract static class DefaultConsumerVisitor implements ConsumerVisitor {
        @Override
        public void onCsv(CsvDataSeriesConfiguration csvConfiguration) {
            otherwise(csvConfiguration);
        }

        @Override
        public void onJdbc(JdbcDataSeriesConfiguration jdbcConfiguration) {
            otherwise(jdbcConfiguration);
        }

        @Override
        public void onJson(JsonIngestDataSeriesConfiguration jsonConfiguration) {
            otherwise(jsonConfiguration);
        }

        @Override
        public void onImportWorker(ImportWorkerDataSeriesConfiguration importWorkerConfiguration) {
            otherwise(importWorkerConfiguration);
        }

        public abstract void otherwise(DataSeriesConfiguration configuration);
    }
}
