package fi.jubic.quanta.dao;

import fi.jubic.quanta.db.tables.records.InvocationRecord;
import fi.jubic.quanta.exception.ApplicationException;
import fi.jubic.quanta.models.ColumnSelector;
import fi.jubic.quanta.models.DataConnection;
import fi.jubic.quanta.models.DataSeries;
import fi.jubic.quanta.models.Invocation;
import fi.jubic.quanta.models.InvocationQuery;
import fi.jubic.quanta.models.InvocationStatus;
import fi.jubic.quanta.models.OutputColumn;
import fi.jubic.quanta.models.Pagination;
import fi.jubic.quanta.models.Parameter;
import fi.jubic.quanta.models.Task;
import fi.jubic.quanta.models.TaskType;
import fi.jubic.quanta.models.Worker;
import fi.jubic.quanta.models.WorkerDef;
import fi.jubic.quanta.models.WorkerDefColumn;
import org.jooq.Condition;
import org.jooq.Configuration;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;

import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fi.jubic.quanta.db.Tables.DATA_CONNECTION;
import static fi.jubic.quanta.db.Tables.DATA_SERIES;
import static fi.jubic.quanta.db.Tables.INVOCATION;
import static fi.jubic.quanta.db.Tables.INVOCATION_COLUMN_SELECTOR;
import static fi.jubic.quanta.db.Tables.INVOCATION_OUTPUT_COLUMN;
import static fi.jubic.quanta.db.Tables.INVOCATION_PARAMETER;
import static fi.jubic.quanta.db.Tables.TASK;
import static fi.jubic.quanta.db.Tables.WORKER;
import static fi.jubic.quanta.db.Tables.WORKER_DEFINITION;
import static fi.jubic.quanta.db.Tables.WORKER_DEFINITION_COLUMN;


public class InvocationDao {
    private final org.jooq.Configuration conf;
    private final WorkerDefDao workerDefDao;
    private final DataSeriesDao dataSeriesDao;

    @Inject
    InvocationDao(
            fi.jubic.quanta.config.Configuration conf,
            WorkerDefDao workerDefDao,
            DataSeriesDao dataSeriesDao) {
        this.conf = conf.getJooqConfiguration().getConfiguration();
        this.workerDefDao = workerDefDao;
        this.dataSeriesDao = dataSeriesDao;
    }

    public List<Invocation> search(InvocationQuery query) {
        return search(query, new Pagination());
    }

