package fi.jubic.quanta.controller;

import fi.jubic.quanta.dao.ExternalClientDao;
import fi.jubic.quanta.dao.TaskDao;
import fi.jubic.quanta.domain.ExternalClientDomain;
import fi.jubic.quanta.models.ExternalClient;
import fi.jubic.quanta.models.Task;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.NotFoundException;
import java.util.List;

@Singleton
public class ExternalClientController {
    private final ExternalClientDomain externalClientDomain;

    private final ExternalClientDao externalClientDao;
    private final TaskDao taskDao;

    @Inject
    ExternalClientController(
            ExternalClientDomain externalClientDomain,
            ExternalClientDao externalClientDao,
            TaskDao taskDao
    ) {
        this.externalClientDomain = externalClientDomain;
        this.externalClientDao = externalClientDao;
        this.taskDao = taskDao;
    }

    public ExternalClient generateExternalClient(Long taskId, ExternalClient externalClient) {
        Task task = taskDao
                .getDetails(taskId)
                .orElseThrow(NotFoundException::new);
        return externalClientDao
                .create(
                        externalClientDomain
                                .create(externalClient)
                                .toBuilder()
                                .setTask(task)
                                .build()
                );
    }

    public ExternalClient deleteExternalClient(Long externalClientId) {
        return externalClientDao.update(
                externalClientId,
                optionalClient -> externalClientDomain.softDelete(
                        optionalClient.orElseThrow(NotFoundException::new)
                )
        );
    }

    public List<ExternalClient> getAllOfTask(Long taskId) {
        return externalClientDao.getAllOfTask(taskId);
    }
}
