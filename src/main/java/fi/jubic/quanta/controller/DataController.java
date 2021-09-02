package fi.jubic.quanta.controller;

import fi.jubic.quanta.config.Configuration;
import fi.jubic.quanta.dao.DataConnectionDao;
import fi.jubic.quanta.dao.DataSeriesDao;
import fi.jubic.quanta.dao.InvocationDao;
import fi.jubic.quanta.dao.SeriesTableDao;
import fi.jubic.quanta.dao.TaskDao;
import fi.jubic.quanta.dao.TimeSeriesDao;
import fi.jubic.quanta.domain.DataDomain;
import fi.jubic.quanta.domain.TaskDomain;
import fi.jubic.quanta.exception.ApplicationException;
import fi.jubic.quanta.exception.InputException;
import fi.jubic.quanta.external.Importer;
import fi.jubic.quanta.external.Ingester;
import fi.jubic.quanta.models.DataConnection;
import fi.jubic.quanta.models.DataConnectionQuery;
import fi.jubic.quanta.models.DataConnectionType;
import fi.jubic.quanta.models.DataSample;
import fi.jubic.quanta.models.DataSeries;
import fi.jubic.quanta.models.DataSeriesQuery;
import fi.jubic.quanta.models.Invocation;
import fi.jubic.quanta.models.SeriesTable;
import fi.jubic.quanta.models.Task;
import fi.jubic.quanta.models.TaskQuery;
import fi.jubic.quanta.models.metadata.DataConnectionMetadata;
import fi.jubic.quanta.models.typemetadata.TypeMetadata;
import fi.jubic.quanta.util.DateUtil;
import org.jooq.impl.DSL;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.NotFoundException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Singleton
public class DataController {
    private final DataConnectionDao dataConnectionDao;
    private final DataSeriesDao dataSeriesDao;
    private final InvocationDao invocationDao;
    private final SeriesTableDao seriesTableDao;
    private final TaskDao taskDao;
    private final TimeSeriesDao timeSeriesDao;

    private final Importer importer;
    private final Ingester ingester;

    private final DataDomain dataDomain;
    private final TaskDomain taskDomain;

    private final Configuration configuration;
    private final org.jooq.Configuration conf;

    @Inject
    DataController(
            DataConnectionDao dataConnectionDao,
            DataSeriesDao dataSeriesDao,
            InvocationDao invocationDao,
            SeriesTableDao seriesTableDao,
            TaskDao taskDao,
            TimeSeriesDao timeSeriesDao,
            Importer importer,
            Ingester ingester,
            DataDomain dataDomain,
            TaskDomain taskDomain,
            Configuration configuration
    ) {
        this.dataConnectionDao = dataConnectionDao;
        this.dataSeriesDao = dataSeriesDao;
        this.invocationDao = invocationDao;
        this.seriesTableDao = seriesTableDao;
        this.taskDao = taskDao;
        this.timeSeriesDao = timeSeriesDao;
        this.importer = importer;
        this.ingester = ingester;
        this.dataDomain = dataDomain;
        this.taskDomain = taskDomain;
        this.conf = configuration.getJooqConfiguration().getConfiguration();
        this.configuration = configuration;
    }

    public List<DataConnection> searchConnections(DataConnectionQuery query) {
        return dataConnectionDao.search(query)
                .stream()
                .collect(Collectors.toList());
    }

    public Optional<DataConnection> getConnectionDetails(Long connectionId) {
        return dataConnectionDao.getDetails(connectionId);
    }

    public Optional<DataSeries> getDataSeriesDetails(Long seriesId) {
        return dataSeriesDao.getDetails(seriesId);
    }

    public Optional<DataConnectionMetadata> getConnectionMetadata(Long connectionId) {
        return getConnectionDetails(connectionId)
                .map(importer::getConnectionMetadata);
    }

    public DataConnection create(DataConnection dataConnection) {
        return dataConnectionDao.create(
                        importer.validate(
                                dataDomain.create(dataConnection)
                        )
        );
    }

    public DataSeries create(
            Long dataConnectionId,
            DataSeries dataSeries,
            boolean skipImportData
    ) {
        DataConnection dataConnection = dataConnectionDao.getDetails(dataConnectionId)
                .orElseThrow(NotFoundException::new);


        DataSeries createdSeries = DSL.using(conf).transactionResult(transaction -> {
            DataSeries series = dataSeriesDao.create(
                    dataDomain.create(
                            dataSeries,
                            dataConnection
                    ),
                    transaction
            );

            timeSeriesDao.createTable(
                    series,
                    transaction
            );

            return series;
        });

        if (!skipImportData) {
            Task.syncTask(createdSeries)
                    .map(taskDao::create)
                    .ifPresent(task -> invocationDao.create(Invocation.invoke(task)));
        }

        return createdSeries;
    }