    public List<Invocation> search(
            InvocationQuery query,
            Pagination pagination
    ) {
        Condition condition = Stream
                .of(
                        query.getTaskId().map(TASK.ID::eq),
                        query.getWorkerId().map(WORKER.ID::eq),
                        query.getWorkerToken().map(WORKER.TOKEN::eq),
                        query.getStatus()
                                .map(Enum::name)
                                .map(INVOCATION.STATUS::eq)
                )
                .filter(Optional::isPresent)
                .map(Optional::get)
                .reduce(Condition::and)
                .orElseGet(DSL::trueCondition);

        List<Invocation> invocations = DSL.using(conf).transactionResult(transaction ->
                DSL.using(transaction)
                        .select()
                        .from(INVOCATION)
                        .leftJoin(TASK)
                        .on(INVOCATION.TASK_ID.eq(TASK.ID))
                        .leftJoin(WORKER)
                        .on(INVOCATION.WORKER_ID.eq(WORKER.ID))
                        .leftJoin(WORKER_DEFINITION)
                        .on(WORKER.DEFINITION_ID.eq(WORKER_DEFINITION.ID))
                        .leftJoin(INVOCATION_COLUMN_SELECTOR)
                        .on(INVOCATION.ID.eq(INVOCATION_COLUMN_SELECTOR.INVOCATION_ID))
                        .leftJoin(WORKER_DEFINITION_COLUMN)
                        .on(INVOCATION_COLUMN_SELECTOR.WORKER_DEFINITION_COLUMN_ID
                                .eq(WORKER_DEFINITION_COLUMN.ID)
                        )
                        .leftJoin(DATA_SERIES)
                        .on(TASK.DATA_SERIES_ID.eq(DATA_SERIES.ID))
                        .where(condition)
                        .limit(pagination.getLimit().orElse(10000))
                        .offset(pagination.getOffset().orElse(0))
                        .fetchStream()
                        .collect(
                                Invocation.mapper
                                        .withTask(Task.mapper
                                                .withWorkerDef(WorkerDef.mapper)
                                                .withSeries(DataSeries.mapper)
                                        )
                                        .withWorker(Worker.mapper.withDefinition(
                                                WorkerDef.mapper
                                                )
                                        )
                                        .collectingManyWithColumnSelectors(
                                                ColumnSelector.invocationColumnSelectorMapper
                                                        .withSeries(DataSeries.mapper
                                                                .withDataConnection(
                                                                        DataConnection.mapper
                                                                )
                                                        )
                                                        .withWorkerDefColumn(
                                                                WorkerDefColumn
                                                                        .workerDefColumnMapper
                                                        )
                                        )
                        )
                        .stream()
                        .map(invocation -> invocation.toBuilder()
                                .setWorker(
                                        Objects.nonNull(invocation.getWorker())
                                                ? invocation.getWorker()
                                                .toBuilder()
                                                .setDefinition(
                                                        workerDefDao.getDetailsWithTransaction(
                                                                invocation.getWorker()
                                                                        .getDefinition()
                                                                        .getId(),
                                                                transaction
                                                        ).orElseThrow(NotFoundException::new)
                                                )
                                                .build()
                                                : null
                                )
                                .setTask(
                                        Objects.nonNull(invocation.getTask())
                                                ? invocation.getTask()
                                                .toBuilder()
                                                .setSeries(invocation
                                                        .getTask()
                                                        .getSeries())
                                                .build()
                                                : null
                                )
                                .setOutputColumns(
                                        getInvocationOutputColumns(
                                                invocation.getId(),
                                                transaction
                                        )
                                )
                                .build()
                        )
                        .collect(Collectors.toList())
        );

        return getParameters(invocations, conf);
    }

    public Optional<Invocation> getDetails(Long id) {
        return getDetails(INVOCATION.ID.eq(id), conf);
    }

    public Optional<Invocation> getDetails(Long id, Configuration transaction) {
        return getDetails(INVOCATION.ID.eq(id), transaction);
    }

    public Optional<Invocation> getDetails(Long taskId, Long invocationNumber) {
        return getDetails(
                INVOCATION.TASK_ID.eq(taskId)
                        .and(INVOCATION.INVOCATION_NUMBER.eq(invocationNumber)),
                conf);
    }

