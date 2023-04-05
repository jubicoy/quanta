package fi.jubic.quanta.controller;

import fi.jubic.quanta.dao.ExternalClientDao;
import fi.jubic.quanta.dao.TaskDao;
import fi.jubic.quanta.domain.ExternalClientDomain;
import fi.jubic.quanta.models.ExternalClient;
import fi.jubic.quanta.models.ExternalClientQuery;
import fi.jubic.quanta.models.Task;
import fi.jubic.quanta.models.User;
import jakarta.ws.rs.NotFoundException;

import javax.inject.Inject;
import javax.inject.Singleton;
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

    public List<ExternalClient> search(ExternalClientQuery query) {
        return externalClientDao.search(query);
    }

    public ExternalClient generateExternalClient(String name, String description, User user) {
        return externalClientDao
                .create(
                        externalClientDomain
                                .create(
                                        ExternalClient.builder()
                                                .setId(0L)
                                                .setName(name)
                                                .setToken("")
                                                .setDescription(description)
                                                .setUser(user)
                                                .build()
                                )
                );
    }

    public ExternalClient generateExternalClient(
            Long taskId,
            String name,
            String description,
            User user
    ) {
        Task task = taskDao
                .getDetails(taskId)
                .orElseThrow(
                        () -> new NotFoundException("Task not found")
                );

        return externalClientDao
                .create(
                        externalClientDomain
                                .create(
                                        ExternalClient.builder()
                                                .setId(0L)
                                                .setName(name)
                                                .setDescription(description)
                                                .setToken("")
                                                .setTask(task)
                                                .setUser(user)
                                                .build()
                                )
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

    public List<ExternalClient> getExternalClients() {
        return externalClientDao.getExternalClients();
    }

    public List<ExternalClient> getAllOfTask(Long taskId) {
        return externalClientDao.getAllOfTask(taskId);
    }
}
