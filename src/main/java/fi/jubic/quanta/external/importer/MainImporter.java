package fi.jubic.quanta.external.importer;

import fi.jubic.quanta.external.Importer;
import fi.jubic.quanta.external.importer.csv.CsvImporter;
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
import fi.jubic.quanta.models.configuration.JdbcDataConnectionConfiguration;
import fi.jubic.quanta.models.configuration.JdbcDataSeriesConfiguration;
import fi.jubic.quanta.models.configuration.JsonIngestDataConnectionConfiguration;
import fi.jubic.quanta.models.configuration.JsonIngestDataSeriesConfiguration;
import fi.jubic.quanta.models.metadata.DataConnectionMetadata;
import fi.jubic.quanta.models.typemetadata.TypeMetadata;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.stream.Stream;

@Singleton
public class MainImporter implements Importer {
    private final Importer csvImporter;
    private final Importer jdbcImporter;
    private final Importer jsonImporter;

    @Inject
    public MainImporter(
            CsvImporter csvImporter,
            JdbcImporter jdbcImporter,
            JsonIngestImporter jsonImporter
    ) {
        this.csvImporter = csvImporter;
        this.jdbcImporter = jdbcImporter;
        this.jsonImporter = jsonImporter;
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
                });
    }

    @Override
    public DataConnection getWithEmptyLogin(DataConnection dataConnection) {
        return dataConnection.getConfiguration()
                .visit(new DataConnectionConfiguration.FunctionVisitor<DataConnection>() {
                    @Override
                    public DataConnection onCsv(CsvDataConnectionConfiguration ignored) {
                        return csvImporter.getWithEmptyLogin(dataConnection);
                    }

                    @Override
                    public DataConnection onJdbc(
                            JdbcDataConnectionConfiguration jdbcConfiguration
                    ) {
                        return jdbcImporter.getWithEmptyLogin(dataConnection);
                    }

                    @Override
                    public DataConnection onJson(JsonIngestDataConnectionConfiguration ignored) {
                        return jsonImporter.getWithEmptyLogin(dataConnection);
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
                });
    }

    @Override
    public Stream<List<String>> getRows(DataSeries dataSeries) {
        return dataSeries.getConfiguration()
                .visit(new DataSeriesConfiguration.FunctionVisitor<Stream<List<String>>>() {
                    @Override
                    public Stream<List<String>> onCsv(CsvDataSeriesConfiguration ignored) {
                        return csvImporter.getRows(dataSeries);
                    }

                    @Override
                    public Stream<List<String>> onJdbc(
                            JdbcDataSeriesConfiguration jdbcConfiguration
                    ) {
                        return jdbcImporter.getRows(dataSeries);
                    }

                    @Override
                    public Stream<List<String>> onJson(JsonIngestDataSeriesConfiguration ignored) {
                        return jsonImporter.getRows(dataSeries);
                    }
                });
    }


}