    public Optional<Invocation> getDetails(
            Condition condition,
            Configuration transaction
    ) {
        Optional<Invocation> invocationResult = DSL.using(transaction)
                .select()
                .from(INVOCATION)
                .leftJoin(TASK)
                .on(INVOCATION.TASK_ID.eq(TASK.ID))
                .leftJoin(WORKER)
                .on(INVOCATION.WORKER_ID.eq(WORKER.ID))
                .leftJoin(WORKER_DEFINITION)
                .on(WORKER.DEFINITION_ID.eq(WORKER_DEFINITION.ID))
                .leftJoin(INVOCATION_COLUMN_SELECTOR)
                .on(INVOCATION.ID.eq(INVOCATION_COLUMN_SELECTOR.INVOCATION_ID))
                .leftJoin(WORKER_DEFINITION_COLUMN)
                .on(INVOCATION_COLUMN_SELECTOR.WORKER_DEFINITION_COLUMN_ID
                        .eq(WORKER_DEFINITION_COLUMN.ID)
                )
                .leftJoin(DATA_SERIES)
                .on(TASK.DATA_SERIES_ID.eq(DATA_SERIES.ID))
                .leftJoin(DATA_CONNECTION)
                .on(DATA_SERIES.DATA_CONNECTION_ID.eq(DATA_CONNECTION.ID))
                .where(condition)
                .fetchStream()
                .collect(
                        Invocation.mapper
                                .withTask(Task.mapper
                                        .withWorkerDef(WorkerDef.mapper)
                                        .withSeries(DataSeries.mapper
                                                .withDataConnection(DataConnection.mapper))
                                )
                                .withWorker(Worker.mapper.withDefinition(WorkerDef.mapper))
                                .collectingWithColumnSelectors(
                                        ColumnSelector.invocationColumnSelectorMapper
                                                .withSeries(DataSeries.mapper
                                                        .withDataConnection(DataConnection.mapper)
                                                )
                                                .withWorkerDefColumn(
                                                        WorkerDefColumn.workerDefColumnMapper
                                                )
                                )
                )
                .map(invocation -> invocation.toBuilder()
                        .setWorker(
                                Objects.nonNull(invocation.getWorker())
                                        ? invocation.getWorker()
                                        .toBuilder()
                                        .setDefinition(
                                                workerDefDao.getDetailsWithTransaction(
                                                        invocation.getWorker()
                                                                .getDefinition()
                                                                .getId(),
                                                        transaction
                                                ).orElseThrow(NotFoundException::new)
                                        )
                                        .build()
                                        : null
                        )
                        .setTask(
                                Objects.nonNull(invocation.getTask())
                                        ? invocation.getTask()
                                        .toBuilder()
                                        .setSeries(invocation
                                                .getTask()
                                                .getSeries())
                                        .build()
                                        : null
                        )
                        .setOutputColumns(
                                getInvocationOutputColumns(
                                        invocation.getId(),
                                        transaction
                                )
                        )
                        .build()
                );

        return invocationResult.flatMap(invocation -> getParameters(invocation, transaction));
    }

    public Long getLatestCompleteInvocationNumber(Long taskId) {
        return getLatestInvocationNumber(taskId, INVOCATION.STATUS.eq("Completed"), conf);
    }

    private Long getLatestInvocationNumber(
            Long taskId,
            Condition condition,
            Configuration transaction
    ) {
        AtomicReference<Long> maxInvocationNumber = new AtomicReference<>(0L);
        DSL.using(transaction)
                .select(DSL.max(INVOCATION.INVOCATION_NUMBER))
                .from(INVOCATION)
                .where(INVOCATION.TASK_ID.eq(taskId).and(condition))
                .fetchOptional(0, Long.class)
                .ifPresent(maxInvocationNumber::set);
        return maxInvocationNumber.get();
    }

