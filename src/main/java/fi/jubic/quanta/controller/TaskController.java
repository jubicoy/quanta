package fi.jubic.quanta.controller;

import fi.jubic.quanta.dao.AnomalyDao;
import fi.jubic.quanta.dao.DataSeriesDao;
import fi.jubic.quanta.dao.ImportWorkerDataSampleDao;
import fi.jubic.quanta.dao.InvocationDao;
import fi.jubic.quanta.dao.SeriesResultDao;
import fi.jubic.quanta.dao.TaskDao;
import fi.jubic.quanta.dao.TimeSeriesDao;
import fi.jubic.quanta.dao.WorkerDao;
import fi.jubic.quanta.domain.DataDomain;
import fi.jubic.quanta.domain.TaskDomain;
import fi.jubic.quanta.domain.TimeSeriesDomain;
import fi.jubic.quanta.domain.WorkerDomain;
import fi.jubic.quanta.exception.ApplicationException;
import fi.jubic.quanta.exception.AuthorizationException;
import fi.jubic.quanta.exception.InputException;
import fi.jubic.quanta.models.Anomaly;
import fi.jubic.quanta.models.AnomalyQuery;
import fi.jubic.quanta.models.Column;
import fi.jubic.quanta.models.DataSeries;
import fi.jubic.quanta.models.ImportWorkerDataSample;
import fi.jubic.quanta.models.Invocation;
import fi.jubic.quanta.models.InvocationQuery;
import fi.jubic.quanta.models.InvocationStatus;
import fi.jubic.quanta.models.Measurement;
import fi.jubic.quanta.models.OutputColumn;
import fi.jubic.quanta.models.Pagination;
import fi.jubic.quanta.models.SeriesResult;
import fi.jubic.quanta.models.SeriesResultQuery;
import fi.jubic.quanta.models.SeriesTable;
import fi.jubic.quanta.models.Task;
import fi.jubic.quanta.models.TaskQuery;
import fi.jubic.quanta.models.TaskType;
import fi.jubic.quanta.models.Worker;
import fi.jubic.quanta.models.WorkerQuery;
import fi.jubic.quanta.models.WorkerStatus;
import fi.jubic.quanta.util.DateUtil;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import org.jooq.Configuration;
import org.jooq.impl.DSL;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Singleton
public class TaskController {
    private final DataController dataController;
    private final TaskDomain taskDomain;
    private final TimeSeriesDomain timeSeriesDomain;
    private final WorkerDomain workerDomain;
    private final DataDomain dataDomain;

    private final AnomalyDao anomalyDao;
    private final InvocationDao invocationDao;
    private final SeriesResultDao seriesResultDao;
    private final TaskDao taskDao;
    private final TimeSeriesDao timeSeriesDao;
    private final WorkerDao workerDao;
    private final ImportWorkerDataSampleDao importWorkerDataSampleDao;
    private final DataSeriesDao dataSeriesDao;

    private final Configuration conf;

    @Inject
    TaskController(
            DataController dataController,
            TaskDomain taskDomain,
            TimeSeriesDomain timeSeriesDomain,
            WorkerDomain workerDomain,
            DataDomain dataDomain,
            AnomalyDao anomalyDao,
            InvocationDao invocationDao,
            SeriesResultDao seriesResultDao,
            TaskDao taskDao,
            TimeSeriesDao timeSeriesDao,
            WorkerDao workerDao,
            ImportWorkerDataSampleDao importWorkerDataSampleDao,
            DataSeriesDao dataSeriesDao,
            fi.jubic.quanta.config.Configuration configuration
    ) {
        this.dataController = dataController;
        this.taskDomain = taskDomain;
        this.timeSeriesDomain = timeSeriesDomain;
        this.workerDomain = workerDomain;
        this.dataDomain = dataDomain;
        this.anomalyDao = anomalyDao;
        this.invocationDao = invocationDao;
        this.seriesResultDao = seriesResultDao;
        this.taskDao = taskDao;
        this.timeSeriesDao = timeSeriesDao;
        this.workerDao = workerDao;
        this.importWorkerDataSampleDao = importWorkerDataSampleDao;
        this.dataSeriesDao = dataSeriesDao;

        this.conf = configuration.getJooqConfiguration().getConfiguration();
    }

    public List<Task> search(TaskQuery query) {
        return taskDao.search(query);
    }

    public Optional<Task> getDetails(Long taskId) {
        return taskDao.getDetails(taskId);
    }


    public Task create(Task task) {
        return taskDao.create(
                taskDomain.create(task)
        );
    }

    public Task update(Task task) {
        taskDao.getDetails(task.getId())
                .orElseThrow(() -> new ApplicationException("Task does not exist"));

        return taskDao.update(
                task.getId(),
                optionalTask -> taskDomain.update(
                        optionalTask.orElseThrow(
                                () -> new ApplicationException("Can't update non-existing Task")
                        ),
                        task
                )
        );
    }

    public List<Invocation> searchInvocations(InvocationQuery query) {
        return invocationDao.search(query);
    }

