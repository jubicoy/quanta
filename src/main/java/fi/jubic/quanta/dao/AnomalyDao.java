package fi.jubic.quanta.dao;

import fi.jubic.quanta.models.Anomaly;
import fi.jubic.quanta.models.AnomalyQuery;
import fi.jubic.quanta.models.Invocation;
import org.jooq.Condition;
import org.jooq.Configuration;
import org.jooq.impl.DSL;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fi.jubic.quanta.db.tables.DetectionResult.DETECTION_RESULT;

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
                        query.getInvocationId().map(DETECTION_RESULT.INVOCATION_ID::eq)
                )
                .filter(Optional::isPresent)
                .map(Optional::get)
                .reduce(Condition::and)
                .orElseGet(DSL::trueCondition);

        return DSL.using(conf)
                .selectFrom(DETECTION_RESULT)
                .where(condition)
                .fetchStream()
                .map(Anomaly.mapper::map)
                .collect(Collectors.toList());
    }

    public void create(List<Anomaly> anomalies, Invocation invocation) {
        DSL.using(conf).transaction(transaction -> {
            DSL.using(transaction)
                    .batchInsert(
                            anomalies.stream()
                                    .map(anomaly -> Anomaly.mapper.write(
                                            DSL.using(transaction).newRecord(DETECTION_RESULT),
                                            anomaly
                                    ))
                                    .peek(record -> record.setInvocationId(invocation.getId()))
                                    .collect(Collectors.toList())
                    )
                    .execute();
        });
    }
}
