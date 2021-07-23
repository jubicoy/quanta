package fi.jubic.quanta.dao;

import fi.jubic.quanta.models.Anomaly;
import fi.jubic.quanta.models.AnomalyAggregationSelector;
import fi.jubic.quanta.models.AnomalyFilter;
import fi.jubic.quanta.models.AnomalyGroupSelector;
import fi.jubic.quanta.models.AnomalyQuery;
import fi.jubic.quanta.models.AnomalyResult;
import fi.jubic.quanta.models.AnomalySelector;
import fi.jubic.quanta.models.Invocation;
import fi.jubic.quanta.models.Pagination;
import fi.jubic.quanta.models.TimeSeriesModifier;
import fi.jubic.quanta.models.TimeSeriesQuery;
import fi.jubic.quanta.util.DateUtil;
import fi.jubic.quanta.util.Json;
import org.jooq.Condition;
import org.jooq.Configuration;
import org.jooq.Field;
import org.jooq.impl.DSL;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fi.jubic.quanta.db.tables.Anomaly.ANOMALY;

@Singleton
public class AnomalyDao {
    private final Configuration conf;

    @Inject
    AnomalyDao(fi.jubic.quanta.config.Configuration configuration) {
        this.conf = configuration.getJooqConfiguration().getConfiguration();
    }

    public List<Anomaly> search(AnomalyQuery query) {
        Condition condition = Stream
                .of(
                        query.getInvocationId().map(ANOMALY.INVOCATION_ID::eq)
                )
                .filter(Optional::isPresent)
                .map(Optional::get)
                .reduce(Condition::and)
                .orElseGet(DSL::trueCondition);

        return DSL.using(conf)
                .selectFrom(ANOMALY)
                .where(condition)
                .fetchStream()
                .map(Anomaly.mapper::map)
                .collect(Collectors.toList());
    }

    private Condition buildCondition(
            List<AnomalyFilter> filters,
            TimeSeriesQuery query
    ) {
        return filters
                .stream()
                .map(
                        filter -> DSL.condition(
                                DSL.sql(
                                        String.format(
                                                "%s%s",
                                                filter.getColumn(),
                                                filter.getFilterCondition()
                                        )
                                )
                        )
                )
                .reduce(Condition::and)
                .orElseGet(DSL::trueCondition)
                .and(
                        ANOMALY.STARTING_TIME.greaterOrEqual(
                                DateUtil.toLocalDateTime(query.getStart())
                        )
                        .and(
                                ANOMALY.ENDING_TIME.lessOrEqual(
                                        DateUtil.toLocalDateTime(query.getEnd())
                                )
                        )
                );
    }

    private List<Field<?>> buildGrouping(
            List<AnomalyGroupSelector> groupings
    ) {
        return groupings
                .stream()
                .map(
                        grouping -> DSL.field(
                                DSL.sql(
                                        grouping.getColumnName()
                                )
                        )
                )
                .collect(Collectors.toList());
    }

    private List<Field<?>> buildAggregation(
            List<AnomalyAggregationSelector> aggregations
    ) {
        return aggregations
                .stream()
                .map(
                        aggregation -> DSL.field(
                                DSL.sql(
                                        String.format(
                                                "%s(%s)",
                                                aggregation.getModifier(),
                                                aggregation.getColumnName()
                                        )
                                )
                        )
                )
                .collect(Collectors.toList());
    }