    public DataConnection updateDataConnection(DataConnection dataConnection) {
        DataConnection updatedDataConnection = dataConnectionDao.update(
                dataConnection.getId(),
                optionalDataConnection -> dataDomain.updateDataConnection(
                        optionalDataConnection.orElseThrow(
                                () -> new ApplicationException(
                                        "Can't update non-existing DataConnection"
                                )
                        ),
                        dataConnection
                )
        );
        return updatedDataConnection;
    }

    public void sync(DataSeries dataSeries, Task task) {
        if (dataSeries.getType().equals(DataConnectionType.JSON_INGEST)
                || dataSeries.getType().equals(DataConnectionType.CSV)) {
            throw new ApplicationException("Unable to update DataSeries with DataConnectionType "
                    + "of JSON_INGEST / CVS ");
        }
        // Creating and importing data to new table before updating the dataSeries in case
        // the either of the operations fails
        DataSeries existingSeries = getSeriesDetailsByName(dataSeries.getName())
                .orElseThrow(NotFoundException::new);

        if (task.getSyncIntervalOffset() != null) {
            DateTimeFormatter dateTimeFormatter = dataSeries.getColumns().isEmpty()
                    ? DateUtil.dateTimeFormatter(null)
                    : DateUtil.dateTimeFormatter(
                            dataSeries.getColumns().get(0).getType().getFormat()
            );
            Instant now = Instant.now();
            syncIncremental(
                    existingSeries,
                    now.minusSeconds(task.getSyncIntervalOffset()),
                    now,
                    dateTimeFormatter
            );
        }
        else {
            syncReplace(existingSeries);
        }
    }

    private void syncReplace(DataSeries dataSeries) {
        DataSeries dataSeriesWithNewTableName = dataSeries.toBuilder()
                .setTableName(
                        String.format(
                                "series_%s",
                                UUID.randomUUID().toString().replace('-', '_')
                        )
                )
                .build();

        // Creating and importing data to new table
        timeSeriesDao.createTable(
                dataSeriesWithNewTableName,
                conf
        );
        importer.getRows(
                dataSeriesWithNewTableName,
                batch -> timeSeriesDao.insertData(
                        dataSeriesWithNewTableName,
                        batch
                )
        ).join();

        // Updating the dataSeries with new table
        dataSeriesDao.update(
                dataSeries.getId(),
                optionalSeries -> dataDomain.update(
                        optionalSeries.orElseThrow(
                                () -> new ApplicationException(
                                        "Can't update non-existing DataSeries"
                                )
                        ),
                        dataSeriesWithNewTableName
                )
        );

        // Updating the old SeriesTable with deleteAt
        updateSeriesTableWithDeleteAt(
                dataSeries.getTableName()
        );
    }

    private void syncIncremental(
            DataSeries dataSeries,
            Instant start,
            Instant end,
            DateTimeFormatter dateTimeFormatter
    ) {
        DSL.using(conf).transaction(transaction -> {
            timeSeriesDao.deleteRowsWithTableName(
                    dataSeries.getTableName(),
                    "0",
                    start,
                    end,
                    dateTimeFormatter,
                    transaction
            );

            importer.getRows(
                    dataSeries,
                    batch -> timeSeriesDao.insertData(
                            dataSeries,
                            batch,
                            transaction
                    ),
                    start,
                    end
            ).join();
        });
    }

    public List<DataSeries> searchSeries(DataSeriesQuery query) {
        return dataSeriesDao.search(query);
    }

    public Optional<DataSeries> getSeriesDetailsByName(String name) {
        return dataSeriesDao.getDetailsByName(name);
    }

    public long ingestData(String dataConnectionToken, Object payload) {
        DataSeries dataSeries = Objects
                .requireNonNull(
                        dataConnectionDao
                                .getDetailsByToken(dataConnectionToken)
                                .orElseThrow(NotFoundException::new)
                                .getSeries()
                )
                .get(0);
        // TODO: Implement multiple DataSeries

        switch (dataSeries.getType()) {
            case CSV:
            case IMPORT_WORKER:
            case JDBC:
                throw new UnsupportedOperationException();

            case JSON_INGEST:
                return timeSeriesDao.insertData(
                        dataSeries,
                        ingester.getIngestRows(dataSeries, payload).stream()
                );

            default:
                throw new IllegalStateException("Invalid DataSeries type");
        }
    }

