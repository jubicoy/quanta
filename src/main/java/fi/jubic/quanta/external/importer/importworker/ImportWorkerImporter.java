package fi.jubic.quanta.external.importer.importworker;

import fi.jubic.quanta.dao.ImportWorkerDataSampleDao;
import fi.jubic.quanta.dao.InvocationDao;
import fi.jubic.quanta.dao.TaskDao;
import fi.jubic.quanta.dao.WorkerDao;
import fi.jubic.quanta.dao.WorkerDefDao;
import fi.jubic.quanta.domain.TaskDomain;
import fi.jubic.quanta.exception.InputException;
import fi.jubic.quanta.external.Importer;
import fi.jubic.quanta.models.DataConnection;
import fi.jubic.quanta.models.DataConnectionConfiguration;
import fi.jubic.quanta.models.DataConnectionType;
import fi.jubic.quanta.models.DataSample;
import fi.jubic.quanta.models.DataSeries;
import fi.jubic.quanta.models.ImportWorkerDataSample;
import fi.jubic.quanta.models.Invocation;
import fi.jubic.quanta.models.InvocationQuery;
import fi.jubic.quanta.models.InvocationStatus;
import fi.jubic.quanta.models.OutputColumn;
import fi.jubic.quanta.models.Task;
import fi.jubic.quanta.models.TaskType;
import fi.jubic.quanta.models.Worker;
import fi.jubic.quanta.models.WorkerDef;
import fi.jubic.quanta.models.WorkerQuery;
import fi.jubic.quanta.models.WorkerStatus;
import fi.jubic.quanta.models.configuration.ImportWorkerDataConnectionConfiguration;
import fi.jubic.quanta.models.metadata.DataConnectionMetadata;
import fi.jubic.quanta.models.typemetadata.TypeMetadata;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.NotFoundException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Singleton
public class ImportWorkerImporter implements Importer {

    private final ImportWorkerDataSampleDao importWorkerDataSampleDao;
    private final InvocationDao invocationDao;
    private final TaskDao taskDao;
    private final WorkerDefDao workerDefDao;
    private final WorkerDao workerDao;

    private final TaskDomain taskDomain;

    @Inject
    ImportWorkerImporter(
            ImportWorkerDataSampleDao importWorkerDataSampleDao,
            InvocationDao invocationDao,
            TaskDao taskDao,
            WorkerDefDao workerDefDao,
            WorkerDao workerDao,
            TaskDomain taskDomain) {

        this.importWorkerDataSampleDao = importWorkerDataSampleDao;
        this.invocationDao = invocationDao;
        this.taskDao = taskDao;
        this.workerDefDao = workerDefDao;
        this.workerDao = workerDao;
        this.taskDomain = taskDomain;
    }

    @Override
    public boolean test(DataConnection dataConnection) {
        return true;
    }

    @Override
    public DataConnection validate(DataConnection dataConnection) {
        return dataConnection;
    }

    @Override
    public DataConnection getWithEmptyLogin(DataConnection dataConnection) {
        return dataConnection;
    }