    /**
     * Query anomaly.
     * @param invocationId invocation id
     * @param selectors anomaly selectors
     * @return anomalyResult
     */
    public AnomalyResult query(
            TimeSeriesQuery query,
            Long invocationId,
            AnomalySelector selectors,
            Pagination pagination
    ) {

        List<AnomalyAggregationSelector> distinct = selectors
                .getAggregations()
                .stream()
                .filter(
                        selector -> selector
                                .getModifier()
                                .equals(TimeSeriesModifier.distinct)
                )
                .collect(Collectors.toList());

        if (!distinct.isEmpty()) {
            return query(
                    invocationId,
                    buildAggregation(distinct),
                    buildCondition(
                            selectors.getFilters(),
                            query
                    ),
                    pagination
            );
        }

        if (!selectors.getAggregations().isEmpty()) {
            return query(
                    invocationId,
                    buildGrouping(selectors.getGroupings()),
                    buildAggregation(selectors.getAggregations()),
                    buildCondition(
                            selectors.getFilters(),
                            query
                    ),
                    pagination
            );
        }

        List<Anomaly> anomalies = DSL.using(conf)
                .selectFrom(ANOMALY)
                .where(
                        buildCondition(
                                selectors.getFilters(),
                                query
                        )
                                .and(
                                        ANOMALY.INVOCATION_ID.eq(invocationId)
                                )
                )
                .offset(pagination.getOffset().orElse(0))
                .limit(pagination.getLimit().orElse(Integer.MAX_VALUE))
                .fetchStream()
                .map(Anomaly.mapper::map)
                .collect(Collectors.toList());

        return AnomalyResult.builder()
                .setAnomalies(anomalies)
                .build();
    }

    /**
     * Query aggregations with grouping fields.
     * @param invocationId invocation id
     * @param groupings groupings fields
     * @param aggregations aggregations fields
     * @param condition conditions
     * @return anomalyResult
     */
    public AnomalyResult query(
            Long invocationId,
            List<Field<?>> groupings,
            List<Field<?>> aggregations,
            Condition condition,
            Pagination pagination
    ) {
        List<Field<?>> anchorFields = Stream.concat(
                aggregations.stream(),
                groupings.stream()
        ).collect(Collectors.toList());

        List<Map<String, Object>> aggAnomalies = DSL.using(conf)
                .select(
                        anchorFields
                )
                .from(ANOMALY)
                .where(
                        ANOMALY.INVOCATION_ID.eq(invocationId)
                                .and(condition)
                )
                .groupBy(
                        groupings
                )
                .offset(pagination.getOffset().orElse(0))
                .limit(pagination.getOffset().orElse(Integer.MAX_VALUE))
                .fetchMaps()
                .stream()
                .map(
                        AnomalyDao::mapToJson
                )
                .collect(Collectors.toList());


        return AnomalyResult.builder()
                .setAggregationAnomalies(aggAnomalies)
                .build();
    }

    /**
     * Query distinct value without grouping.
     * @param invocationId invocation id
     * @param distinct distinct fields
     * @param condition conditions
     * @return anomalyResult
     */
    public AnomalyResult query(
            Long invocationId,
            List<Field<?>> distinct,
            Condition condition,
            Pagination pagination
    ) {
        List<Map<String, Object>> distinctValues = DSL.using(conf)
                .select(
                        distinct
                )
                .from(ANOMALY)
                .where(
                        ANOMALY.INVOCATION_ID.eq(invocationId)
                                .and(condition)
                )
                .offset(pagination.getOffset().orElse(0))
                .limit(pagination.getOffset().orElse(Integer.MAX_VALUE))
                .fetchMaps()
                .stream()
                .map(
                        AnomalyDao::mapToJson
                )
                .collect(Collectors.toList());

        return AnomalyResult.builder()
                .setAggregationAnomalies(distinctValues)
                .build();
    }

    private static Map<String, Object> mapToJson(
            Map<String, Object> record
    ) {
        return record
                .entrySet()
                .stream()
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> Json.read(entry.getValue().toString(), Object.class)
                        )
                )
                .entrySet()
                .stream()
                .filter(
                        obj -> obj.getValue().isPresent()
                )
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> entry.getValue().get()
                        )
                );
    }


    public void create(List<Anomaly> anomalies, Invocation invocation) {
        DSL.using(conf).transaction(transaction -> {
            DSL.using(transaction)
                    .batchInsert(
                            anomalies.stream()
                                    .map(anomaly -> Anomaly.mapper.write(
                                            DSL.using(transaction).newRecord(ANOMALY),
                                            anomaly
                                    ))
                                    .peek(record -> record.setInvocationId(invocation.getId()))
                                    .collect(Collectors.toList())
                    )
                    .execute();
        });
    }
}
