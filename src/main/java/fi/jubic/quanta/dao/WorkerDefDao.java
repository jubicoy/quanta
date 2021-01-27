package fi.jubic.quanta.dao;

import fi.jubic.quanta.config.Configuration;
import fi.jubic.quanta.exception.ApplicationException;
import fi.jubic.quanta.models.WorkerDef;
import fi.jubic.quanta.models.WorkerDefColumn;
import fi.jubic.quanta.models.WorkerDefQuery;
import fi.jubic.quanta.models.WorkerParameter;
import org.jooq.Condition;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fi.jubic.quanta.db.Tables.WORKER_DEFINITION;
import static fi.jubic.quanta.db.Tables.WORKER_DEFINITION_COLUMN;
import static fi.jubic.quanta.db.Tables.WORKER_PARAMETER;

public class WorkerDefDao {

    private final org.jooq.Configuration conf;

    @Inject
    public WorkerDefDao(Configuration conf) {
        this.conf = conf.getJooqConfiguration().getConfiguration();
    }

    public List<WorkerDef> search(WorkerDefQuery query) {
        Condition condition = Stream
                .of(
                        query.getNotDeleted().map(notDeleted ->
                                WORKER_DEFINITION.DELETED_AT.isNull()
                        ),
                        query.getNameLike().map(
                                nameLike -> WORKER_DEFINITION.NAME
                                        .lower()
                                        .contains(nameLike.toLowerCase())
                        )
                )
                .filter(Optional::isPresent)
                .map(Optional::get)
                .reduce(Condition::and)
                .orElseGet(DSL::trueCondition);

        List<WorkerDef> workerDefs = DSL.using(conf).transactionResult(transaction ->
                DSL.using(transaction)
                        .select()
                        .from(WORKER_DEFINITION)
                        .join(WORKER_DEFINITION_COLUMN)
                        .on(WORKER_DEFINITION_COLUMN.DEFINITION_ID.eq(WORKER_DEFINITION.ID))
                        .where(condition)
                        .fetchStream()
                        .collect(WorkerDef.mapper
                                .collectingManyWithColumns(
                                        WorkerDefColumn.workerDefColumnMapper
                                )
                        )
        );

        return getParameters(workerDefs, conf);
    }

    public Optional<WorkerDef> getDetails(Long workerDefId) {
        return getDetails(workerDefId, conf);
    }

    private Optional<WorkerDef> getDetails(
            Long workerDefId,
            org.jooq.Configuration transaction
    ) {
        return getBy(WORKER_DEFINITION.ID.eq(workerDefId), transaction);
    }

    public Optional<WorkerDef> getDetailsByName(
            String name,
            org.jooq.Configuration transaction
    ) {
        return getBy(
                WORKER_DEFINITION.NAME.eq(name),
                transaction
        );
    }

    public Optional<WorkerDef> getDetailsWithTransaction(
            Long workerDefId,
            org.jooq.Configuration transaction
    ) {
        return getDetails(workerDefId, transaction);
    }

    public WorkerDef create(
            WorkerDef workerDef,
            org.jooq.Configuration transaction
    ) {
        try {
            return DSL.using(transaction).transactionResult(result -> {
                        Long workerDefId = DSL.using(transaction)
                                .insertInto(WORKER_DEFINITION)
                                .set(
                                        WorkerDef.mapper.write(
                                                DSL.using(transaction)
                                                        .newRecord(WORKER_DEFINITION),
                                                workerDef
                                        ))
                                .returning(WORKER_DEFINITION.ID)
                                .fetchOne()
                                .getId();

                        DSL.using(result).batchInsert(
                                workerDef.getColumns()
                                        .stream()
                                        .map(column ->
                                                WorkerDefColumn
                                                        .workerDefColumnMapper
                                                        .write(
                                                                DSL.using(conf).newRecord(
                                                                        WORKER_DEFINITION_COLUMN
                                                                ),
                                                                column
                                                        )
                                        )
                                        .peek(record ->
                                                record.setDefinitionId(workerDefId)
                                        )
                                        .collect(Collectors.toList())
                        )
                                .execute();

                        if (workerDef.getParameters() != null) {
                            DSL.using(result).batchInsert(
                                    workerDef.getParameters()
                                            .stream()
                                            .map(
                                                    parameter ->
                                                            WorkerParameter.mapper.write(
                                                                    DSL.using(conf).newRecord(
                                                                            WORKER_PARAMETER
                                                                    ),
                                                                    parameter
                                                            )
                                            )
                                            .peek(
                                                    record -> record
                                                            .setWorkerDefinitionId(workerDefId)
                                            )
                                            .collect(Collectors.toList())
                            )
                                    .execute();
                        }

                        return getDetails(workerDefId, result)
                                .orElseThrow(IllegalStateException::new);
                    }
            );
        }
        catch (DataAccessException exception) {
            throw new ApplicationException("Could not create a Worker Definition", exception);
        }
    }

