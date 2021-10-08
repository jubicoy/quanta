package fi.jubic.quanta.dao;

import fi.jubic.quanta.db.tables.records.DataSeriesRecord;
import fi.jubic.quanta.exception.ApplicationException;
import fi.jubic.quanta.models.Column;
import fi.jubic.quanta.models.DataConnection;
import fi.jubic.quanta.models.DataSeries;
import fi.jubic.quanta.models.DataSeriesQuery;
import org.jooq.Condition;
import org.jooq.Configuration;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fi.jubic.quanta.db.Tables.COLUMN;
import static fi.jubic.quanta.db.Tables.DATA_CONNECTION;
import static fi.jubic.quanta.db.Tables.DATA_SERIES;

@Singleton
public class DataSeriesDao {
    private final Configuration conf;

    @Inject
    DataSeriesDao(fi.jubic.quanta.config.Configuration configuration) {
        this.conf = configuration.getJooqConfiguration().getConfiguration();
    }

    public List<DataSeries> search(DataSeriesQuery query) {
        Condition condition = Stream
                .of(
                        query.getNotDeleted().map(notDeleted ->
                                DATA_SERIES.DELETED_AT.isNull()
                        )
                )
                .filter(Optional::isPresent)
                .map(Optional::get)
                .reduce(Condition::and)
                .orElseGet(DSL::trueCondition);

        return DSL.using(conf)
                .select()
                .from(DATA_SERIES)
                .leftJoin(COLUMN)
                .on(DATA_SERIES.ID.eq(COLUMN.DATA_SERIES_ID))
                .leftJoin(DATA_CONNECTION)
                .on(DATA_SERIES.DATA_CONNECTION_ID.eq(DATA_CONNECTION.ID))
                .where(condition)
                .fetchStream()
                .collect(
                        DataSeries.mapper
                                .withDataConnection(DataConnection.mapper)
                                .collectingManyWithColumns(Column.seriesColumnMapper)
                );
    }

    public Optional<DataSeries> getDetails(Long dataSeriesId) {
        return getDetails(dataSeriesId, conf);
    }

    public Optional<DataSeries> getDetails(
            Long dataSeriesId,
            Configuration transaction
    ) {
        return getBy(DATA_SERIES.ID.eq(dataSeriesId), transaction);
    }

    public Map<Long, String> getTableNames(List<Long> dataSeriesIds) {
        if (dataSeriesIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return DSL.using(conf)
                .select(DATA_SERIES.ID, DATA_SERIES.TABLE_NAME)
                .from(DATA_SERIES)
                .where(DATA_SERIES.ID.in(dataSeriesIds))
                .fetchMap(DATA_SERIES.ID, DATA_SERIES.TABLE_NAME);
    }

    public Optional<DataSeries> getDetailsByName(String name) {
        return getBy(DATA_SERIES.NAME.eq(name), conf);
    }

    public DataSeries create(DataSeries series, Configuration transaction) {
        DataSeriesRecord record = DataSeries.mapper.write(
                DSL.using(transaction).newRecord(DATA_SERIES),
                series
        );
        record.store();

        DataSeries intermediateSeries = DataSeries.mapper.map(record);

        DSL.using(transaction)
                .batchInsert(
                        series.getColumns()
                                .stream()
                                .map(column -> column.toBuilder()
                                        .setSeries(intermediateSeries)
                                        .build())
                                .map(column -> Column.seriesColumnMapper.write(
                                        DSL.using(transaction).newRecord(COLUMN),
                                        column
                                ))
                                .collect(Collectors.toList())
                )
                .execute();

        return getDetails(record.getId(), transaction)
                    .orElseThrow(IllegalStateException::new);
    }

    public DataSeries update(
            Long id,
            Function<Optional<DataSeries>, DataSeries> updater,
            Configuration transaction
    ) {
        return updateSeries(
                id,
                updater,
                transaction
        );
    }

    public DataSeries update(
            Long id,
            Function<Optional<DataSeries>, DataSeries> updater
    ) {
        return DSL.using(conf).transactionResult(transaction -> updateSeries(
                id,
                updater,
                transaction
        ));
    }

    private DataSeries updateSeries(
            Long id,
            Function<Optional<DataSeries>, DataSeries> updater,
            Configuration transaction
    ) {
        try {
            DataSeries dataSeries = updater.apply(getDetails(id, transaction));

            DSL.using(transaction)
                    .update(DATA_SERIES)
                    .set(
                            DataSeries.mapper.write(
                                    DSL.using(transaction).newRecord(DATA_SERIES),
                                    dataSeries
                            )
                    )
                    .where(DATA_SERIES.ID.eq(dataSeries.getId()))
                    .execute();

            return getDetails(id, transaction)
                    .orElseThrow(IllegalStateException::new);
        }
        catch (DataAccessException exception) {
            throw new ApplicationException("Could not update a DataSeries", exception);
        }
    }

    private Optional<DataSeries> getBy(Condition condition, Configuration transaction) {
        return DSL.using(transaction)
                .select()
                .from(DATA_SERIES)
                .leftJoin(COLUMN)
                .on(DATA_SERIES.ID.eq(COLUMN.DATA_SERIES_ID))
                .leftJoin(DATA_CONNECTION)
                .on(DATA_SERIES.DATA_CONNECTION_ID.eq(DATA_CONNECTION.ID))
                .where(condition)
                .fetchStream()
                .collect(
                        DataSeries.mapper
                                .withDataConnection(DataConnection.mapper)
                                .collectingWithColumns(Column.seriesColumnMapper)
                );
    }
}
