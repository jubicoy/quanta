package fi.jubic.quanta.dao;

import fi.jubic.quanta.db.tables.records.DataConnectionRecord;
import fi.jubic.quanta.exception.ApplicationException;
import fi.jubic.quanta.exception.InputException;
import fi.jubic.quanta.models.Column;
import fi.jubic.quanta.models.DataConnection;
import fi.jubic.quanta.models.DataConnectionConfiguration;
import fi.jubic.quanta.models.DataConnectionQuery;
import fi.jubic.quanta.models.DataConnectionType;
import fi.jubic.quanta.models.DataSeries;
import fi.jubic.quanta.models.configuration.CsvDataConnectionConfiguration;
import fi.jubic.quanta.models.configuration.JdbcDataConnectionConfiguration;
import fi.jubic.quanta.models.configuration.JsonIngestDataConnectionConfiguration;
import org.jooq.Condition;
import org.jooq.Record;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.NotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static fi.jubic.quanta.db.Tables.COLUMN;
import static fi.jubic.quanta.db.Tables.DATA_CONNECTION;
import static fi.jubic.quanta.db.Tables.DATA_SERIES;

@Singleton
public class DataConnectionDao {
    private final org.jooq.Configuration conf;

    @Inject
    DataConnectionDao(fi.jubic.quanta.config.Configuration conf) {
        this.conf = conf.getJooqConfiguration().getConfiguration();
    }

    public List<DataConnection> search(DataConnectionQuery query) {
        Condition condition = Stream
                .of(
                        query.getNotDeleted().map(notDeleted ->
                                DATA_CONNECTION.DELETED_AT.isNull()
                        )
                )
                .filter(Optional::isPresent)
                .map(Optional::get)
                .reduce(Condition::and)
                .orElseGet(DSL::trueCondition);

        return DSL.using(conf)
                .select()
                .from(DATA_CONNECTION)
                .where(condition)
                .fetchStream()
                .collect(DataConnection.mapper);
    }

    public Optional<DataConnection> getDetails(Long id) {
        List<Record> records = DSL.using(conf)
                .select()
                .from(DATA_CONNECTION)
                .leftJoin(DATA_SERIES)
                .on(DATA_CONNECTION.ID.eq(DATA_SERIES.DATA_CONNECTION_ID))
                .leftJoin(COLUMN)
                .on(DATA_SERIES.ID.eq(COLUMN.DATA_SERIES_ID))
                .where(DATA_CONNECTION.ID.eq(id))
                .fetch();

        if (records.size() == 0) {
            return Optional.empty();
        }
        if (records.size() == 1) {
            return Optional.of(
                    DataConnection.mapper.map(records.get(0))
            );
        }
        return records.stream()
                .collect(
                        DataConnection.mapper.collectingWithSeries(
                                DataSeries.mapper
                                        .collectingManyWithColumns(Column.seriesColumnMapper)
                        )
                );
    }

    public Optional<DataConnection> getDetailsByToken(String token) {
        DataConnection dataConnectionWithToken = search(new DataConnectionQuery())
                .stream()
                .filter(dataConnection ->
                        dataConnection.getType().equals(DataConnectionType.JSON_INGEST)
                )
                .filter(dataConnection -> {
                    JsonIngestDataConnectionConfiguration config = dataConnection
                            .getConfiguration()
                            .visit(new DataConnectionConfiguration
                                    .FunctionVisitor<JsonIngestDataConnectionConfiguration>() {
                                @Override
                                public JsonIngestDataConnectionConfiguration onCsv(
                                        CsvDataConnectionConfiguration csvConfiguration
                                ) {
                                    throw new InputException(
                                            "JSON_INGEST DataConnection has invalid configurations"
                                    );
                                }

                                @Override
                                public JsonIngestDataConnectionConfiguration onJdbc(
                                        JdbcDataConnectionConfiguration jdbcConfiguration
                                ) {
                                    throw new InputException(
                                            "JSON_INGEST DataConnection has invalid configurations"
                                    );
                                }

                                @Override
                                public JsonIngestDataConnectionConfiguration onJson(
                                        JsonIngestDataConnectionConfiguration jsonConfiguration
                                ) {
                                    return jsonConfiguration;
                                }
                            });
                    return config.getToken().equals(token);
                })
                .findFirst()
                .orElseThrow(NotFoundException::new);

        return getDetails(dataConnectionWithToken.getId());
    }

    public DataConnection create(DataConnection dataConnection) {
        return Optional
                .ofNullable(
                        DSL.using(conf)
                                .insertInto(DATA_CONNECTION)
                                .set(
                                        DataConnection.mapper.write(
                                                DSL.using(conf).newRecord(DATA_CONNECTION),
                                                dataConnection
                                        )
                                )
                                .returning(DATA_CONNECTION.ID)
                                .fetchOne()
                )
                .map(DataConnectionRecord::getId)
                .flatMap(this::getDetails)
                .orElseThrow(IllegalStateException::new);
    }

    public DataConnection update(
            Long id,
            Function<Optional<DataConnection>, DataConnection> updater
    ) {
        try {
            return DSL.using(conf).transactionResult(transaction -> {
                DataConnection dataConnection = updater.apply(getDetails(id));

                DSL.using(conf)
                        .update(DATA_CONNECTION)
                        .set(
                                DataConnection.mapper.write(
                                        DSL.using(conf).newRecord(DATA_CONNECTION),
                                        dataConnection
                                )
                        )
                        .where(DATA_CONNECTION.ID.eq(dataConnection.getId()))
                        .execute();

                return getDetails(id)
                        .orElseThrow(IllegalStateException::new);
            });
        }
        catch (DataAccessException exception) {
            throw new ApplicationException("Could not update a DataConnection", exception);
        }
    }
}