    public Invocation invoke(Long taskId) {
        Task task = taskDao.getDetails(taskId)
                .orElseThrow(() -> new ApplicationException("Task does not exist"));

        Invocation invocation = Invocation.invoke(task);
        if (Objects.nonNull(task.getWorkerDef())) {
            var worker = workerDao
                    .search(
                            new WorkerQuery()
                                    .withWorkerDefId(task.getWorkerDef().getId())
                                    .withStatus(WorkerStatus.Accepted)
                                    .withNotDeleted(true)
                    )
                    .stream()
                    .findFirst()
                    .orElseThrow(
                            () -> new ApplicationException(
                                    "Could not invoke task: No available Workers."
                            )
                    );
            invocation = invocation.toBuilder()
                    .setWorker(worker)
                    .build();
        }

        return invocationDao.create(invocation);
    }

    public Optional<Invocation> getInvocationDetails(Long id) {
        return invocationDao.getDetails(id)
                .map(Invocation::toBuilder)
                .map(builder -> builder.setSeriesResults(
                        seriesResultDao.search(new SeriesResultQuery().withInvocationId(id))
                ))
                .map(builder -> builder.setDetectionResults(
                        anomalyDao.search(new AnomalyQuery().withInvocationId(id))
                ))
                .map(Invocation.Builder::build);
    }

    public Invocation getInvocationDetails(
            Long id,
            String workerToken
    ) {
        Invocation invocation = getInvocationDetails(id)
                .orElseThrow(NotFoundException::new);

        TaskType taskType = invocation.getTask().getTaskType();

        //the import tasks can have empty column selectors
        if (!taskType.equals(TaskType.IMPORT_SAMPLE) && !taskType.equals(TaskType.IMPORT)) {
            invocation = getInvocationDetails(id)
                    .filter(inv -> inv.getColumnSelectors().size() > 0)
                    .orElseThrow(NotFoundException::new);
        }

        if (!invocation.getWorker().getToken().equals(workerToken)) {
            throw new AuthorizationException("Authorization Worker token is not same as "
                    + "the Invocation's worker token ");
        }
        checkWorkerAuthorization(workerToken);

        return invocation;
    }


    public Optional<Invocation> getNextInvocation(String workerToken) {
        checkWorkerAuthorization(workerToken);

        return invocationDao
                .search(
                        new InvocationQuery()
                                .withWorkerToken(workerToken)
                                .withStatus(InvocationStatus.Pending),
                        new Pagination(1, 0)
                )
                .stream()
                .findFirst()
                .map(Invocation::getId)
                .flatMap(this::getInvocationDetails);
    }

    public void updateInvocationStatus(
            Invocation invocation,
            InvocationStatus status
    ) {
        invocationDao.update(
                invocation.getId(),
                optionalInvocation -> taskDomain.updateInvocationStatus(
                        optionalInvocation.orElseThrow(
                                () -> new InputException("Can't update non-existing Invocation")
                        ),
                        status
                )
        );
        // Trigger downstream tasks if invocation is completed
        if (status.equals(InvocationStatus.Completed)) {
            taskDao.search(
                    new TaskQuery().withTriggeredById(invocation.getTask().getId())
                            .withNotDeleted(true)
            ).forEach(task -> invoke(task.getId()));
        }
    }

    private DataSeries syncReplace(
            DataSeries dataSeries,
            List<OutputColumn> outputColumn,
            Configuration transaction
    ) {
        DataSeries dataSeriesWithNewTableName = dataSeries.toBuilder()
                .setTableName(
                        String.format(
                                "series_%s",
                                UUID.randomUUID().toString().replace('-', '_')
                        )
                )
                .build();

        SeriesTable table = SeriesTable
                .builder()
                .setId(-1L)
                .setTableName(dataSeriesWithNewTableName.getTableName())
                .setDataSeries(dataSeriesWithNewTableName)
                .build();

        // Creating new table
        timeSeriesDao.createTableWithOutputColumns(
                table,
                outputColumn,
                transaction
        );

        // Updating the old SeriesTable with deleteAt
        dataController.updateSeriesTableWithDeleteAt(
                dataSeries.getTableName(),
                transaction
        );

        // Updating the dataSeries with new table
        return dataSeriesDao.update(
                dataSeries.getId(),
                optionalSeries -> dataDomain.update(
                        optionalSeries.orElseThrow(
                                () -> new ApplicationException(
                                        "Can't update non-existing DataSeries"
                                )
                        ),
                        dataSeriesWithNewTableName
                ),
                transaction
        );
    }

