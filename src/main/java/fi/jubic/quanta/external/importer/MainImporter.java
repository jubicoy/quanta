package fi.jubic.quanta.external.importer;

import fi.jubic.quanta.external.Importer;
import fi.jubic.quanta.external.importer.csv.CsvImporter;
import fi.jubic.quanta.external.importer.importworker.ImportWorkerImporter;
import fi.jubic.quanta.external.importer.jdbc.JdbcImporter;
import fi.jubic.quanta.external.importer.jsoningest.JsonIngestImporter;
import fi.jubic.quanta.models.DataConnection;
import fi.jubic.quanta.models.DataConnectionConfiguration;
import fi.jubic.quanta.models.DataConnectionType;
import fi.jubic.quanta.models.DataSample;
import fi.jubic.quanta.models.DataSeries;
import fi.jubic.quanta.models.DataSeriesConfiguration;
import fi.jubic.quanta.models.configuration.CsvDataConnectionConfiguration;
import fi.jubic.quanta.models.configuration.CsvDataSeriesConfiguration;
import fi.jubic.quanta.models.configuration.ImportWorkerDataConnectionConfiguration;
import fi.jubic.quanta.models.configuration.ImportWorkerDataSeriesConfiguration;
import fi.jubic.quanta.models.configuration.JdbcDataConnectionConfiguration;
import fi.jubic.quanta.models.configuration.JdbcDataSeriesConfiguration;
import fi.jubic.quanta.models.configuration.JsonIngestDataConnectionConfiguration;
import fi.jubic.quanta.models.configuration.JsonIngestDataSeriesConfiguration;
import fi.jubic.quanta.models.metadata.DataConnectionMetadata;
import fi.jubic.quanta.models.typemetadata.TypeMetadata;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Singleton
public class MainImporter implements Importer {
    private final Importer csvImporter;
    private final Importer jdbcImporter;
    private final Importer jsonImporter;
    private final Importer importWorkerImporter;

    @Inject
    public MainImporter(
            CsvImporter csvImporter,
            JdbcImporter jdbcImporter,
            JsonIngestImporter jsonImporter,
            ImportWorkerImporter importWorkerImporter
    ) {
        this.csvImporter = csvImporter;
        this.jdbcImporter = jdbcImporter;
        this.jsonImporter = jsonImporter;
        this.importWorkerImporter = importWorkerImporter;
    }

    @Override
    public boolean test(DataConnection dataConnection) {
        return dataConnection.getConfiguration()
                .visit(new DataConnectionConfiguration.FunctionVisitor<Boolean>() {
                    @Override
                    public Boolean onCsv(CsvDataConnectionConfiguration ignored) {
                        return csvImporter.test(dataConnection);
                    }

                    @Override
                    public Boolean onJdbc(JdbcDataConnectionConfiguration jdbcConfiguration) {
                        return jdbcImporter.test(dataConnection);
                    }

                    @Override
                    public Boolean onJson(JsonIngestDataConnectionConfiguration ignored) {
                        return jsonImporter.test(dataConnection);
                    }

                    @Override
                    public Boolean onImportWorker(ImportWorkerDataConnectionConfiguration ignored) {
                        return importWorkerImporter.test(dataConnection);
                    }
                });
    }

    @Override
    public DataConnection validate(DataConnection dataConnection) {
        return dataConnection.getConfiguration()
                .visit(new DataConnectionConfiguration.FunctionVisitor<DataConnection>() {
                    @Override
                    public DataConnection onCsv(CsvDataConnectionConfiguration ignored) {
                        return csvImporter.validate(dataConnection);
                    }

                    @Override
                    public DataConnection onJdbc(
                            JdbcDataConnectionConfiguration jdbcConfiguration
                    ) {
                        return jdbcImporter.validate(dataConnection);
                    }

                    @Override
                    public DataConnection onJson(JsonIngestDataConnectionConfiguration ignored) {
                        return jsonImporter.validate(dataConnection);
                    }

                    @Override
                    public DataConnection onImportWorker(
                            ImportWorkerDataConnectionConfiguration ignored
                    ) {
                        return importWorkerImporter.validate(dataConnection);
                    }
                });
    }

