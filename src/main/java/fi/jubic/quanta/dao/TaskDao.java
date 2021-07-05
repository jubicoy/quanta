package fi.jubic.quanta.dao;

import fi.jubic.quanta.db.tables.records.TaskParameterRecord;
import fi.jubic.quanta.exception.ApplicationException;
import fi.jubic.quanta.models.ColumnSelector;
import fi.jubic.quanta.models.DataConnection;
import fi.jubic.quanta.models.DataSeries;
import fi.jubic.quanta.models.OutputColumn;
import fi.jubic.quanta.models.Parameter;
import fi.jubic.quanta.models.Task;
import fi.jubic.quanta.models.TaskQuery;
import fi.jubic.quanta.models.WorkerDef;
import fi.jubic.quanta.models.WorkerDefColumn;
import org.jooq.Condition;
import org.jooq.Configuration;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;

import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fi.jubic.quanta.db.Tables.DATA_CONNECTION;
import static fi.jubic.quanta.db.Tables.DATA_SERIES;
import static fi.jubic.quanta.db.Tables.TASK;
import static fi.jubic.quanta.db.Tables.TASK_COLUMN_SELECTOR;
import static fi.jubic.quanta.db.Tables.TASK_OUTPUT_COLUMN;
import static fi.jubic.quanta.db.Tables.TASK_PARAMETER;
import static fi.jubic.quanta.db.Tables.WORKER_DEFINITION;
import static fi.jubic.quanta.db.Tables.WORKER_DEFINITION_COLUMN;

public class TaskDao {
    private final org.jooq.Configuration conf;
    private final WorkerDefDao workerDefDao;
    private final DataSeriesDao dataSeriesDao;

    @Inject
    TaskDao(
            fi.jubic.quanta.config.Configuration conf,
            WorkerDefDao workerDefDao,
            DataSeriesDao dataSeriesDao
    ) {
        this.conf = conf.getJooqConfiguration().getConfiguration();
        this.workerDefDao = workerDefDao;
        this.dataSeriesDao = dataSeriesDao;
    }

    public List<Task> search(TaskQuery query) {
        Condition condition = Stream
                .of(
                        query.getConnectionId().map(DATA_CONNECTION.ID::eq),
                        query.getWorkerDefId().map(WORKER_DEFINITION.ID::eq),
                        query.getTriggeredById().map(TASK.TASK_TRIGGER::eq),
                        query.getHasCronTrigger().map(
                                hasCronTrigger -> TASK.CRON_TRIGGER.isNotNull()
                        ),
                        query.getDataSeriesId().map(TASK.DATA_SERIES_ID::eq),
                        query.getNotDeleted().map(
                                hasCronTrigger -> TASK.DELETED_AT.isNull()
                        )
                )
                .filter(Optional::isPresent)
                .map(Optional::get)
                .reduce(Condition::and)
                .orElseGet(DSL::trueCondition);

        List<Task> tasks = DSL.using(conf).transactionResult(transaction -> DSL.using(transaction)
                .select()
                .from(TASK)
                .leftJoin(WORKER_DEFINITION)
                .on(TASK.WORKER_DEF_ID.eq(WORKER_DEFINITION.ID))
                .leftJoin(TASK_COLUMN_SELECTOR)
                .on(TASK_COLUMN_SELECTOR.TASK_ID.eq(TASK.ID))
                .leftJoin(WORKER_DEFINITION_COLUMN)
                .on(TASK_COLUMN_SELECTOR.WORKER_DEFINITION_COLUMN_ID
                        .eq(WORKER_DEFINITION_COLUMN.ID)
                )
                .leftJoin(DATA_SERIES)
                .on(TASK.DATA_SERIES_ID.eq(DATA_SERIES.ID))
                .leftJoin(DATA_CONNECTION)
                .on(DATA_SERIES.DATA_CONNECTION_ID.eq(DATA_CONNECTION.ID))
                .where(condition)
                .fetchStream()
                .collect(Task.mapper
                        .withWorkerDef(WorkerDef.mapper)
                        .withSeries(DataSeries.mapper.withDataConnection(DataConnection.mapper))
                        .collectingManyWithColumnSelectors(
                                ColumnSelector.taskColumnSelectorMapper
                                        .withSeries(
                                                DataSeries.mapper.withDataConnection(
                                                        DataConnection.mapper
                                                )
                                        )
                                        .withWorkerDefColumn(
                                                WorkerDefColumn.workerDefColumnMapper
                                        )
                        )
                )
                .stream()
                .map(task -> task.toBuilder()
                        .setWorkerDef(
                                Objects.nonNull(task.getWorkerDef())
                                        ? workerDefDao.getDetailsWithTransaction(
                                                task.getWorkerDef().getId(),
                                                transaction
                                        ).orElseThrow(NotFoundException::new)
                                        : null
                        )
                        .setSeries(
                                Objects.nonNull(task.getSeries())
                                        ? dataSeriesDao.getDetails(
                                        task.getSeries().getId(),
                                        transaction
                                ).orElseThrow(NotFoundException::new)
                                : null
                        )
                        .setOutputColumns(
                                getOutputColumns(
                                        task.getId(),
                                        transaction
                                )
                        )
                        .build()
                )
                .collect(Collectors.toList())
        );

        return getParameters(tasks, conf);
    }

