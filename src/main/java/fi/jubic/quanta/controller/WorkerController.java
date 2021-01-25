package fi.jubic.quanta.controller;

import fi.jubic.quanta.dao.WorkerDao;
import fi.jubic.quanta.dao.WorkerDefDao;
import fi.jubic.quanta.domain.WorkerDomain;
import fi.jubic.quanta.exception.InputException;
import fi.jubic.quanta.models.*;
import org.jooq.Configuration;
import org.jooq.impl.DSL;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;

@Singleton
public class WorkerController {
    private final WorkerDomain workerDomain;
    private final WorkerDao workerDao;
    private final WorkerDefDao workerDefDao;
    private final Configuration conf;

    @Inject
    WorkerController(
            WorkerDomain workerDomain,
            WorkerDao workerDao,
            WorkerDefDao workerDefDao,
            fi.jubic.quanta.config.Configuration configuration
    ) {
        this.workerDomain = workerDomain;
        this.workerDao = workerDao;
        this.workerDefDao = workerDefDao;
        this.conf = configuration.getJooqConfiguration()
                .getConfiguration();
    }

    public List<Worker> search(WorkerQuery query) {
        return workerDao.search(query);
    }

    public Optional<Worker> getDetails(Long workerId) {
        return workerDao.getDetails(workerId);
    }

    public Worker register(Worker worker) {
        return DSL.using(conf).transactionResult(transaction -> {
            Worker existingWorker = workerDao
                    .getDetailsByToken(
                            worker.getToken(),
                            transaction
                    )
                    .orElse(null);
            WorkerDef existingWorkerDef = workerDefDao
                    .getDetailsByName(
                            worker.getDefinition().getName(),
                            transaction
                    )
                    .orElse(null);

            Worker createdWorker = workerDomain.createOrGetWorker(
                    worker,
                    existingWorker,
                    existingWorkerDef
            );

            if (createdWorker.getDefinition().getId().equals(0L)) {
                createdWorker = createdWorker.toBuilder()
                        .setDefinition(
                                workerDefDao.create(
                                        createdWorker.getDefinition(),
                                        transaction
                                )
                        )
                        .build();
            }

            if (createdWorker.getId().equals(0L)) {
                return workerDao.create(createdWorker, transaction);
            }

            Worker finalCreatedWorker = createdWorker;
            return workerDao.update(
                    createdWorker.getId(),
                    original -> workerDomain.update(
                            original.orElseThrow(IllegalStateException::new),
                            finalCreatedWorker
                    ),
                    transaction
            );
        });
    }

    public Worker delete(Long workerId) {
        Worker deletedWorker = workerDao.update(
                workerId,
                worker -> workerDomain.delete(
                        worker.orElseThrow(
                                () -> new InputException("Can't delete a non-existing Worker")
                        )
                )
        );
        // Delete also worker's definition so it cannot used to create tasks
        workerDefDao.update(
                deletedWorker.getDefinition().getId(),
                workerDef -> workerDomain.deleteWorkerDefinition(
                        workerDef.orElseThrow(
                                () -> new InputException(
                                        "Can't delete a non-existing Worker Definition"
                                )
                        )
                )
        );

        return deletedWorker;
    }

    public Worker authorize(Long workerId) {
        return workerDao.update(
                workerId,
                worker -> workerDomain.authorize(
                        worker.orElseThrow(
                                () -> new InputException(
                                        "Can't authorize a non-existing Worker"
                                )
                        )
                )
        );
    }

    public Worker unauthorize(Long workerId) {
        return workerDao.update(
                workerId,
                worker -> workerDomain.unauthorize(
                        worker.orElseThrow(
                                () -> new InputException(
                                        "Can't unauthorize a non-existing Worker"
                                )
                        )
                )
        );
    }

    public List<WorkerDef> searchDefs(WorkerDefQuery query) {
        return workerDefDao.search(query);
    }
}
