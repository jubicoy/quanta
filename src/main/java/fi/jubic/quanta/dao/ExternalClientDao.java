package fi.jubic.quanta.dao;

import fi.jubic.quanta.exception.ApplicationException;
import fi.jubic.quanta.models.ColumnSelector;
import fi.jubic.quanta.models.DataConnection;
import fi.jubic.quanta.models.DataSeries;
import fi.jubic.quanta.models.ExternalClient;
import fi.jubic.quanta.models.Task;
import fi.jubic.quanta.models.WorkerDef;
import fi.jubic.quanta.models.WorkerDefColumn;
import org.jooq.Condition;
import org.jooq.Configuration;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.NotFoundException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static fi.jubic.quanta.db.Tables.DATA_CONNECTION;
import static fi.jubic.quanta.db.Tables.DATA_SERIES;
import static fi.jubic.quanta.db.Tables.EXTERNAL_CLIENT;
import static fi.jubic.quanta.db.Tables.TASK;
import static fi.jubic.quanta.db.Tables.TASK_COLUMN_SELECTOR;
import static fi.jubic.quanta.db.Tables.WORKER_DEFINITION;
import static fi.jubic.quanta.db.Tables.WORKER_DEFINITION_COLUMN;

@Singleton
public class ExternalClientDao {
    private final Configuration conf;

    @Inject
    ExternalClientDao(fi.jubic.quanta.config.Configuration configuration) {
        this.conf = configuration.getJooqConfiguration().getConfiguration();
    }

    public List<ExternalClient> getAllOfTask(Long taskId) {
        return DSL.using(conf)
                .select()
                .from(EXTERNAL_CLIENT)
                .where(EXTERNAL_CLIENT.TASK_ID.eq(taskId))
                .fetchStream()
                .collect(ExternalClient.mapper)
                .stream()
                .map(
                        externalClient -> getDetails(externalClient.getId())
                                .orElseThrow(NotFoundException::new)
                )
                .filter(
                        externalClient -> Objects.isNull(externalClient.getDeletedAt())
                )
                .collect(Collectors.toList());
    }

    public Optional<ExternalClient> getDetailsByToken(String token) {
        return getDetails(
                EXTERNAL_CLIENT.TOKEN.eq(token),
                conf
        );
    }

    public Optional<ExternalClient> getDetails(Long id) {
        return getDetails(
                EXTERNAL_CLIENT.ID.eq(id),
                conf
        );
    }

    public Optional<ExternalClient> getDetails(Long id, Configuration transaction) {
        return getDetails(
                EXTERNAL_CLIENT.ID.eq(id),
                transaction
        );
    }

    private Optional<ExternalClient> getDetails(Condition condition, Configuration transaction) {
        return DSL.using(transaction)
                .select()
                .from(EXTERNAL_CLIENT)
                .join(TASK)
                .on(EXTERNAL_CLIENT.TASK_ID.eq(TASK.ID))
                .leftJoin(WORKER_DEFINITION)
                .on(TASK.WORKER_DEF_ID.eq(WORKER_DEFINITION.ID))
                .leftJoin(TASK_COLUMN_SELECTOR)
                .on(TASK_COLUMN_SELECTOR.TASK_ID.eq(TASK.ID))
                .leftJoin(WORKER_DEFINITION_COLUMN)
                .on(TASK_COLUMN_SELECTOR.WORKER_DEFINITION_COLUMN_ID
                        .eq(WORKER_DEFINITION_COLUMN.ID)
                )
                .leftJoin(DATA_SERIES)
                .on(TASK_COLUMN_SELECTOR.DATA_SERIES_ID.eq(DATA_SERIES.ID))
                .leftJoin(DATA_CONNECTION)
                .on(DATA_SERIES.DATA_CONNECTION_ID.eq(DATA_CONNECTION.ID))
                .where(condition)
                .fetchStream()
                .collect(
                        ExternalClient.mapper.collectingWithTask(
                                Task.mapper
                                        .withWorkerDef(WorkerDef.mapper)
                                        .collectingWithColumnSelectors(
                                                ColumnSelector.taskColumnSelectorMapper
                                                        .withSeries(
                                                                DataSeries.mapper
                                                                        .withDataConnection(
                                                                                DataConnection
                                                                                        .mapper
                                                                        )
                                                        )
                                                        .withWorkerDefColumn(
                                                                WorkerDefColumn
                                                                        .workerDefColumnMapper
                                                        )
                                        )
                        )
                );
    }

    public ExternalClient create(ExternalClient externalClient) {
        return create(externalClient, conf);
    }

    private ExternalClient create(ExternalClient externalClient, Configuration transaction) {
        Long externalClientId = DSL.using(transaction)
                .insertInto(EXTERNAL_CLIENT)
                .set(
                        ExternalClient.mapper.write(
                                DSL.using(conf).newRecord(EXTERNAL_CLIENT),
                                externalClient
                        )
                )
                .returning(EXTERNAL_CLIENT.ID)
                .fetchOne()
                .getId();

        return getDetails(externalClientId)
                .orElseThrow(IllegalStateException::new);
    }

    public ExternalClient update(
            Long id,
            Function<Optional<ExternalClient>, ExternalClient> updater
    ) {
        try {
            return DSL.using(conf).transactionResult(
                    transaction -> {
                        ExternalClient externalClient = updater.apply(getDetails(id));

                        DSL.using(transaction)
                                .update(EXTERNAL_CLIENT)
                                .set(
                                        ExternalClient.mapper.write(
                                                DSL.using(transaction).newRecord(EXTERNAL_CLIENT),
                                                externalClient
                                        )
                                )
                                .where(EXTERNAL_CLIENT.ID.eq(id))
                                .execute();

                        return getDetails(id, transaction)
                                .orElseThrow(IllegalStateException::new);
                    }
            );
        }
        catch (DataAccessException exception) {
            throw new ApplicationException("Could not update ExternalClient", exception);
        }
    }
}