    @Override
    public DataSample getSample(DataSeries dataSeries, int rows, String start, String end) {

        String taskName = dataSeries.getId() + "-" + dataSeries.getName()
                + "-" + System.currentTimeMillis();

        Instant startTime = null;
        Instant endTime = null;

        if (start != null && end != null) {
            startTime = Instant.parse(start);
            endTime = Instant.parse(end);
        }

        ImportWorkerDataConnectionConfiguration configuration =
                Objects.requireNonNull(dataSeries.getDataConnection()).getConfiguration()
                        .visit(new DataConnectionConfiguration
                                .DefaultFunctionVisitor<ImportWorkerDataConnectionConfiguration>() {

                            @Override
                            public ImportWorkerDataConnectionConfiguration onImportWorker(
                                    ImportWorkerDataConnectionConfiguration importConfiguration
                            ) {
                                return importConfiguration;
                            }

                            @Override
                            public ImportWorkerDataConnectionConfiguration otherwise(
                                    DataConnectionConfiguration configuration
                            ) {
                                throw new InputException(
                                        "IMPORT_WORKER DataConnection has invalid configurations"
                                );
                            }
                        });

        WorkerDef workerDef = workerDefDao.getDetails(configuration.getWorkerDefId())
                .orElseThrow(NotFoundException::new);

        Worker worker = workerDao.search(
                new WorkerQuery()
                        .withWorkerDefId(workerDef.getId())
                        .withStatus(WorkerStatus.Accepted)
                        .withNotDeleted(true)
        )
                .stream()
                .findFirst()
                .orElseThrow(NotFoundException::new);

        Task task = Task.builder()
                .setId(-1L)
                .setName(taskName)
                .setWorkerDef(workerDef)
                .setSeries(dataSeries)
                .setTaskType(TaskType.IMPORT_SAMPLE)
                .setSyncIntervalStartTime(startTime)
                .setSyncIntervalEndTime(endTime)
                .build();

        taskDao.create(task);

        Invocation invocation = Invocation.builder()
                .setId(-1L)
                .setInvocationNumber(-1L)
                .setStatus(InvocationStatus.Pending)
                .setWorker(worker)
                .setTask(taskDao.getDetails(taskName)
                        .orElseThrow(NotFoundException::new))
                .setStartTime(Instant.now())
                .build();

        invocationDao.create(invocation);

        try {
            //We have to get the task/invocation from Dao,
            //the IDs change once entered into the db

            Invocation inv = invocationDao
                    .search(
                            new InvocationQuery()
                                    .withTaskId(
                                            taskDao.getDetails(taskName)
                                                    .orElseThrow(NotFoundException::new)
                                                    .getId()
                                    )
                                    .withStatus(InvocationStatus.Pending)
                    )
                    .stream()
                    .findFirst()
                    .orElseThrow(NotFoundException::new);

            while (true) {
                Thread.sleep(5000);

                Optional<ImportWorkerDataSample> importWorkerDataSample =
                        importWorkerDataSampleDao.takeSample(inv.getId());

                if (importWorkerDataSample.isPresent()) {

                    invocationDao.update(
                            inv.getId(),
                            optionalInvocation -> taskDomain
                                    .updateInvocationStatus(invocation, InvocationStatus.Running)
                    );


                    List<OutputColumn> columnList = new ArrayList<>();

                    importWorkerDataSample.get().getColumns().forEach(column ->
                            columnList.add(
                                    OutputColumn.builder()
                                            .setId(column.getId())
                                            .setColumnName(column.getName())
                                            .setIndex(column.getIndex())
                                            .setType(column.getType())
                                            .build()
                            )
                    );

                    invocationDao.createOutputColumns(inv.getId(), columnList);

                    Invocation running = invocationDao
                            .search(
                                    new InvocationQuery()
                                            .withTaskId(
                                                    taskDao.getDetails(taskName)
                                                            .orElseThrow(NotFoundException::new)
                                                            .getId()
                                            )
                                            .withStatus(InvocationStatus.Running)
                            )
                            .stream()
                            .findFirst()
                            .orElseThrow(NotFoundException::new);

                    invocationDao.update(
                            running.getId(),
                            optionalInvocation -> taskDomain
                                    .updateInvocationStatus(running, InvocationStatus.Completed)
                    );

                    return DataSample.builder()
                            .setDataSeries(dataSeries)
                            .setData(importWorkerDataSample.get().getData())
                            .build();

                }
            }
/*
            while (true) {
                Thread.sleep(5000);

                Optional<ImportWorkerDataSample> bigDataSample =
                        importWorkerDataSampleDao.takeSample(inv.getId());

                if (bigDataSample.isPresent()) {

                    List<List<String>> sample;

                    //if the data has less rows than sample row amount we return the whole data
                    if (bigDataSample.get().getData().size() > rows) {
                        sample = bigDataSample.get().getData().subList(0, rows);
                    }

                    else {
                        sample = bigDataSample.get().getData();
                    }

                    return DataSample.builder()
                            .setDataSeries(dataSeries)
                            .setData(sample)
                            .build();
                }

            }

 */

        }
        catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Stream<List<String>> getRows(DataSeries dataSeries) {
        return Stream.empty();
    }

    @Override
    public TypeMetadata getMetadata(DataConnectionType type) {
        return null;
    }

    @Override
    public DataConnectionMetadata getConnectionMetadata(DataConnection dataConnection) {
        return null;
    }
}
