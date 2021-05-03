package fi.jubic.quanta.dao;

import fi.jubic.quanta.config.Configuration;
import fi.jubic.quanta.db.tables.records.WorkerRecord;
import fi.jubic.quanta.exception.ApplicationException;
import fi.jubic.quanta.models.Worker;
import fi.jubic.quanta.models.WorkerDef;
import fi.jubic.quanta.models.WorkerQuery;
import fi.jubic.quanta.models.WorkerStatus;
import org.jooq.Condition;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;

import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fi.jubic.quanta.db.Tables.WORKER;
import static fi.jubic.quanta.db.Tables.WORKER_DEFINITION;

public class WorkerDao {
    private final org.jooq.Configuration conf;
    private final WorkerDefDao workerDefDao;

    @Inject
    WorkerDao(Configuration conf, WorkerDefDao workerDefDao) {
        this.conf = conf.getJooqConfiguration().getConfiguration();
        this.workerDefDao = workerDefDao;
    }

    public List<Worker> search(WorkerQuery query) {
        Condition condition = Stream
                .of(
                        query.getNotDeleted().map(notDeleted ->
                                WORKER.DELETED_AT.isNull()
                        ),
                        query.getWorkerDefId().map(WORKER.DEFINITION_ID::eq),
                        query.getStatus().map(status -> {
                            if (status == WorkerStatus.Accepted) {
                                return WORKER.ACCEPTED_ON.isNotNull();
                            }
                            return WORKER.ACCEPTED_ON.isNull();
                        })
                )
                .filter(Optional::isPresent)
                .map(Optional::get)
                .reduce(Condition::and)
                .orElseGet(DSL::trueCondition);

        return DSL.using(conf).transactionResult(transaction ->
                DSL.using(transaction)
                        .select()
                        .from(WORKER)
                        .join(WORKER_DEFINITION)
                        .on(WORKER.DEFINITION_ID.eq(WORKER_DEFINITION.ID))
                        .where(condition)
                        .fetchStream()
                        .collect(Worker.mapper
                                .withDefinition(
                                        WorkerDef.mapper
                                )
                        )
                        .stream()
                        .map(worker ->
                                worker.toBuilder()
                                        .setDefinition(
                                                workerDefDao.getDetailsWithTransaction(
                                                        worker.getDefinition().getId(),
                                                        transaction
                                                ).orElseThrow(NotFoundException::new)
                                        )
                                        .build()
                                )
                            .collect(Collectors.toList())
        );

    }

    public Optional<Worker> getDetails(Long workerId) {
        return getDetails(workerId, conf);
    }

    private Optional<Worker> getDetails(
            Long workerId,
            org.jooq.Configuration transaction
    ) {
        return getBy(WORKER.ID.eq(workerId), transaction);
    }

    public Optional<Worker> getDetailsByToken(
            String workerToken,
            org.jooq.Configuration transaction
    ) {
        return getBy(WORKER.TOKEN.eq(workerToken), transaction);
    }

    public Optional<Worker> getDetailsByToken(String workerToken) {
        return getBy(WORKER.TOKEN.eq(workerToken), conf);
    }

    public Worker create(
            Worker worker,
            org.jooq.Configuration transaction
    ) {
        WorkerRecord record = Worker.mapper.write(
                DSL.using(transaction).newRecord(WORKER),
                worker
        );
        record.store();
        return getDetails(record.getId(), transaction)
                .orElseThrow(IllegalStateException::new);
    }

    public Worker update(
            Long workerId,
            Function<Optional<Worker>, Worker> updater
    ) {
        try {
            return DSL.using(conf).transactionResult(
                    transaction -> update(workerId, updater, transaction)
            );
        }
        catch (DataAccessException exception) {
            throw new ApplicationException("Worker update failed", exception);
        }
    }

    public Worker update(
            Long workerId,
            Function<Optional<Worker>, Worker> updater,
            org.jooq.Configuration transaction
    ) {
        Optional<Worker> originalWorker = getDetails(workerId, transaction);
        Worker worker = updater.apply(originalWorker);

        boolean noUpdateRequired = originalWorker
                .filter(original -> Objects.equals(worker, original))
                .isPresent();

        if (noUpdateRequired) {
            // Return original Worker instead of updated one
            // Because it will be obvious that no database change has been made
            return originalWorker
                    .orElseThrow(NotFoundException::new);
        }

        WorkerRecord record = Optional
                .ofNullable(
                        DSL.using(transaction)
                                .select()
                                .from(WORKER)
                                .where(WORKER.ID.eq(workerId))
                                .fetchOneInto(WorkerRecord.class)
                )
                .orElseThrow(IllegalStateException::new);

        Worker.mapper.write(record, worker).store();

        return getDetails(workerId, transaction)
                .orElseThrow(IllegalStateException::new);
    }

    private Optional<Worker> getBy(
            Condition condition,
            org.jooq.Configuration transaction
    ) {
        return DSL.using(transaction)
                .select()
                .from(WORKER)
                .join(WORKER_DEFINITION)
                .on(WORKER.DEFINITION_ID.eq(WORKER_DEFINITION.ID))
                .where(condition)
                .fetchOptional()
                .map(record -> Worker.mapper
                        .withDefinition(
                                WorkerDef.mapper
                        )
                        .map(record)
                ).map(worker -> worker
                        .toBuilder()
                        .setDefinition(
                                workerDefDao.getDetailsWithTransaction(
                                        worker.getDefinition().getId(),
                                        transaction
                                ).orElseThrow(NotFoundException::new)
                        )
                        .build()
                );

    }
}
