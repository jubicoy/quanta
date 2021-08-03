package fi.jubic.quanta.external.importer.importworker;

import fi.jubic.quanta.dao.DataSeriesDao;
import fi.jubic.quanta.dao.ImportWorkerDataSampleDao;
import fi.jubic.quanta.dao.InvocationDao;
import fi.jubic.quanta.dao.TaskDao;
import fi.jubic.quanta.dao.WorkerDao;
import fi.jubic.quanta.dao.WorkerDefDao;
import fi.jubic.quanta.exception.InputException;
import fi.jubic.quanta.external.Importer;
import fi.jubic.quanta.models.Column;
import fi.jubic.quanta.models.DataConnection;
import fi.jubic.quanta.models.DataConnectionConfiguration;
import fi.jubic.quanta.models.DataConnectionType;
import fi.jubic.quanta.models.DataSample;
import fi.jubic.quanta.models.DataSeries;
import fi.jubic.quanta.models.ImportWorkerDataSample;
import fi.jubic.quanta.models.Invocation;
import fi.jubic.quanta.models.InvocationQuery;
import fi.jubic.quanta.models.InvocationStatus;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Singleton
public class ImportWorkerImporter implements Importer {

    private final ImportWorkerDataSampleDao importWorkerDataSampleDao;
    private final InvocationDao invocationDao;
    private final TaskDao taskDao;
    private final WorkerDefDao workerDefDao;
    private final WorkerDao workerDao;
    private final DataSeriesDao dataSeriesDao;

    private final ExecutorService executor;

    @Inject
    ImportWorkerImporter(
            ImportWorkerDataSampleDao importWorkerDataSampleDao,
            InvocationDao invocationDao,
            TaskDao taskDao,
            WorkerDefDao workerDefDao,
            WorkerDao workerDao,
            DataSeriesDao dataSeriesDao) {

        this.importWorkerDataSampleDao = importWorkerDataSampleDao;
        this.invocationDao = invocationDao;
        this.taskDao = taskDao;
        this.workerDefDao = workerDefDao;
        this.workerDao = workerDao;
        this.dataSeriesDao = dataSeriesDao;

        this.executor = Executors.newFixedThreadPool(5);
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
    public DataSample getSample(DataSeries dataSeries, int rows) {

        String taskName = dataSeries.getId() + "-" + dataSeries.getName()
                + "-" + System.currentTimeMillis();

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
                .setTaskType(TaskType.IMPORT_SAMPLE)
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

            for (int i = 0; i < 5; i++) {

                Thread.sleep(5000);

                Optional<ImportWorkerDataSample> importWorkerDataSample =
                        importWorkerDataSampleDao.takeSample(inv.getId());

                if (importWorkerDataSample.isPresent()) {

                    List<Column> seriesColumns = new ArrayList<>();

                    importWorkerDataSample
                            .get()
                            .getColumns()
                            .forEach(workerDefColumn -> seriesColumns.add(
                                    Column.builder()
                                            .setIndex(workerDefColumn.getIndex())
                                            .setName(workerDefColumn.getName())
                                            .setType(Optional.ofNullable(
                                                    workerDefColumn.getValueType()
                                            ).orElseThrow(NotFoundException::new))
                                            .setId(workerDefColumn.getId())
                                            .build()
                            ));

                    return DataSample.builder()
                            .setDataSeries(dataSeries
                                    .toBuilder()
                                    .setColumns(seriesColumns)
                                    .build())
                            .setData(importWorkerDataSample.get().getData())
                            .build();

                }
            }

            return null;
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public CompletableFuture<Void> getRows(
            DataSeries dataSeries,
            Consumer<Stream<List<String>>> consumer
    ) {
        ImportWorkerDataConnectionConfiguration configuration =
                Objects.requireNonNull(dataSeries.getDataConnection()).getConfiguration()
                        .visit(new DataConnectionConfiguration
                                .DefaultFunctionVisitor<>() {

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
                                        "IMPORT_WORKER DataConnection"
                                                + " has invalid configurations"
                                );
                            }
                        });

        List<Invocation> invocations = invocationDao.search(
                new InvocationQuery().withWorker(
                        workerDao.search(
                                new WorkerQuery()
                                        .withWorkerDefId(configuration.getWorkerDefId())
                                        .withStatus(WorkerStatus.Accepted)
                        )
                                .stream()
                                .findFirst()
                                .get()
                                .getId()
                )
        );

        Invocation invocation = invocations.get(invocations.size() - 1);

        return CompletableFuture.supplyAsync(
                () -> {
                    for (int i = 0; i < 120; i++) {
                        try {
                            Thread.sleep(5000);

                            Optional<ImportWorkerDataSample> bigDataSample =
                                    importWorkerDataSampleDao.takeSample(invocation.getId());

                            if (bigDataSample.isPresent()) {
                                consumer.accept(
                                        bigDataSample
                                                .get()
                                                .getData()
                                                .stream()
                                );
                                return null;
                            }
                        }
                        catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    return null;
                },
                executor
        );
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
