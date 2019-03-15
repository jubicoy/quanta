package fi.jubic.quanta.dao;

import fi.jubic.quanta.db.tables.records.SeriesTableRecord;
import fi.jubic.quanta.exception.ApplicationException;
import fi.jubic.quanta.models.DataSeries;
import fi.jubic.quanta.models.SeriesTable;
import org.jooq.Condition;
import org.jooq.Configuration;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static fi.jubic.quanta.db.Tables.DATA_SERIES;
import static fi.jubic.quanta.db.Tables.SERIES_TABLE;

@Singleton
public class SeriesTableDao {
    private final Configuration conf;

    @Inject
    SeriesTableDao(
            fi.jubic.quanta.config.Configuration configuration
    ) {
        this.conf = configuration.getJooqConfiguration().getConfiguration();
    }

    public void create(
            SeriesTable seriesTable,
            Configuration transaction
    ) {
        SeriesTableRecord record = SeriesTable.mapper.write(
                DSL.using(transaction).newRecord(SERIES_TABLE),
                seriesTable
        );
        record.store();
    }

    public void deleteWithTableName(
            String tableName,
            Configuration transaction
    ) {
        DSL.using(transaction)
                .delete(SERIES_TABLE)
                .where(SERIES_TABLE.TABLE_NAME.eq(tableName))
                .execute();
    }

    public SeriesTable update(
            String tableName,
            Function<Optional<SeriesTable>, SeriesTable> updater
    ) {
        try {
            return DSL.using(conf).transactionResult(transaction -> {
                SeriesTable seriesTable = updater.apply(
                        getSeriesTableByName(tableName, transaction)
                );

                DSL.using(conf)
                        .update(SERIES_TABLE)
                        .set(
                                SeriesTable.mapper.write(
                                        DSL.using(conf).newRecord(SERIES_TABLE),
                                        seriesTable
                                )
                        )
                        .where(SERIES_TABLE.TABLE_NAME.eq(seriesTable.getTableName()))
                        .execute();
                return getSeriesTableByName(tableName, transaction)
                        .orElseThrow(IllegalStateException::new);
            });
        }
        catch (DataAccessException exception) {
            throw new ApplicationException("Could not update a SeriesTable", exception);
        }
    }


    public List<SeriesTable> getSeriesTablesHasDeleteAt(
            Configuration transaction
    ) {
        return getSeriesTables(SERIES_TABLE.DELETE_AT.isNotNull(), transaction);
    }

    public List<SeriesTable> getSeriesTables(
            Configuration transaction
    ) {
        return getSeriesTables(SERIES_TABLE.DELETE_AT.isNull(), transaction);
    }

    private List<SeriesTable> getSeriesTables(
            Condition condition,
            Configuration transaction
    ) {
        return DSL.using(transaction)
                .select()
                .from(SERIES_TABLE)
                .leftJoin(DATA_SERIES)
                .on(SERIES_TABLE.DATA_SERIES_ID.eq(DATA_SERIES.ID))
                .where(condition)
                .fetchStream()
                .collect(
                        SeriesTable.mapper.withDataSeries(DataSeries.mapper)
                );
    }


    public Optional<SeriesTable> getSeriesTableByName(
            String tableName,
            Configuration transaction
    ) {
        return getSeriesTableBy(SERIES_TABLE.TABLE_NAME.eq(tableName), transaction);
    }

    private Optional<SeriesTable> getSeriesTableBy(
            Condition condition,
            Configuration transaction
    ) {
        return DSL.using(transaction)
                .select()
                .from(SERIES_TABLE)
                .leftJoin(DATA_SERIES)
                .on(SERIES_TABLE.DATA_SERIES_ID.eq(DATA_SERIES.ID))
                .where(condition)
                .fetchOptional()
                .map(record ->
                        SeriesTable.mapper
                                .withDataSeries(DataSeries.mapper)
                                .map(record)
                );
    }
}