    public void storeSeriesResult(
            Invocation invocation,
            List<Measurement> measurements
    ) {
        if (invocation.getTask().getTaskType().equals(TaskType.IMPORT)) {
            //TODO - get series columns straight from invocation
            List<OutputColumn> outputColumns = convertSeriesColumnsToOutputColumns(
                    dataSeriesDao.getDetails(
                            Objects.requireNonNull(invocation.getTask().getSeries().getId())
                            )
                            .orElseThrow(NotFoundException::new)
                            .getColumns()
            );

            List<Measurement> newMeasurements = new ArrayList<>();

            DataSeries createdSeries = Objects.requireNonNull(
                        invocation.getTask().getSeries()
            );

            if (invocation.getTask().getSyncIntervalOffset() != null) {
                newMeasurements.addAll(measurements.stream()
                        .filter(measurement -> measurement.getTime()
                                .isAfter(Instant.now().minusSeconds(
                                        invocation.getTask().getSyncIntervalOffset()
                                )))
                        .filter(measurement -> measurement.getTime()
                                .isBefore(Instant.now()))
                        .collect(Collectors.toList()));
            }
            else {
                newMeasurements.addAll(measurements);
            }

            //if offset != null, we replace data within that offset...
            if (invocation.getTask().getSyncIntervalOffset() != null) {
                Instant deleteEdge = newMeasurements.stream()
                        .map(Measurement::getTime)
                        .min(Instant::compareTo)
                        .orElseThrow();

                DateTimeFormatter dateTimeFormatter = invocation.getColumnSelectors().size() == 0
                        ? DateUtil.dateTimeFormatter(null)
                        : DateUtil.dateTimeFormatter(
                        invocation.getColumnSelectors().get(0).getType().getFormat()
                );

                DSL.using(conf).transaction(transaction -> {
                    timeSeriesDao.deleteRowsWithTableName(
                            createdSeries.getTableName(),
                            "0",
                            deleteEdge,
                            Instant.from(Instant.now()),
                            dateTimeFormatter,
                            transaction
                    );

                    timeSeriesDao.insertDataWithOutputColumns(
                            createdSeries,
                            outputColumns,
                            timeSeriesDomain.convertFromMeasurement(
                                    invocation,
                                    newMeasurements
                            ),
                            transaction
                    );
                });
            }
            //...and if offset == null we replace everything
            else {
                DSL.using(conf).transaction(transaction -> {
                    DataSeries intermediateUpdatedDataSeries = syncReplace(
                            createdSeries,
                            outputColumns,
                            transaction
                    );

                    timeSeriesDao.insertDataWithOutputColumns(
                            intermediateUpdatedDataSeries,
                            outputColumns,
                            timeSeriesDomain.convertFromMeasurement(
                                    invocation,
                                    newMeasurements
                            ),
                            transaction
                    );

                    importWorkerDataSampleDao.putSample(invocation.getId(),
                            ImportWorkerDataSample.builder()
                                    .setColumns(Collections.emptyList())
                                    .setErrorFlag(false)
                                    .setData(
                                            timeSeriesDomain.convertFromMeasurement(
                                                    invocation,
                                                    newMeasurements
                                            ).collect(Collectors.toList())
                                    )
                                    .build());
                });
            }
        }
        else {
            SeriesResult result = seriesResultDao
                    .search(new SeriesResultQuery().withInvocationId(invocation.getId()))
                    .stream()
                    .findFirst()
                    .orElseGet(() -> DSL.using(conf).transactionResult(transaction -> {
                        SeriesResult intermediateResult = seriesResultDao.create(
                                taskDomain.createSeriesResult(invocation),
                                transaction
                        );

                        timeSeriesDao.createTableWithOutputColumns(
                                intermediateResult,
                                invocation.getOutputColumns(),
                                transaction
                        );

                        return intermediateResult;
                    }));

            timeSeriesDao.insertDataWithOutputColumns(
                    result,
                    invocation.getOutputColumns(),
                    timeSeriesDomain.convertFromMeasurement(
                            invocation,
                            measurements
                    )
            );
        }
    }

    public void storeAnomalyResult(
            Invocation invocation,
            List<Anomaly> anomalies
    ) {
        anomalyDao.create(
                anomalies,
                invocation
        );
    }

    private void checkWorkerAuthorization(String workerToken) {
        Worker worker = workerDao.getDetailsByToken(workerToken)
                .orElse(null);
        workerDomain.checkAuthorization(worker);
        workerDao.update(
                Objects.requireNonNull(worker).getId(),
                optionalWorker -> workerDomain.updateLastSeen(
                        optionalWorker.orElseThrow(IllegalStateException::new)
                )
        );
    }

    public Task delete(Long id) {
        return taskDao.update(
                id,
                task -> taskDomain.delete(
                        task.orElseThrow(
                                () -> new InputException("Can't delete a non-existing Task")
                        )
                )
        );
    }

    public Response submitDataSample(
            Invocation invocation,  ImportWorkerDataSample sample
    ) {
        if (invocation.getTask().getTaskType().equals(TaskType.IMPORT_SAMPLE)) {

            importWorkerDataSampleDao.putSample(invocation.getId(), sample);

            return Response.ok().build();
        }

        return null;
    }

    public List<OutputColumn> convertSeriesColumnsToOutputColumns(
            List<Column> seriesColumns
    ) {
        List<OutputColumn> outputColumns = new ArrayList<>();

        seriesColumns.forEach(column ->
                outputColumns.add(
                        OutputColumn.builder()
                                .setIndex(column.getIndex())
                                .setType(column.getType())
                                .setId(column.getId())
                                .setColumnName(column.getName())
                                .setAlias(column.getName())
                                .build()
                )
        );

        return outputColumns;
    }
}
