package fi.jubic.quanta.external.importer.importworker;

import fi.jubic.quanta.dao.ImportWorkerDataSampleDao;
import fi.jubic.quanta.dao.InvocationDao;
import fi.jubic.quanta.dao.TaskDao;
import fi.jubic.quanta.external.Importer;
import fi.jubic.quanta.models.DataConnection;
import fi.jubic.quanta.models.DataConnectionType;
import fi.jubic.quanta.models.DataSample;
import fi.jubic.quanta.models.DataSeries;
import fi.jubic.quanta.models.ImportWorkerDataSample;
import fi.jubic.quanta.models.Invocation;
import fi.jubic.quanta.models.InvocationQuery;
import fi.jubic.quanta.models.InvocationStatus;
import fi.jubic.quanta.models.Task;
import fi.jubic.quanta.models.TaskType;
import fi.jubic.quanta.models.metadata.DataConnectionMetadata;
import fi.jubic.quanta.models.typemetadata.TypeMetadata;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.NotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Singleton
public class ImportWorkerImporter implements Importer {

    private final ImportWorkerDataSampleDao importWorkerDataSampleDao;
    private final InvocationDao invocationDao;
    private final TaskDao taskDao;

    @Inject
    ImportWorkerImporter(
            ImportWorkerDataSampleDao importWorkerDataSampleDao,
            InvocationDao invocationDao,
            TaskDao taskDao
    ) {

        this.importWorkerDataSampleDao = importWorkerDataSampleDao;
        this.invocationDao = invocationDao;
        this.taskDao = taskDao;
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

        Task task = Task.builder()
                .setId(-1L)
                .setName(taskName)
                .setTaskType(TaskType.IMPORT_SAMPLE)
                .build();

        taskDao.create(task);

        Invocation invocation = Invocation.builder()
                .setId(-1L)
                .setInvocationNumber(-1L)
                .setStatus(InvocationStatus.Pending)
                .setTask(taskDao.getDetails(taskName)
                        .orElseThrow(NotFoundException::new))
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
                    return DataSample
                            .builder()
                            .setData(importWorkerDataSample.get().getData())
                            .setDataSeries(dataSeries)
                            .build();
                }
            }

        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
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