    public Invocation create(Invocation invocation) {
        try {
            return DSL.using(conf).transactionResult(transaction -> {
                long nextInvocationNumber = getLatestInvocationNumber(
                        invocation.getTask().getId(),
                        DSL.noCondition(),
                        transaction
                ) + 1L;

                Long invocationId = Optional
                        .ofNullable(
                                DSL.using(transaction)
                                        .insertInto(INVOCATION)
                                        .set(
                                                Invocation.mapper.write(
                                                        DSL.using(transaction)
                                                                .newRecord(INVOCATION),
                                                        invocation.toBuilder()
                                                                .setInvocationNumber(
                                                                        nextInvocationNumber
                                                                )
                                                                .build()
                                                )
                                        )
                                        .returning(INVOCATION.ID)
                                        .fetchOne()
                        )
                        .map(InvocationRecord::getId)
                        .orElseThrow(IllegalStateException::new);

                if (!invocation.getColumnSelectors().isEmpty()) {
                    DSL.using(transaction)
                            .batchInsert(
                                    invocation.getColumnSelectors()
                                            .stream()
                                            .map(columnSelector ->
                                                    ColumnSelector.invocationColumnSelectorMapper
                                                            .write(DSL.using(transaction).newRecord(
                                                                    INVOCATION_COLUMN_SELECTOR
                                                                    ),
                                                                    columnSelector
                                                            )
                                            )
                                            .peek(record -> record
                                                    .setInvocationId(invocationId)
                                            )
                                            .collect(Collectors.toList())
                            )
                            .execute();
                }

                if (!invocation.getOutputColumns().isEmpty()) {
                    DSL.using(transaction)
                            .batchInsert(
                                    invocation.getOutputColumns()
                                            .stream()
                                            .map(outputColumn ->
                                                    OutputColumn
                                                            .invocationOutputColumnMapper.write(
                                                            DSL.using(transaction).newRecord(
                                                                    INVOCATION_OUTPUT_COLUMN
                                                            ),
                                                            outputColumn
                                                    )
                                            )
                                            .peek(record -> record.setInvocationId(invocationId))
                                            .collect(Collectors.toList())
                            )
                            .execute();
                }

                if (invocation.getParameters() != null) {
                    DSL.using(transaction)
                            .batchInsert(
                                    invocation.getParameters()
                                            .stream()
                                            .map(parameter ->
                                                    Parameter
                                                            .invocationParameterRecordMapper.write(
                                                            DSL.using(transaction).newRecord(
                                                                    INVOCATION_PARAMETER
                                                            ),
                                                            parameter
                                                    )
                                            )
                                            .peek(record ->
                                                    record.setInvocationId(invocationId)
                                            )
                                            .collect(Collectors.toList())
                            )
                            .execute();
                }

                return getDetails(invocationId, transaction)
                        .orElseThrow(IllegalStateException::new);
            });
        }
        catch (DataAccessException exception) {
            throw new ApplicationException("Could not store an Invocation", exception);
        }
    }

    public void createOutputColumns(Long invocationId, List<OutputColumn> columns) {
        try {
            DSL.using(conf).transactionResult(transaction -> DSL.using(transaction)
                    .batchInsert(
                            columns.stream()
                                    .map(outputColumn ->
                                            OutputColumn
                                                    .invocationOutputColumnMapper.write(
                                                    DSL.using(transaction).newRecord(
                                                            INVOCATION_OUTPUT_COLUMN
                                                    ),
                                                    outputColumn
                                            )
                                    )
                                    .peek(record -> record.setInvocationId(invocationId))
                                    .collect(Collectors.toList())
                    )
                    .execute());
        }
        catch (DataAccessException exception) {
            throw new ApplicationException("Could not store columns", exception);
        }
    }

    public void createColumnSelectors(Long invocationId, List<ColumnSelector> selectors) {
        try {
            if (!selectors.isEmpty()) {
                DSL.using(conf).transactionResult(transaction -> DSL.using(transaction)
                        .batchInsert(
                                selectors
                                        .stream()
                                        .map(columnSelector ->
                                                ColumnSelector.invocationColumnSelectorMapper
                                                        .write(DSL.using(transaction).newRecord(
                                                                INVOCATION_COLUMN_SELECTOR
                                                                ),
                                                                columnSelector
                                                        )
                                        )
                                        .peek(record -> record
                                                .setInvocationId(invocationId)
                                        )
                                        .collect(Collectors.toList())
                        )
                        .execute());
            }
        }
        catch (DataAccessException exception) {
            throw new ApplicationException("Could not store selectors", exception);
        }
    }

    public Invocation update(
            Long invocationId,
            Function<Optional<Invocation>, Invocation> updater
    ) {
        Invocation invocation = updater.apply(getDetails(invocationId));

        InvocationRecord record = Optional
                .ofNullable(
                        DSL.using(conf)
                                .select()
                                .from(INVOCATION)
                                .where(INVOCATION.ID.eq(invocationId))
                                .fetchOneInto(INVOCATION)
                )
                .orElseThrow(IllegalStateException::new);


        Invocation.mapper.write(
                record,
                invocation
        ).update();

        return getDetails(invocationId)
                .orElseThrow(IllegalStateException::new);
    }

    public List<OutputColumn> getInvocationOutputColumns(
            Long invocationId
    ) {
        return getInvocationOutputColumns(invocationId, conf);
    }