    @Override
    public DataSample getSample(DataSeries dataSeries, int rows) {
        return dataSeries.getConfiguration()
                .visit(new DataSeriesConfiguration.FunctionVisitor<DataSample>() {
                    @Override
                    public DataSample onCsv(CsvDataSeriesConfiguration ignored) {
                        return csvImporter.getSample(dataSeries, rows);
                    }

                    @Override
                    public DataSample onJdbc(JdbcDataSeriesConfiguration jdbcConfiguration) {
                        return jdbcImporter.getSample(dataSeries, rows);
                    }

                    @Override
                    public DataSample onJson(JsonIngestDataSeriesConfiguration ignored) {
                        return jsonImporter.getSample(dataSeries, rows);
                    }

                    @Override
                    public DataSample onImportWorker(ImportWorkerDataSeriesConfiguration ignored) {
                        return importWorkerImporter.getSample(dataSeries, rows);
                    }
                });
    }

    @Override
    public TypeMetadata getMetadata(DataConnectionType type) {
        switch (type) {
            case CSV:
                return csvImporter.getMetadata(type);
            case JDBC:
                return jdbcImporter.getMetadata(type);
            case JSON_INGEST:
                return jsonImporter.getMetadata(type);
            case IMPORT_WORKER:
                return importWorkerImporter.getMetadata(type);
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public DataConnectionMetadata getConnectionMetadata(DataConnection dataConnection) {
        return dataConnection.getConfiguration()
                .visit(new DataConnectionConfiguration.FunctionVisitor<DataConnectionMetadata>() {
                    @Override
                    public DataConnectionMetadata onCsv(CsvDataConnectionConfiguration ignored) {
                        return csvImporter.getConnectionMetadata(dataConnection);
                    }

                    @Override
                    public DataConnectionMetadata onJdbc(
                            JdbcDataConnectionConfiguration jdbcConfiguration
                    ) {
                        return jdbcImporter.getConnectionMetadata(dataConnection);
                    }

                    @Override
                    public DataConnectionMetadata onJson(
                            JsonIngestDataConnectionConfiguration ignored
                    ) {
                        return jsonImporter.getConnectionMetadata(dataConnection);
                    }

                    @Override
                    public DataConnectionMetadata onImportWorker(
                            ImportWorkerDataConnectionConfiguration ignored
                    ) {
                        return importWorkerImporter.getConnectionMetadata(dataConnection);
                    }
                });
    }

    @Override
    public CompletableFuture<Void> getRows(
            DataSeries dataSeries,
            Consumer<Stream<List<String>>> consumer
    ) {
        return dataSeries.getConfiguration()
                .visit(new DataSeriesConfiguration.FunctionVisitor<CompletableFuture<Void>>() {
                    @Override
                    public CompletableFuture<Void> onCsv(
                            CsvDataSeriesConfiguration csvConfiguration
                    ) {
                        return csvImporter.getRows(dataSeries, consumer);
                    }

                    @Override
                    public CompletableFuture<Void> onJdbc(
                            JdbcDataSeriesConfiguration jdbcConfiguration
                    ) {
                        return jdbcImporter.getRows(dataSeries, consumer);
                    }

                    @Override
                    public CompletableFuture<Void> onJson(
                            JsonIngestDataSeriesConfiguration ignored
                    ) {
                        return jsonImporter.getRows(dataSeries, consumer);
                    }

                    @Override
                    public CompletableFuture<Void> onImportWorker(
                            ImportWorkerDataSeriesConfiguration ignored
                    ) {
                        return importWorkerImporter.getRows(dataSeries, consumer);
                    }
                });
    }
}
