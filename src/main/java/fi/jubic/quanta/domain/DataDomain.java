package fi.jubic.quanta.domain;

import fi.jubic.quanta.exception.InputException;
import fi.jubic.quanta.models.DataConnection;
import fi.jubic.quanta.models.DataConnectionType;
import fi.jubic.quanta.models.DataSeries;
import fi.jubic.quanta.models.DataSeriesConfiguration;
import fi.jubic.quanta.models.SeriesTable;
import fi.jubic.quanta.models.configuration.CsvDataSeriesConfiguration;
import fi.jubic.quanta.models.configuration.ImportWorkerDataSeriesConfiguration;
import fi.jubic.quanta.models.configuration.JdbcDataSeriesConfiguration;
import fi.jubic.quanta.models.configuration.JsonIngestDataConnectionConfiguration;
import fi.jubic.quanta.models.configuration.JsonIngestDataSeriesConfiguration;
import fi.jubic.quanta.util.TokenGenerator;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Singleton
public class DataDomain {
    private static final String NAMING_PATTERN_REGEX = "^[a-zA-Z0-9-_]+$";

    @Inject
    DataDomain() {

    }

    public DataConnection create(DataConnection connection) {
        // Validate DataConnection name
        if (!Pattern.matches(NAMING_PATTERN_REGEX, connection.getName())) {
            throw new InputException("DataConnection's name is invalid");
        }

        DataConnection.Builder builder = connection.toBuilder()
                .setId(0L);

        if (connection.getType().equals(DataConnectionType.JSON_INGEST)) {
            builder = builder.setConfiguration(
                    JsonIngestDataConnectionConfiguration
                            .builder()
                            .setToken(TokenGenerator.generate())
                            .build()
            );
        }

        return builder
                .build();
    }

    public DataSeries create(
            DataSeries series,
            DataConnection dataConnection
    ) {
        series.getConfiguration()
                .visit(new DataSeriesConfiguration.ConsumerVisitor() {
                    @Override
                    public void onCsv(CsvDataSeriesConfiguration ignored) {
                        // No validation
                    }

                    @Override
                    public void onJdbc(JdbcDataSeriesConfiguration jdbcConfiguration) {
                        boolean nonSelectQuery = !jdbcConfiguration.getQuery()
                                .toLowerCase()
                                .startsWith("select");
                        if (nonSelectQuery) {
                            throw new InputException("Only SELECT queries are accepted");
                        }
                    }

                    @Override
                    public void onJson(JsonIngestDataSeriesConfiguration ignored) {
                        // No validation
                    }

                    @Override
                    public void onImportWorker(ImportWorkerDataSeriesConfiguration ignored) {
                        // No validation
                    }
                });
        return series.toBuilder()
                .setId(0L)
                .setName(series.getName())
                .setDescription(series.getDescription())
                .setDataConnection(dataConnection)
                .setTableName(
                        String.format(
                                "series_%s",
                                UUID.randomUUID().toString().replace('-', '_'))
                        )
                .setColumns(
                        series.getColumns()
                                .stream()
                                .map(column -> column.toBuilder()
                                        .setId(0L)
                                        .build()
                                )
                                .collect(Collectors.toList())
                )
                .build();
    }

    public DataSeries update(DataSeries oldDataSeries, DataSeries newDataSeries) {
        if (Objects.equals(oldDataSeries, newDataSeries)) {
            return oldDataSeries;
        }
        return oldDataSeries.toBuilder()
                .setTableName(newDataSeries.getTableName())
                .build();
    }

    public SeriesTable updateSeriesTable(SeriesTable oldSeriesTable, SeriesTable newSeriesTable) {
        if (Objects.equals(oldSeriesTable, newSeriesTable)) {
            return oldSeriesTable;
        }
        return oldSeriesTable.toBuilder()
                .setDeleteAt(newSeriesTable.getDeleteAt())
                .build();
    }

    public DataSeries updateSeriesProperties(
            DataSeries oldDataSeries, DataSeries newDataSeries
    ) {
        if (Objects.equals(oldDataSeries, newDataSeries)) {
            return oldDataSeries;
        }
        return oldDataSeries.toBuilder()
                .setDescription(newDataSeries.getDescription())
                .setName(newDataSeries.getName())
                .build();

    }

    public DataSeries updateSeriesColumns(
            DataSeries oldDataSeries, DataSeries newDataSeries
    ) {
        if (Objects.equals(oldDataSeries.getColumns(), newDataSeries.getColumns())) {
            return oldDataSeries;
        }
        return oldDataSeries.toBuilder()
                .setColumns(newDataSeries.getColumns())
                .build();

    }

    public DataConnection deleteDataConnection(DataConnection dataConnection) {
        return dataConnection.toBuilder()
                .setDeletedAt(Instant.now())
                .build();
    }

    public DataSeries deleteDataSeries(DataSeries dataSeries) {
        return dataSeries.toBuilder()
                .setDeletedAt(Instant.now())
                .build();
    }

    public SeriesTable deleteSeriesTable(SeriesTable seriesTable) {
        return seriesTable.toBuilder()
                .setDeleteAt(Instant.now())
                .build();
    }
}