    public List<WorkerDef> getDefinitions() {
        return DSL.using(conf)
                .select()
                .from(WORKER_DEFINITION)
                .fetchStream()
                .collect(WorkerDef.mapper);
    }

    public Optional<WorkerDef> insert(WorkerDef workerDef) {
        try {
            return DSL.using(conf).transactionResult(transactionResult -> {
                Long workerDefId = DSL.using(transactionResult)
                        .insertInto(WORKER_DEFINITION)
                        .set(
                                WorkerDef.mapper.write(
                                        DSL.using(transactionResult).newRecord(WORKER_DEFINITION),
                                        workerDef
                        ))
                        .returning(WORKER_DEFINITION.ID)
                        .fetchOne()
                        .getId();

                DSL.using(transactionResult)
                        .batchInsert(
                                workerDef.getColumns()
                                        .stream()
                                        .map(inputColumn ->
                                                WorkerDefColumn.workerDefColumnMapper.write(
                                                        DSL.using(conf).newRecord(
                                                                WORKER_DEFINITION_COLUMN
                                                        ),
                                                        inputColumn
                                                )
                                        )
                                        .peek(record -> record.setDefinitionId(workerDefId))
                                        .collect(Collectors.toList())
                        )
                        .execute();

                if (workerDef.getParameters() != null) {
                    DSL.using(transactionResult)
                            .batchInsert(
                                    workerDef.getParameters()
                                            .stream()
                                            .map(parameter ->
                                                    WorkerParameter.mapper.write(
                                                            DSL.using(conf).newRecord(
                                                                    WORKER_PARAMETER
                                                            ),
                                                            parameter
                                                    )
                                            )
                                            .peek(record ->
                                                    record.setWorkerDefinitionId(workerDefId)
                                            )
                                            .collect(Collectors.toList())
                            )
                            .execute();
                }

                return getDetails(workerDefId, transactionResult);
            });
        } catch (DataAccessException exception) {
            throw new ApplicationException("Could not insert a Worker Definition", exception);
        }
    }

    private Optional<WorkerDef> getBy(
            Condition condition,
            org.jooq.Configuration transaction
    ) {
        Optional<WorkerDef> workerDef = DSL.using(transaction)
                    .select()
                    .from(WORKER_DEFINITION)
                    .leftJoin(WORKER_DEFINITION_COLUMN)
                    .on(WORKER_DEFINITION_COLUMN.DEFINITION_ID.eq(WORKER_DEFINITION.ID))
                    .where(condition)
                    .fetchStream()
                    .collect(WorkerDef.mapper
                            .collectingWithColumns(
                                    WorkerDefColumn.workerDefColumnMapper
                            )
                    );

        return workerDef.flatMap(def -> getParameters(def, transaction));

    }

    public WorkerDef update(
            Long id,
            Function<Optional<WorkerDef>, WorkerDef> updater
    ) {
        try {
            return DSL.using(conf).transactionResult(transaction -> {
                        WorkerDef workerDef = updater.apply(getDetails(id));

                        DSL.using(conf)
                                .update(WORKER_DEFINITION)
                                .set(
                                        WorkerDef.mapper.write(
                                                DSL.using(conf).newRecord(WORKER_DEFINITION),
                                                workerDef
                                        )
                                )
                                .where(WORKER_DEFINITION.ID.eq(workerDef.getId()))
                                .execute();

                        return getDetails(id)
                                .orElseThrow(IllegalStateException::new);
                    }
            );
        }
        catch (DataAccessException exception) {
            throw new ApplicationException("Could not update a Worker Definition", exception);
        }
    }

    private List<WorkerDef> getParameters(
            List<WorkerDef> workerDefs,
            org.jooq.Configuration transaction
    ) {
        Map<Long, List<WorkerParameter>> workerParameters =
                DSL.using(transaction)
                        .select()
                        .from(WORKER_PARAMETER)
                        .where(WORKER_PARAMETER.WORKER_DEFINITION_ID.in(
                                workerDefs.stream()
                                        .map(WorkerDef::getId)
                                        .collect(Collectors.toList())
                        ))
                        .fetch()
                        .stream()
                        .collect(
                                Collectors.groupingBy(
                                        record ->
                                                record.into(WORKER_PARAMETER)
                                                        .getWorkerDefinitionId(),
                                        WorkerParameter.mapper
                                )
                        );

        return workerDefs
                .stream()
                .map(workerDef ->
                        workerDef.toBuilder()
                                .setParameters(
                                        workerParameters.getOrDefault(
                                                workerDef.getId(),
                                                Collections.emptyList()
                                        )
                                )
                                .build()
                )
                .collect(Collectors.toList());
    }

    private Optional<WorkerDef> getParameters(
            WorkerDef workerDef,
            org.jooq.Configuration transaction) {

        return getParameters(Collections.singletonList(workerDef), transaction)
                .stream()
                .findFirst();
    }
}
