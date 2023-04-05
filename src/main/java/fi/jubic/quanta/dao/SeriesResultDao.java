package fi.jubic.quanta.dao;

import fi.jubic.quanta.db.tables.records.SeriesResultRecord;
import fi.jubic.quanta.models.ColumnSelector;
import fi.jubic.quanta.models.DataConnection;
import fi.jubic.quanta.models.DataSeries;
import fi.jubic.quanta.models.Invocation;
import fi.jubic.quanta.models.SeriesResult;
import fi.jubic.quanta.models.SeriesResultQuery;
import fi.jubic.quanta.models.Task;
import fi.jubic.quanta.models.Worker;
import fi.jubic.quanta.models.WorkerDef;
import fi.jubic.quanta.models.WorkerDefColumn;
import fi.jubic.quanta.util.Sql;
import jakarta.ws.rs.NotFoundException;
import org.jooq.Condition;
import org.jooq.Configuration;
import org.jooq.Record;
import org.jooq.impl.DSL;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static fi.jubic.quanta.db.Tables.DATA_SERIES;
import static fi.jubic.quanta.db.Tables.WORKER_DEFINITION_COLUMN;
import static fi.jubic.quanta.db.tables.DataConnection.DATA_CONNECTION;
import static fi.jubic.quanta.db.tables.Invocation.INVOCATION;
import static fi.jubic.quanta.db.tables.InvocationColumnSelector.INVOCATION_COLUMN_SELECTOR;
import static fi.jubic.quanta.db.tables.SeriesResult.SERIES_RESULT;
import static fi.jubic.quanta.db.tables.Task.TASK;
import static fi.jubic.quanta.db.tables.Worker.WORKER;
import static fi.jubic.quanta.db.tables.WorkerDefinition.WORKER_DEFINITION;

@Singleton
public class SeriesResultDao {
    private final Configuration conf;
    private final InvocationDao invocationDao;
    private final WorkerDefDao workerDefDao;


    @Inject
    SeriesResultDao(
            fi.jubic.quanta.config.Configuration configuration,
            InvocationDao invocationDao,
            WorkerDefDao workerDefDao
    ) {
        this.conf = configuration.getJooqConfiguration().getConfiguration();
        this.invocationDao = invocationDao;
        this.workerDefDao = workerDefDao;
    }

    public List<SeriesResult> search(
            SeriesResultQuery query
    ) {
        Condition condition = Stream
                .of(
                        query.getInvocationId().map(SERIES_RESULT.INVOCATION_ID::eq)
                )
                .filter(Optional::isPresent)
                .map(Optional::get)
                .reduce(Condition::and)
                .orElseGet(DSL::trueCondition);

        return DSL.using(conf)
                .select()
                .from(SERIES_RESULT)
                .where(condition)
                .fetchStream()
                .collect(SeriesResult.mapper);
    }

    public List<SeriesResult> searchCompleted(
            SeriesResultQuery query
    ) {
        Condition condition = Stream
                .of(
                        query.getInvocationId().map(SERIES_RESULT.INVOCATION_ID::eq),
                        query.getInvocationStatus().map(INVOCATION.STATUS::eq)
                )
                .filter(Optional::isPresent)
                .map(Optional::get)
                .reduce(Condition::and)
                .orElseGet(DSL::trueCondition);

        return DSL.using(conf)
                .select()
                .from(SERIES_RESULT)
                .leftJoin(INVOCATION)
                .on(SERIES_RESULT.INVOCATION_ID.eq(INVOCATION.ID))
                .leftJoin(TASK)
                .on(INVOCATION.TASK_ID.eq(TASK.ID))
                .leftJoin(WORKER_DEFINITION)
                .on(TASK.WORKER_DEF_ID.eq(WORKER_DEFINITION.ID))
                .leftJoin(WORKER)
                .on(INVOCATION.WORKER_ID.eq(WORKER.ID))
                .where(condition)
                .fetchStream()
                .collect(SeriesResult.mapper.withInvocation(
                        Invocation.mapper
                                .withTask(
                                        Task.mapper.withWorkerDef(WorkerDef.mapper)
                                )
                                .withWorker(Worker.mapper
                                        .withDefinition(WorkerDef.mapper)
                                )
                        )
                );
    }

    public Optional<SeriesResult> getDetails(Long seriesResultId) {
        return getDetails(seriesResultId, conf);
    }