    public Optional<Task> getDetails(Long id) {
        return getDetails(TASK.ID.eq(id), conf);
    }

    public Optional<Task> getDetails(Long id, Configuration transaction) {
        return getDetails(TASK.ID.eq(id), transaction);
    }

    public Optional<Task> getDetails(String name) {
        return getDetails(TASK.NAME.eq(name), conf);
    }

    private Optional<Task> getDetails(Condition condition, Configuration transaction) {
        Optional<Task> taskResult = DSL.using(transaction)
                .select()
                .from(TASK)
                .leftJoin(WORKER_DEFINITION)
                .on(TASK.WORKER_DEF_ID.eq(WORKER_DEFINITION.ID))
                .leftJoin(TASK_COLUMN_SELECTOR)
                .on(TASK_COLUMN_SELECTOR.TASK_ID.eq(TASK.ID))
                .leftJoin(WORKER_DEFINITION_COLUMN)
                .on(TASK_COLUMN_SELECTOR.WORKER_DEFINITION_COLUMN_ID
                        .eq(WORKER_DEFINITION_COLUMN.ID)
                )
                .leftJoin(DATA_SERIES)
                .on(TASK.DATA_SERIES_ID.eq(DATA_SERIES.ID))
                .leftJoin(DATA_CONNECTION)
                .on(DATA_SERIES.DATA_CONNECTION_ID.eq(DATA_CONNECTION.ID))
                .where(condition)
                .fetchStream()
                .collect(Task.mapper
                        .withWorkerDef(WorkerDef.mapper)
                        .withSeries(DataSeries.mapper.withDataConnection(DataConnection.mapper))
                        .collectingWithColumnSelectors(
                                ColumnSelector.taskColumnSelectorMapper
                                        .withSeries(
                                                DataSeries.mapper
                                                        .withDataConnection(DataConnection.mapper)
                                        )
                                        .withWorkerDefColumn(
                                                WorkerDefColumn.workerDefColumnMapper
                                        )
                        )
                )
                .map(task -> task
                        .toBuilder()
                        .setWorkerDef(
                                Objects.nonNull(task.getWorkerDef())
                                        ? workerDefDao.getDetailsWithTransaction(
                                                task.getWorkerDef().getId(),
                                                transaction
                                        ).orElseThrow(NotFoundException::new)
                                        : null
                        )
                        .setSeries(
                                Objects.nonNull(task.getSeries())
                                    ? dataSeriesDao.getDetails(
                                            task.getSeries().getId(),
                                            transaction
                                    ).orElseThrow(NotFoundException::new)
                                    : null
                        )
                        .setOutputColumns(
                                getOutputColumns(
                                        task.getId(),
                                        transaction
                                )
                        )
                        .build()
                );

        return taskResult.flatMap(task -> getParameters(task, transaction));
    }


