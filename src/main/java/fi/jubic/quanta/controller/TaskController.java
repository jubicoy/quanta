package fi.jubic.quanta.controller;

import fi.jubic.quanta.dao.AnomalyDao;
import fi.jubic.quanta.dao.ImportWorkerDataSampleDao;
import fi.jubic.quanta.dao.InvocationDao;
import fi.jubic.quanta.dao.SeriesResultDao;
import fi.jubic.quanta.dao.SeriesTableDao;
import fi.jubic.quanta.dao.TaskDao;
import fi.jubic.quanta.dao.TimeSeriesDao;
import fi.jubic.quanta.dao.WorkerDao;
import fi.jubic.quanta.domain.TaskDomain;
import fi.jubic.quanta.domain.TimeSeriesDomain;
import fi.jubic.quanta.domain.WorkerDomain;
import fi.jubic.quanta.exception.ApplicationException;
import fi.jubic.quanta.exception.AuthorizationException;
import fi.jubic.quanta.exception.InputException;
import fi.jubic.quanta.models.Anomaly;
import fi.jubic.quanta.models.AnomalyQuery;
import fi.jubic.quanta.models.ColumnSelector;
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
import fi.jubic.quanta.models.WorkerDefColumn;
import fi.jubic.quanta.models.WorkerQuery;
import fi.jubic.quanta.models.WorkerStatus;
import fi.jubic.quanta.scheduled.CronRegistration;
import fi.jubic.quanta.scheduled.SingleTriggerJob;
import org.jooq.Configuration;
import org.jooq.impl.DSL;
import org.quartz.CronExpression;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public class TaskController {
    private final TaskDomain taskDomain;
    private final TimeSeriesDomain timeSeriesDomain;
    private final WorkerDomain workerDomain;

    private final AnomalyDao anomalyDao;
    private final InvocationDao invocationDao;
    private final SeriesResultDao seriesResultDao;
    private final TaskDao taskDao;
    private final TimeSeriesDao timeSeriesDao;
    private final WorkerDao workerDao;
    private final ImportWorkerDataSampleDao importWorkerDataSampleDao;
    private final SeriesTableDao seriesTableDao;

    private final DataController dataController;
    private final SchedulerController schedulerController;

    private final Configuration conf;

    @Inject
    TaskController(
            TaskDomain taskDomain,
            TimeSeriesDomain timeSeriesDomain,
            WorkerDomain workerDomain,
            AnomalyDao anomalyDao,
            InvocationDao invocationDao,
            SeriesResultDao seriesResultDao,
            TaskDao taskDao,
            TimeSeriesDao timeSeriesDao,
            WorkerDao workerDao,
            ImportWorkerDataSampleDao importWorkerDataSampleDao,
            SeriesTableDao seriesTableDao,
            DataController dataController,
            SchedulerController schedulerController,
            fi.jubic.quanta.config.Configuration configuration
    ) {
        this.taskDomain = taskDomain;
        this.timeSeriesDomain = timeSeriesDomain;
        this.workerDomain = workerDomain;
        this.anomalyDao = anomalyDao;
        this.invocationDao = invocationDao;
        this.seriesResultDao = seriesResultDao;
        this.taskDao = taskDao;
        this.timeSeriesDao = timeSeriesDao;
        this.workerDao = workerDao;
        this.importWorkerDataSampleDao = importWorkerDataSampleDao;
        this.seriesTableDao = seriesTableDao;

        this.dataController = dataController;
        this.schedulerController = schedulerController;
        this.conf = configuration.getJooqConfiguration().getConfiguration();
    }

    public List<Task> search(TaskQuery query) {
        return taskDao.search(query);
    }

    public Optional<Task> getDetails(Long taskId) {
        return taskDao.getDetails(taskId);
    }


    public Task create(Task task) {
        Task createdTask = taskDao.create(
                taskDomain.create(task)
        );

        if (Objects.nonNull(createdTask.getCronTrigger())) {
            schedulerController.scheduleTask(
                    getCronTasksWithNames(),
                    dataController.getSeriesTablesDeleteJobs(),
                    createdTask
            );
        }

        return createdTask;
    }

    public Task update(Task task) {
        Task existingTask = taskDao.getDetails(task.getId())
                .orElseThrow(() -> new ApplicationException("Task does not exist"));

        Task updatedTask = taskDao.update(
                task.getId(),
                optionalTask -> taskDomain.update(
                        optionalTask.orElseThrow(
                                () -> new ApplicationException("Can't update non-existing Task")
                        ),
                        task
                )
        );
        // Reschedule task if cron trigger has been changed
        if (Objects.nonNull(existingTask.getCronTrigger())
                && Objects.nonNull(updatedTask.getCronTrigger())
                && !Objects.equals(existingTask.getCronTrigger(), updatedTask.getCronTrigger())
        ) {
            if (!CronExpression.isValidExpression(updatedTask.getCronTrigger())) {
                throw new InputException("Cron expression is invalid ");
            }

            schedulerController.deleteTask(
                    existingTask
            );
            schedulerController.scheduleTask(
                    getCronTasksWithNames(),
                    getAllSingleTriggerJobs(),
                    updatedTask
            );
        }

        // Unscheduling the task that has cron-trigger
        if (Objects.nonNull(existingTask.getCronTrigger())
                && Objects.isNull(updatedTask.getCronTrigger())) {
            schedulerController.deleteTask(
                    getCronTasksWithNames(),
                    getAllSingleTriggerJobs(),
                    existingTask
            );
        }
        // Scheduling the task with cron-trigger
        if (Objects.isNull(existingTask.getCronTrigger())
                && Objects.nonNull(updatedTask.getCronTrigger())) {
            if (!CronExpression.isValidExpression(updatedTask.getCronTrigger())) {
                throw new InputException("Cron expression is invalid ");
            }

            schedulerController.scheduleTask(
                    getCronTasksWithNames(),
                    getAllSingleTriggerJobs(),
                    updatedTask
            );
        }

        return updatedTask;
    }

    public List<Invocation> searchInvocations(InvocationQuery query) {
        return invocationDao.search(query);
    }

    public Invocation invoke(Long id) {
        Task task = taskDao.getDetails(id)
                .orElseThrow(() -> new ApplicationException("Task does not exist"));

        Invocation invocation;
        if (Objects.nonNull(task.getWorkerDef())) {
            List<Worker> workers = workerDao.search(
                    new WorkerQuery()
                            .withWorkerDefId(task.getWorkerDef().getId())
                            .withStatus(WorkerStatus.Accepted)
                            .withNotDeleted(true)
            );
            invocation = invocationDao.create(
                    taskDomain.createInvocation(task, workers)
            );
        }
        else {
            invocation = invocationDao.create(
                    taskDomain.createInvocationWithoutWorker(task)
                            .toBuilder()
                            .setStatus(InvocationStatus.Pending)
                            .build()
            );

            if (task.getTaskType().equals(TaskType.sync)) {
                schedulerController.scheduleSingleTriggerJob(
                        getCronTasksWithNames(),
                        getAllSingleTriggerJobs(),
                        SingleTriggerJob.of(
                                Instant.now(),
                                syncDataSeries(invocation),
                                getSyncJobName(invocation)
                        )
                );
            }
        }

        return invocation;
    }

    public fi.jubic.easyschedule.Task invokeTask(Long id) {
        return () -> {
            invoke(id);
        };
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

    public void storeSeriesResult(
            Invocation invocation,
            List<Measurement> measurements
    ) {
        if (invocation.getTask().getTaskType().equals(TaskType.IMPORT)) {

            List<OutputColumn> outputColumns = convertWorkerDefColumnsToOutputColumns(
                    invocation.getWorker().getDefinition().getColumns()
            );

            invocationDao.update(
                    invocation.getId(),
                    optionalInvocation -> taskDomain
                            .updateInvocationStatus(invocation, InvocationStatus.Running)
            );

            List<Measurement> newMeasurements = new ArrayList<>();

            DataSeries createdSeries = DSL.using(conf).transactionResult(transaction -> {

                DataSeries invocationSeries = Objects.requireNonNull(
                        invocation.getTask().getSeries()
                );

                SeriesTable table = SeriesTable
                        .builder()
                        .setId(-1L)
                        .setTableName(invocationSeries.getTableName())
                        .setDataSeries(invocationSeries)
                        .build();

                InvocationQuery query = new InvocationQuery();

                //Get all completed invocations created with the worker currently being used
                List<Invocation> oldInvocations = invocationDao.search(query.withWorker(
                        Objects.requireNonNull(invocation.getWorker()).getId()
                ))
                        .stream()
                        .filter(invocation1 -> invocation1
                        .getStatus()
                        .equals(InvocationStatus.Completed))
                        .collect(Collectors.toList());

                if (invocation.getTask().getSyncIntervalStartTime() != null
                        && invocation.getTask().getSyncIntervalEndTime() != null) {
                    newMeasurements.addAll(measurements.stream()
                            .filter(measurement -> measurement.getTime()
                                    .isAfter(invocation.getTask().getSyncIntervalStartTime()))
                            .filter(measurement -> measurement.getTime()
                                    .isBefore(invocation.getTask().getSyncIntervalEndTime()))
                            .collect(Collectors.toList()));
                }

                else {
                    newMeasurements.addAll(measurements);
                }

                //arranging old invocations by ID - earliest first
                if (oldInvocations.size() > 0) {
                    oldInvocations.sort(Comparator.comparing(Invocation::getId));
                }

                //If the last invocation has different columns as this one we recreate table
                //we also recreate the table if there have been no completed invocations yet
                if (oldInvocations.size() == 0
                        || !oldInvocations.get(oldInvocations.size() - 1)
                        .getWorker()
                        .getDefinition()
                        .getColumns()
                        .stream()
                        .map(WorkerDefColumn::getType)
                        .collect(Collectors.toList()).equals(
                                invocation.getWorker()
                                        .getDefinition()
                                        .getColumns()
                                        .stream()
                                        .map(WorkerDefColumn::getType)
                                        .collect(Collectors.toList())
                        )
                ) {

                    seriesTableDao.deleteWithTableName(
                            invocationSeries.getTableName(), transaction
                    );

                    timeSeriesDao.deleteTable(invocationSeries, transaction);

                    seriesTableDao.create(table, transaction);
                    timeSeriesDao.createTableWithOutputColumns(
                            table,
                            outputColumns,
                            transaction
                    );
                }
                return invocationSeries;

            });
            if (invocation.getTask().getSyncIntervalStartTime() != null
                    && invocation.getTask().getSyncIntervalEndTime() != null) {

                timeSeriesDao.deleteRowsWithTableName(
                        createdSeries.getTableName(),
                        "0",
                        Timestamp.from(invocation.getTask().getSyncIntervalStartTime()),
                        Timestamp.from(invocation.getTask().getSyncIntervalEndTime()),
                        conf
                );
            }


            timeSeriesDao.insertDataWithOutputColumns(
                    createdSeries,
                    outputColumns,
                    timeSeriesDomain.convertFromMeasurement(
                            invocation,
                            newMeasurements
                    )
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

            Invocation running = invocationDao.getDetails(invocation.getId())
                    .orElseThrow(NotFoundException::new);

            invocationDao.update(
                    invocation.getId(),
                    optionalInvocation -> taskDomain
                            .updateInvocationStatus(running, InvocationStatus.Completed)
            );
        }


        else {
            SeriesResult createdResult = DSL.using(conf).transactionResult(transaction -> {
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
            });

            timeSeriesDao.insertDataWithOutputColumns(
                    createdResult,
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

    public Map<String, CronRegistration> createMapOfCronRegistrations(Stream<Task> tasks) {
        return tasks.map(task -> CronRegistration.of(
                        task.getCronTrigger(),
                        invokeTask(task.getId()),
                        task.getName()
                ))
                .collect(
                        Collectors.toMap(
                                CronRegistration::getTaskName,
                                registration -> registration
                        )
                );
    }

    public Map<String, CronRegistration> getCronTasksWithNames() {
        return createMapOfCronRegistrations(
                search(new TaskQuery().withHasCronTrigger(true).withNotDeleted(true))
                        .stream()
                        .filter(task -> CronExpression.isValidExpression(task.getCronTrigger()))
        );
    }

    public fi.jubic.easyschedule.Task syncDataSeries(
            Invocation invocation
    ) {
        return () -> {
            if (!invocation.getStatus().equals(InvocationStatus.Running)) {
                updateInvocationStatus(
                        invocation,
                        InvocationStatus.Running
                );
            }

            ColumnSelector firstColumnSelector = invocation
                    .getColumnSelectors()
                    .stream()
                    .findFirst()
                    .orElseThrow(IllegalStateException::new);

            dataController.update(
                    getCronTasksWithNames(),
                    getLatestRunningOrPendingDataSyncJobs(Instant.now()),
                    firstColumnSelector.getSeries()
            );

            updateInvocationStatus(
                    invocation,
                    InvocationStatus.Completed
            );
        };
    }

    public Map<String, SingleTriggerJob> createMapOfSingleTriggerJobs(
            Stream<Invocation> invocationStream,
            Instant startAt
    ) {
        return invocationStream.map(invocation -> SingleTriggerJob.of(
                        getSyncJobStartAt(invocation, startAt),
                        syncDataSeries(invocation),
                        getSyncJobName(invocation)
                ))
                .collect(
                        Collectors.toMap(
                                SingleTriggerJob::getJobName,
                                registration -> registration
                        )
                );
    }

    public Map<String, SingleTriggerJob> getLatestRunningOrPendingDataSyncJobs(
            Instant defaultStartAt
    ) {
        return createMapOfSingleTriggerJobs(
                invocationDao.getLatestRunningOrPendingDataSyncInvocations().stream(),
                defaultStartAt
        );
    }

    public Map<String, SingleTriggerJob> getAllSingleTriggerJobs() {
        return Stream.concat(
                dataController.getSeriesTablesDeleteJobs()
                        .entrySet()
                        .stream(),
                getLatestRunningOrPendingDataSyncJobs(Instant.now())
                        .entrySet()
                        .stream()
        ).collect(
                Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                )
        );
    }

    public String getSyncJobName(
            Invocation invocation
    ) {
        return "sync-"
                + invocation.getTask().getName()
                + "-"
                + invocation.getInvocationNumber();
    }

    public Instant getSyncJobStartAt(
            Invocation invocation,
            Instant defaultStartAt
    ) {
        if (Objects.nonNull(invocation.getStartTime())) {
            return invocation.getStartTime();
        }
        return defaultStartAt;
    }

    public Task delete(Long id) {
        Task deletedTask = taskDao.update(
                id,
                task -> taskDomain.delete(
                        task.orElseThrow(
                                () -> new InputException("Can't delete a non-existing Task")
                        )
                )
        );
        if (Objects.nonNull(deletedTask.getCronTrigger())) {
            schedulerController.deleteTask(
                    getCronTasksWithNames(),
                    getAllSingleTriggerJobs(),
                    deletedTask
            );
        }

        return deletedTask;
    }

    public Response submitDataSample(
            Invocation invocation,  ImportWorkerDataSample sample
    ) {
        invocationDao.update(
                invocation.getId(),
                optionalInvocation -> taskDomain
                        .updateInvocationStatus(invocation, InvocationStatus.Running)
        );

        if (invocation.getTask().getTaskType().equals(TaskType.IMPORT_SAMPLE)) {
            importWorkerDataSampleDao.putSample(invocation.getId(), sample);

            return Response.ok().build();
        }

        return null;
    }

    public List<OutputColumn> convertWorkerDefColumnsToOutputColumns(
            List<WorkerDefColumn> workerDefColumns
    ) {
        List<OutputColumn> outputColumns = new ArrayList<>();

        workerDefColumns.forEach(workerDefColumn ->
                outputColumns.add(
                        OutputColumn.builder()
                        .setIndex(workerDefColumn.getIndex())
                        .setType(workerDefColumn.getType())
                        .setId(workerDefColumn.getId())
                        .setColumnName(workerDefColumn.getName())
                        .setAlias(workerDefColumn.getName())
                        .build()
                )
        );

        return outputColumns;
    }
}
