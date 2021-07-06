package fi.jubic.quanta.controller;

import fi.jubic.quanta.config.Configuration;
import fi.jubic.quanta.dao.DataConnectionDao;
import fi.jubic.quanta.dao.DataSeriesDao;
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
import fi.jubic.quanta.models.SeriesTable;
import fi.jubic.quanta.models.Task;
import fi.jubic.quanta.models.TaskQuery;
import fi.jubic.quanta.models.metadata.DataConnectionMetadata;
import fi.jubic.quanta.models.typemetadata.TypeMetadata;
import fi.jubic.quanta.scheduled.CronRegistration;
import fi.jubic.quanta.scheduled.SingleTriggerJob;
import org.jooq.impl.DSL;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.NotFoundException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public class DataController {
    private final TimeSeriesDao timeSeriesDao;
    private final DataConnectionDao dataConnectionDao;
    private final DataSeriesDao dataSeriesDao;
    private final SeriesTableDao seriesTableDao;
    private final TaskDao taskDao;

    private final Importer importer;
    private final Ingester ingester;

    private final SchedulerController schedulerController;

    private final DataDomain dataDomain;
    private final TaskDomain taskDomain;

    private final Configuration configuration;
    private final org.jooq.Configuration conf;

    @Inject
    DataController(
            TimeSeriesDao timeSeriesDao,
            DataConnectionDao dataConnectionDao,
            DataSeriesDao dataSeriesDao,
            SeriesTableDao seriesTableDao,
            TaskDao taskDao,
            Importer importer,
            Ingester ingester,
            SchedulerController schedulerController,
            DataDomain dataDomain,
            TaskDomain taskDomain,
            Configuration configuration
    ) {
        this.timeSeriesDao = timeSeriesDao;
        this.dataConnectionDao = dataConnectionDao;
        this.dataSeriesDao = dataSeriesDao;
        this.seriesTableDao = seriesTableDao;
        this.taskDao = taskDao;
        this.importer = importer;
        this.ingester = ingester;
        this.schedulerController = schedulerController;
        this.dataDomain = dataDomain;
        this.taskDomain = taskDomain;
        this.conf = configuration.getJooqConfiguration().getConfiguration();
        this.configuration = configuration;
    }

    public List<DataConnection> searchConnections(DataConnectionQuery query) {
        return dataConnectionDao.search(query)
                .stream()
                .map(importer::getWithEmptyLogin)
                .collect(Collectors.toList());
    }

    public Optional<DataConnection> getConnectionDetails(Long connectionId) {
        return dataConnectionDao.getDetails(connectionId);
    }

    public Optional<DataConnection> getConnectionDetailsWithEmptyLogin(Long connectionId) {
        return dataConnectionDao.getDetails(connectionId)
                .map(importer::getWithEmptyLogin);
    }

    public Optional<DataConnectionMetadata> getConnectionMetadata(Long connectionId) {
        return getConnectionDetails(connectionId)
                .map(importer::getConnectionMetadata);
    }

    public DataConnection create(DataConnection dataConnection) {
        return importer.getWithEmptyLogin(
                dataConnectionDao.create(
                        importer.validate(
                                dataDomain.create(dataConnection)
                        )
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

        if (!createdSeries.getType().equals(DataConnectionType.JSON_INGEST) && !skipImportData) {
            // JSON_INGEST DataSeries has no initial rows
            importData(createdSeries);
        }

        return createdSeries;
    }

    public void update(
            Map<String, CronRegistration> cronTasksWithNames,
            Map<String, SingleTriggerJob> runningOrPendingDataSyncJobs,
            DataSeries dataSeries
    ) {
        if (dataSeries.getType().equals(DataConnectionType.JSON_INGEST)
                || dataSeries.getType().equals(DataConnectionType.CSV)) {
            throw new ApplicationException("Unable to update DataSeries with DataConnectionType "
                    + "of JSON_INGEST / CVS ");
        }
        // Creating and importing data to new table before updating the dataSeries in case
        // the either of the operations fails
        DataSeries existingSeries = getSeriesDetailsByName(dataSeries.getName())
                .orElseThrow(NotFoundException::new);
        DataSeries dataSeriesWithNewTableName = existingSeries.toBuilder()
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
        importData(dataSeriesWithNewTableName);

        // Updating the dataSeries with new table
        dataSeriesDao.update(
                existingSeries.getId(),
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
        SeriesTable updatedSeriesTable = updateSeriesTableWithDeleteAt(
                existingSeries.getTableName()
        );

        // Scheduling the deletion of old data series table
        schedulerController.scheduleSingleTriggerJob(
                cronTasksWithNames,
                Stream.concat(
                        runningOrPendingDataSyncJobs
                                .entrySet()
                                .stream(),
                        getSeriesTablesDeleteJobs()
                                .entrySet()
                                .stream()
                ).collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue
                        )
                ),
                SingleTriggerJob.of(
                        updatedSeriesTable.getDeleteAt(),
                        deleteTableTask(updatedSeriesTable.getTableName()),
                        getDeleteJobName(updatedSeriesTable)
                )
        );

    }

    public List<DataSeries> searchSeries(DataSeriesQuery query) {
        return dataSeriesDao.search(query);
    }

    public Optional<DataSeries> getSeriesDetailsByName(String name) {
        return dataSeriesDao.getDetailsByName(name);
    }

    // TODO: Make public and call externally
    private void importData(DataSeries dataSeries) {
        try (Stream<List<String>> stream = importer.getRows(dataSeries)) {
            timeSeriesDao.insertData(
                    dataSeries,
                    stream
            );
        }
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

        if (dataSeries.getType().equals(DataConnectionType.IMPORT_WORKER)) {
            return importer.getSample(dataSeries, 5);
        }

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


    private void deleteTables(String tableName) {
        timeSeriesDao.deleteTableWithTableName(
                tableName,
                conf
        );

        seriesTableDao.deleteWithTableName(
                tableName,
                conf
        );
    }

    public fi.jubic.easyschedule.Task deleteTableTask(String tableName) {
        return () -> {
            deleteTables(tableName);
        };
    }

    public Map<String, SingleTriggerJob> createMapOfSingleTriggerJobs(
            Stream<SeriesTable> seriesTableStream
    ) {
        return seriesTableStream.map(seriesTable ->
                SingleTriggerJob.of(
                        seriesTable.getDeleteAt(),
                        deleteTableTask(seriesTable.getTableName()),
                        getDeleteJobName(seriesTable)
                )
        )
                .collect(
                        Collectors.toMap(
                                SingleTriggerJob::getJobName,
                                registration -> registration
                        )
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
                dataConnectionTask -> {
                    Task deletedTask = taskDao.update(
                            dataConnectionTask.getId(),
                            task -> taskDomain.delete(
                                    task.orElseThrow(
                                            () -> new InputException(
                                                    "Can't delete a non-existing Task"
                                            )
                                    )
                            )
                    );
                    if (Objects.nonNull(deletedTask.getCronTrigger())) {
                        schedulerController.deleteTask(
                                deletedTask
                        );
                    }
                }
        );

        return deletedDataConnection;
    }

    public Map<String, SingleTriggerJob> getSeriesTablesDeleteJobs() {
        return createMapOfSingleTriggerJobs(
                seriesTableDao.getSeriesTablesHasDeleteAt(conf).stream()
        );
    }

    public String getDeleteJobName(
            SeriesTable seriesTable
    ) {
        return "delete-" + seriesTable.getTableName();
    }
}