    public Task create(Task task) {
        try {
            return DSL.using(conf).transactionResult(transaction -> {
                Long taskId = DSL.using(transaction)
                        .insertInto(TASK)
                        .set(
                                Task.mapper.write(
                                        DSL.using(conf).newRecord(TASK),
                                        task
                                )
                        )
                        .returning(TASK.ID)
                        .fetchOne()
                        .getId();

                if (!task.getColumnSelectors().isEmpty()) {
                    DSL.using(transaction)
                            .batchInsert(
                                    task.getColumnSelectors()
                                            .stream()
                                            .map(taskColumnSelector ->
                                                    ColumnSelector.taskColumnSelectorMapper.write(
                                                            DSL.using(conf).newRecord(
                                                                    TASK_COLUMN_SELECTOR
                                                            ),
                                                            taskColumnSelector
                                                    )
                                            )
                                            .peek(record -> record.setTaskId(taskId))
                                            .collect(Collectors.toList())
                            )
                            .execute();
                }

                if (!task.getOutputColumns().isEmpty()) {
                    DSL.using(transaction)
                            .batchInsert(
                                    task.getOutputColumns()
                                            .stream()
                                            .map(outputColumn ->
                                                    OutputColumn.taskOutputColumnMapper.write(
                                                            DSL.using(conf).newRecord(
                                                                    TASK_OUTPUT_COLUMN
                                                            ),
                                                            outputColumn
                                                    )
                                            )
                                            .peek(record -> record.setTaskId(taskId))
                                            .collect(Collectors.toList())
                            )
                            .execute();
                }

                if (task.getParameters() != null) {
                    DSL.using(transaction)
                            .batchInsert(
                                    task.getParameters()
                                            .stream()
                                            .map(parameter ->
                                                    Parameter.taskParameterRecordMapper.write(
                                                            DSL.using(conf).newRecord(
                                                                    TASK_PARAMETER
                                                            ),
                                                            parameter
                                                    )
                                            )
                                            .peek(record -> record.setTaskId(taskId))
                                            .collect(Collectors.toList())
                            )
                            .execute();
                }

                return getDetails(taskId, transaction)
                        .orElseThrow(IllegalStateException::new);
            });
        }
        catch (DataAccessException exception) {
            throw new ApplicationException("Could not create a Task", exception);
        }
    }

    public Task update(
            Long id,
            Function<Optional<Task>, Task> updater
    ) {
        try {
            return DSL.using(conf).transactionResult(transaction -> {
                Task task = updater.apply(getDetails(id));

                DSL.using(conf)
                        .update(TASK)
                        .set(
                                Task.mapper.write(
                                        DSL.using(conf).newRecord(TASK),
                                        task
                                )
                        )
                        .where(TASK.ID.eq(task.getId()))
                        .execute();

                if (task.getParameters() != null) {
                    task.getParameters()
                            .forEach(parameter -> {
                                TaskParameterRecord record = DSL.using(conf)
                                        .select()
                                        .from(TASK_PARAMETER)
                                        .where(TASK_PARAMETER.ID.eq(parameter.getId()))
                                        .fetchOneInto(TASK_PARAMETER);

                                Parameter.taskParameterRecordMapper.write(
                                        record,
                                        parameter
                                ).update();
                            });
                }

                // TODO: Update columns if necessary

                return getDetails(id)
                        .orElseThrow(IllegalStateException::new);
            });
        }
        catch (DataAccessException exception) {
            throw new ApplicationException("Could not update a Task", exception);
        }
    }

    private List<OutputColumn> getOutputColumns(
            Long taskId,
            org.jooq.Configuration transaction
    ) {
        return DSL.using(transaction)
                .select()
                .from(TASK_OUTPUT_COLUMN)
                .where(TASK_OUTPUT_COLUMN.TASK_ID.eq(taskId))
                .fetchStream()
                .collect(OutputColumn.taskOutputColumnMapper);
    }

    private List<Task> getParameters(
            List<Task> tasks,
            org.jooq.Configuration transaction
    ) {
        Map<Long, List<Parameter>> parameters =
                DSL.using(transaction)
                        .select()
                        .from(TASK_PARAMETER)
                        .where(TASK_PARAMETER.TASK_ID.in(
                                tasks.stream()
                                        .map(Task::getId)
                                        .collect(Collectors.toList())
                        ))
                        .fetch()
                        .stream()
                        .collect(
                                Collectors.groupingBy(
                                        record ->
                                                record.into(TASK_PARAMETER)
                                                        .getTaskId(),
                                        Parameter.taskParameterRecordMapper
                                )
                        );

        return tasks
                .stream()
                .map(task ->
                        task.toBuilder()
                                .setParameters(
                                        parameters.getOrDefault(
                                                task.getId(),
                                                Collections.emptyList()
                                        )
                                )
                                .build()
                )
                .collect(Collectors.toList());
    }

    private Optional<Task> getParameters(
            Task task,
            org.jooq.Configuration transaction
    ) {

        return getParameters(Collections.singletonList(task), transaction)
                .stream()
                .findFirst();
    }
}