    public Optional<SeriesResult> getDetails(
            Long seriesResultId,
            Configuration transaction
    ) {
        List<Record> records = DSL.using(transaction)
                .select()
                .from(SERIES_RESULT)
                .leftJoin(INVOCATION)
                .on(SERIES_RESULT.INVOCATION_ID.eq(INVOCATION.ID))
                .leftJoin(TASK)
                .on(INVOCATION.TASK_ID.eq(TASK.ID))
                .leftJoin(WORKER)
                .on(INVOCATION.WORKER_ID.eq(WORKER.ID))
                .leftJoin(WORKER_DEFINITION)
                .on(WORKER.DEFINITION_ID.eq(WORKER_DEFINITION.ID))
                .leftJoin(INVOCATION_COLUMN_SELECTOR)
                .on(INVOCATION.ID.eq(INVOCATION_COLUMN_SELECTOR.INVOCATION_ID))
                .leftJoin(WORKER_DEFINITION_COLUMN)
                .on(INVOCATION_COLUMN_SELECTOR.WORKER_DEFINITION_COLUMN_ID.eq(
                        WORKER_DEFINITION_COLUMN.ID)
                )
                .leftJoin(DATA_SERIES)
                .on(INVOCATION_COLUMN_SELECTOR.DATA_SERIES_ID.eq(DATA_SERIES.ID))
                .leftJoin(DATA_CONNECTION)
                .on(DATA_SERIES.DATA_CONNECTION_ID.eq(DATA_CONNECTION.ID))
                .where(SERIES_RESULT.ID.eq(seriesResultId))
                .fetch();

        return records
                .stream()
                .findFirst()
                .map(record -> SeriesResult.mapper
                        .withInvocation(Invocation.mapper
                                .withTask(Task.mapper.withWorkerDef(WorkerDef.mapper))
                                .withWorker(Worker.mapper
                                        .withDefinition(WorkerDef.mapper)
                                )
                        )
                        .map(record)
                )
                .map(result -> result.toBuilder()
                        .setInvocation(Objects
                                .requireNonNull(result.getInvocation())
                                .toBuilder()
                                .setColumnSelectors(records.stream()
                                        .collect(ColumnSelector.invocationColumnSelectorMapper
                                                .withSeries(DataSeries.mapper
                                                        .withDataConnection(
                                                                DataConnection.mapper
                                                        )
                                                ).withWorkerDefColumn(
                                                        WorkerDefColumn.workerDefColumnMapper
                                                )
                                        )
                                )
                                .setOutputColumns(
                                        invocationDao.getInvocationOutputColumns(Objects
                                                .requireNonNull(result.getInvocation()).getId())
                                )
                                .setWorker(result
                                        .getInvocation().getWorker().toBuilder()
                                        .setDefinition(
                                                workerDefDao.getDetails(
                                                        result.getInvocation()
                                                                .getWorker()
                                                                .getDefinition()
                                                                .getId()
                                                ).orElseThrow(NotFoundException::new)
                                        ).build()
                                )
                                .build()
                        )
                        .build()
                );
    }

    public Map<Long, String> getTableNames(List<Long> seriesResultIds) {
        if (seriesResultIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return DSL.using(conf)
                .select(SERIES_RESULT.ID, SERIES_RESULT.TABLE_NAME)
                .from(SERIES_RESULT)
                .where(SERIES_RESULT.ID.in(seriesResultIds))
                .fetchMap(SERIES_RESULT.ID, SERIES_RESULT.TABLE_NAME);
    }

    public SeriesResult create(
            SeriesResult seriesResult,
            Configuration transaction
    ) {
        SeriesResultRecord record = SeriesResult.mapper.write(
                DSL.using(transaction).newRecord(SERIES_RESULT),
                seriesResult
        );
        record.store();

        // TODO: Store nested stuff

        return getDetails(record.getId(), transaction)
                .orElseThrow(IllegalStateException::new);
    }

    public void dropTable(
            SeriesResult seriesResult
    ) {
        dropTable(seriesResult, conf);
    }

    private void dropTable(
            SeriesResult seriesResult,
            Configuration transaction
    ) {
        String command = String.format(
                "DROP TABLE \"%s\"",
                Sql.sanitize(seriesResult.getTableName())
        );
        DSL.using(transaction).execute(command);
    }
}
