package fi.jubic.quanta.dao;

import fi.jubic.quanta.db.tables.records.ExternalClientRecord;
import fi.jubic.quanta.exception.ApplicationException;
import fi.jubic.quanta.models.ColumnSelector;
import fi.jubic.quanta.models.DataConnection;
import fi.jubic.quanta.models.DataSeries;
import fi.jubic.quanta.models.ExternalClient;
import fi.jubic.quanta.models.ExternalClientQuery;
import fi.jubic.quanta.models.Task;
import fi.jubic.quanta.models.User;
import fi.jubic.quanta.models.WorkerDef;
import fi.jubic.quanta.models.WorkerDefColumn;
import org.jooq.Condition;
import org.jooq.Configuration;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fi.jubic.quanta.db.Tables.DATA_CONNECTION;
import static fi.jubic.quanta.db.Tables.DATA_SERIES;
import static fi.jubic.quanta.db.Tables.EXTERNAL_CLIENT;
import static fi.jubic.quanta.db.Tables.TASK;
import static fi.jubic.quanta.db.Tables.TASK_COLUMN_SELECTOR;
import static fi.jubic.quanta.db.Tables.USER;
import static fi.jubic.quanta.db.Tables.WORKER_DEFINITION;
import static fi.jubic.quanta.db.Tables.WORKER_DEFINITION_COLUMN;

@Singleton
public class ExternalClientDao {
    private final Configuration conf;

    @Inject
    ExternalClientDao(fi.jubic.quanta.config.Configuration configuration) {
        this.conf = configuration.getJooqConfiguration().getConfiguration();
    }

    public List<ExternalClient> search(ExternalClientQuery query) {
        Condition condition = Stream
                .of(
                        query.getUser().map(USER.ID::eq)
                )
                .filter(Optional::isPresent)
                .map(Optional::get)
                .reduce(Condition::and)
                .orElseGet(DSL::trueCondition)
                .and(EXTERNAL_CLIENT.DELETED_AT.isNull());

        return DSL.using(conf)
                .select()
                .from(EXTERNAL_CLIENT)
                .leftJoin(USER)
                .on(
                        USER.ID.eq(EXTERNAL_CLIENT.USER_ID)
                )
                .leftJoin(TASK)
                .on(
                        TASK.ID.eq(EXTERNAL_CLIENT.TASK_ID)
                )
                .where(condition)
                .fetchStream()
                .collect(
                        ExternalClient.mapper
                                .withUser(User.mapper)
                                .withTask(Task.mapper)
                );
    }

    public List<ExternalClient> getAllOfTask(Long taskId) {
        return DSL.using(conf)
                .select()
                .from(EXTERNAL_CLIENT)
                .leftJoin(USER)
                .on(
                        USER.ID.eq(EXTERNAL_CLIENT.USER_ID)
                )
                .leftJoin(TASK)
                .on(
                        TASK.ID.eq(EXTERNAL_CLIENT.TASK_ID)
                )
                .where(
                        EXTERNAL_CLIENT.TASK_ID.eq(taskId)
                )
                .fetchStream()
                .collect(
                        ExternalClient.mapper
                                .withUser(User.mapper)
                                .withTask(Task.mapper)
                )
                .stream()
                .filter(
                        externalClient -> Objects.isNull(externalClient.getDeletedAt())
                )
                .collect(Collectors.toList());
    }

    public List<ExternalClient> getExternalClients() {
        return DSL.using(conf)
                .select()
                .from(EXTERNAL_CLIENT)
                .leftJoin(TASK)
                .on(EXTERNAL_CLIENT.TASK_ID.eq(TASK.ID))
                .leftJoin(USER)
                .on(EXTERNAL_CLIENT.USER_ID.eq(USER.ID))
                .where(EXTERNAL_CLIENT.DELETED_AT.isNull())
                .fetchStream()
                .collect(
                        ExternalClient.mapper
                        .withTask(Task.mapper)
                        .withUser(User.mapper)
                );
    }

    public Optional<ExternalClient> getDetails(User user) {
        return getDetails(
                EXTERNAL_CLIENT.USER_ID.eq(user.getId()),
                conf
        );
    }

    public Optional<ExternalClient> getDetails(String token) {
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
                .leftJoin(USER)
                .on(USER.ID.eq(EXTERNAL_CLIENT.USER_ID))
                .leftJoin(TASK)
                .on(TASK.ID.eq(EXTERNAL_CLIENT.TASK_ID))
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
                        ExternalClient.mapper.withUser(User.mapper).collectingWithTask(
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
        return Optional
                .ofNullable(
                        DSL.using(transaction)
                                .insertInto(EXTERNAL_CLIENT)
                                .set(
                                        ExternalClient.mapper.write(
                                                DSL.using(conf).newRecord(EXTERNAL_CLIENT),
                                                externalClient
                                        )
                                )
                                .returning(EXTERNAL_CLIENT.ID)
                                .fetchOne()
                )
                .map(ExternalClientRecord::getId)
                .flatMap(this::getDetails)
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
                                .where(
                                        EXTERNAL_CLIENT.ID.eq(externalClient.getId())
                                )
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