    private List<OutputColumn> getInvocationOutputColumns(
            Long invocationId,
            org.jooq.Configuration transaction
    ) {
        return DSL.using(transaction)
                .select()
                .from(INVOCATION_OUTPUT_COLUMN)
                .where(INVOCATION_OUTPUT_COLUMN.INVOCATION_ID.eq(invocationId))
                .fetchStream()
                .collect(OutputColumn.invocationOutputColumnMapper);
    }

    public List<Invocation> getLatestRunningOrPendingDataSyncInvocations() {
        return getLatestRunningOrPendingDataSyncInvocations(conf);
    }

    private List<Invocation> getLatestRunningOrPendingDataSyncInvocations(
            org.jooq.Configuration transaction
    ) {
        return DSL.using(transaction)
                .select()
                .from(INVOCATION)
                .leftJoin(TASK)
                .on(INVOCATION.TASK_ID.eq(TASK.ID))
                .leftJoin(WORKER)
                .on(INVOCATION.WORKER_ID.eq(WORKER.ID))
                .leftJoin(WORKER_DEFINITION)
                .on(WORKER.DEFINITION_ID.eq(WORKER_DEFINITION.ID))
                .leftJoin(INVOCATION_COLUMN_SELECTOR)
                .on(INVOCATION.ID.eq(INVOCATION_COLUMN_SELECTOR.INVOCATION_ID))
                .leftJoin(DATA_SERIES)
                .on(TASK.DATA_SERIES_ID.eq(DATA_SERIES.ID))
                .where(TASK.TASK_TYPE.eq(String.valueOf(TaskType.sync)))
                .fetchStream()
                .collect(
                        Invocation.mapper
                                .withTask(Task.mapper
                                        .withWorkerDef(WorkerDef.mapper)
                                        .withSeries(DataSeries.mapper
                                                .withDataConnection(DataConnection.mapper))
                                )
                                .withWorker(Worker.mapper.withDefinition(
                                        WorkerDef.mapper
                                        )
                                )
                                .collectingManyWithColumnSelectors(
                                        ColumnSelector.invocationColumnSelectorMapper
                                                .withSeries(DataSeries.mapper
                                                        .withDataConnection(DataConnection.mapper)
                                                )
                                )
                )
                .stream()
                .sorted(Comparator.comparingLong(Invocation::getInvocationNumber).reversed())
                .filter(distinctByKey(invocation -> invocation.getTask().getName()))
                .filter(invocation -> invocation.getStatus().equals(InvocationStatus.Running)
                        || invocation.getStatus().equals(InvocationStatus.Pending))
                .collect(Collectors.toList());
    }

    public static <T> Predicate<T> distinctByKey(
            Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    private List<Invocation> getParameters(
            List<Invocation> invocations,
            org.jooq.Configuration transaction
    ) {
        Map<Long, List<Parameter>> parameters =
                DSL.using(transaction)
                        .select()
                        .from(INVOCATION_PARAMETER)
                        .where(INVOCATION_PARAMETER.INVOCATION_ID.in(
                                invocations.stream()
                                        .map(Invocation::getId)
                                        .collect(Collectors.toList())
                        ))
                        .fetch()
                        .stream()
                        .collect(
                                Collectors.groupingBy(
                                        record ->
                                                record.into(INVOCATION_PARAMETER)
                                                        .getInvocationId(),
                                        Parameter.invocationParameterRecordMapper
                                )
                        );

        return invocations
                .stream()
                .map(invocation ->
                        invocation.toBuilder()
                                .setParameters(
                                        parameters.getOrDefault(
                                                invocation.getId(),
                                                Collections.emptyList()
                                        )
                                )
                                .build()
                )
                .collect(Collectors.toList());
    }

    private Optional<Invocation> getParameters(
            Invocation invocation,
            org.jooq.Configuration transaction
    ) {

        return getParameters(Collections.singletonList(invocation), transaction)
                .stream()
                .findFirst();
    }
}
