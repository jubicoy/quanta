package fi.jubic.quanta.domain;

import fi.jubic.quanta.exception.ApplicationException;
import fi.jubic.quanta.exception.AuthorizationException;
import fi.jubic.quanta.models.Worker;
import fi.jubic.quanta.models.WorkerDef;
import fi.jubic.quanta.models.WorkerStatus;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

@Singleton
public class WorkerDomain {
    @Inject
    WorkerDomain() {

    }

    public Worker delete(Worker worker) {
        return worker.toBuilder()
                .setDeletedAt(Instant.now())
                .build();
    }

    public WorkerDef deleteWorkerDefinition(WorkerDef workerDef) {
        return workerDef.toBuilder()
                .setDeletedAt(Instant.now())
                .build();
    }

    public Worker authorize(Worker worker) {
        return worker.toBuilder()
                .setAcceptedOn(Instant.now())
                .build();
    }

    public Worker unauthorize(Worker worker) {
        return worker.toBuilder()
                .setAcceptedOn(null)
                .build();
    }

    public void checkAuthorization(@Nullable Worker worker) {
        WorkerStatus status = Optional.ofNullable(worker)
                .map(Worker::getStatus)
                .orElse(WorkerStatus.Pending);
        if (status != WorkerStatus.Accepted) {
            throw new AuthorizationException("Worker not authorized by admin");
        }
    }

    public Worker update(Worker oldWorker, Worker newWorker) {
        if (Objects.equals(oldWorker.getDefinition(), newWorker.getDefinition())) {
            return oldWorker;
        }
        return oldWorker.toBuilder()
                .setDefinition(newWorker.getDefinition())
                .build();
    }

    public Worker updateLastSeen(Worker worker) {
        return worker.toBuilder()
                .setLastSeen(Instant.now())
                .build();
    }

    public Worker createOrGetWorker(
            Worker worker,
            Worker existingWorker,
            WorkerDef existingWorkerDef
    ) {
        WorkerDef workerDef;
        if (existingWorker != null) {
            workerDef = existingWorkerDef;
        }
        else {
            workerDef = worker.getDefinition()
                    .toBuilder()
                    .setId(0L)
                    .build();
        }

        if (workerDef.getName().length() == 0) {
            throw new ApplicationException("Invalid WorkerDef name");
        }

        if (existingWorker == null) {
            return worker.toBuilder()
                    .setId(0L)
                    .setLastSeen(null)
                    .setAcceptedOn(null)
                    .setDeletedAt(null)
                    .setDefinition(workerDef)
                    .build();
        }

        if (Objects.equals(worker.getDefinition(), workerDef)) {
            return existingWorker;
        }

        return existingWorker.toBuilder()
                .setDefinition(workerDef)
                .build();
    }
}