    public boolean test(
            DataConnection dataConnection
    ) {
        if (dataConnection == null) {
            throw new InputException("Invalid DataConnection");
        }
        return importer.test(
                dataDomain.create(dataConnection)
        );
    }

    public DataSample getSample(
            Long dataConnectionId,
            DataSeries dataSeries
    ) {
        DataConnection dataConnection = dataConnectionDao.getDetails(dataConnectionId)
                .orElseThrow(() -> new InputException("No such DataConnection"));

        return importer.getSample(
                dataDomain.create(
                        dataSeries,
                        dataConnection
                ),
                5
        );
    }

    public List<List<String>> getResult(
            DataSeries dataSeries
    ) {
        if (dataSeries.getType().equals(DataConnectionType.IMPORT_WORKER)) {
            return importer.getRows(dataSeries).collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    public TypeMetadata getMetadata(String type) {
        DataConnectionType dataConnectionType = DataConnectionType.valueOf(type.toUpperCase());
        return importer.getMetadata(dataConnectionType);
    }

    public void cleanupDeletedTables() {
        seriesTableDao.getDeletedTables()
                .forEach(seriesTable -> DSL.using(conf)
                        .transaction(transaction -> {
                            timeSeriesDao.deleteTableWithTableName(
                                    seriesTable.getTableName(),
                                    transaction
                            );

                            seriesTableDao.deleteWithTableName(
                                    seriesTable.getTableName(),
                                    transaction
                            );
                        })
                );
    }

    public SeriesTable updateSeriesTableWithDeleteAt(
            String tableName
    ) {
        SeriesTable seriesTable = seriesTableDao
                .getSeriesTableByName(tableName, conf)
                .orElseThrow(NotFoundException::new);

        return seriesTableDao.update(
                seriesTable.getTableName(),
                optionalSeries -> dataDomain.updateSeriesTable(
                        optionalSeries.orElseThrow(
                                () -> new ApplicationException(
                                        "Can't update non-existing SeriesTable"
                                )
                        ),
                        seriesTable
                                .toBuilder()
                                .setDeleteAt(Instant.now().plusSeconds(
                                        configuration.getPersistOldSeriesTables()
                                ))
                                .build()
                )
        );
    }

    public DataConnection delete(Long dataConnectionId) {
        DataConnection deletedDataConnection = dataConnectionDao.update(
                dataConnectionId,
                dataConnection -> dataDomain.deleteDataConnection(
                        dataConnection.orElseThrow(
                                () -> new InputException(
                                        "Can't delete a non-existing DataConnection"
                                )
                        )
                )
        );

        List<Task> dataConnectionTasks = taskDao.search(
                new TaskQuery().withConnectionId(deletedDataConnection.getId())
                        .withNotDeleted(true)
        );

        dataConnectionTasks.forEach(
                dataConnectionTask -> taskDao.update(
                        dataConnectionTask.getId(),
                        task -> taskDomain.delete(
                                task.orElseThrow(
                                        () -> new InputException(
                                                "Can't delete a non-existing Task"
                                        )
                                )
                        )
                )
        );

        return deletedDataConnection;
    }

    public DataSeries deleteSeries(Long dataSeriesId) {
        DataSeries deletedDataSeries = dataSeriesDao.update(
                dataSeriesId,
                dataSeries -> dataDomain.deleteDataSeries(
                        dataSeries.orElseThrow(
                                () -> new InputException(
                                        "Can't delete a non-existing DataSeries"
                                )
                        )
                )
        );

        List<Task> dataSeriesTasks = taskDao.search(
                new TaskQuery().withDataSeriesId(deletedDataSeries.getId())
                        .withNotDeleted(true)
        );

        dataSeriesTasks.forEach(
                dataSeriesTask -> taskDao.update(
                        dataSeriesTask.getId(),
                        task -> taskDomain.delete(
                                task.orElseThrow(
                                        () -> new InputException(
                                                "Can't delete a non-existing Task"
                                        )
                                )
                        )
                )
        );

        seriesTableDao.update(
                deletedDataSeries.getTableName(),
                seriesTable -> dataDomain.deleteSeriesTable(
                        seriesTable.orElseThrow(
                                () -> new InputException(
                                        "Can't delete a non-existing SeriesTable"
                                )
                        )
                )
        );

        return deletedDataSeries;
    }
}
