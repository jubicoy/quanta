package fi.jubic.quanta.dao;

import fi.jubic.quanta.external.importer.Types;
import fi.jubic.quanta.models.Column;
import fi.jubic.quanta.models.ColumnSelector;
import fi.jubic.quanta.models.DataSeries;
import fi.jubic.quanta.models.Measurement;
import fi.jubic.quanta.models.OutputColumn;
import fi.jubic.quanta.models.Pagination;
import fi.jubic.quanta.models.SeriesResult;
import fi.jubic.quanta.models.SeriesTable;
import fi.jubic.quanta.models.TimeSeriesColumnSelector;
import fi.jubic.quanta.models.TimeSeriesFilter;
import fi.jubic.quanta.models.TimeSeriesGroupSelector;
import fi.jubic.quanta.models.TimeSeriesModifier;
import fi.jubic.quanta.models.TimeSeriesQuery;
import fi.jubic.quanta.models.TimeSeriesResultOutputColumnSelector;
import fi.jubic.quanta.models.TimeSeriesResultOutputFilter;
import fi.jubic.quanta.models.TimeSeriesResultOutputGroupSelector;
import fi.jubic.quanta.models.TimeSeriesResultOutputSelector;
import fi.jubic.quanta.models.TimeSeriesSelector;
import fi.jubic.quanta.models.Type;
import fi.jubic.quanta.util.Sql;
import org.jooq.Condition;
import org.jooq.Configuration;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.NotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Singleton
public class TimeSeriesDao {
    private static final String TIMESTAMP_COLUMN = "0";
    private static final Field<Timestamp> TIMESTAMP_FIELD = DSL.field(
            DSL.name(TIMESTAMP_COLUMN),
            Timestamp.class
    );
    private static final Field<Timestamp> TIME_BUCKET_FIELD = DSL.field(
            DSL.name("time"),
            Timestamp.class
    );

    private final Configuration conf;
    private final SeriesTableDao seriesTableDao;

    @Inject
    TimeSeriesDao(
            fi.jubic.quanta.config.Configuration configuration,
            SeriesTableDao seriesTableDao
    ) {
        this.conf = configuration.getJooqConfiguration().getConfiguration();
        this.seriesTableDao = seriesTableDao;
    }

    public List<String> listTables(Pagination pagination) {
        return seriesTableDao.getSeriesTables(conf)
                .stream()
                .map(SeriesTable::getTableName)
                .collect(Collectors.toList());
    }

    public void createTable(
            DataSeries dataSeries,
            Configuration transaction
    ) {
        createTable(
                dataSeries.getTableName(),
                dataSeries.getColumns()
                        .stream()
                        .sorted(Comparator.comparingInt(Column::getIndex))
                        .collect(Collectors.toList()),
                transaction
        );

        seriesTableDao.create(
                SeriesTable.builder()
                        .setId(-1L)
                        .setTableName(dataSeries.getTableName())
                        .setDataSeries(dataSeries)
                        .setDeleteAt(null)
                        .build(),
                transaction
        );
    }

    private void createTable(
            String tableName,
            List<Column> columns,
            Configuration transaction
    ) {
        String command = String.format(
                "CREATE TABLE \"%s\" (%s);",
                Sql.sanitize(tableName),
                columns.stream()
                        .map(column -> String.format(
                                "\"%d\" %s %s",
                                column.getIndex(),
                                Types.getSqlType(column.getType().getClassName())
                                        .orElse("VARCHAR(255)"),
                                column.getType().isNullable() ? "" : "NOT NULL"
                        ))
                        .collect(Collectors.joining(", "))
        );

        DSL.using(transaction).execute(command);

        DSL.using(transaction).execute(
                String.format(
                        "SELECT create_hypertable('%s', '0')",
                        Sql.sanitize(tableName)
                )
        );
    }

    public void createTableWithOutputColumns(
            SeriesResult seriesResult,
            List<OutputColumn> outputColumns,
            Configuration transaction
    ) {
        createTableWithOutputColumns(
                seriesResult.getTableName(),
                outputColumns.stream()
                        .sorted(Comparator.comparingInt(OutputColumn::getIndex))
                        .collect(Collectors.toList()),
                transaction
        );
    }

    public void createTableWithOutputColumns(
            SeriesTable seriesTable,
            List<OutputColumn> outputColumns,
            Configuration transaction
    ) {
        createTableWithOutputColumns(
                seriesTable.getTableName(),
                outputColumns.stream()
                        .sorted(Comparator.comparingInt(OutputColumn::getIndex))
                        .collect(Collectors.toList()),
                transaction
        );
    }

    private void createTableWithOutputColumns(
            String tableName,
            List<OutputColumn> outputColumns,
            Configuration transaction
    ) {
        String command = String.format(
                "CREATE TABLE \"%s\" (%s);",
                Sql.sanitize(tableName),
                outputColumns.stream()
                        .map(column -> String.format(
                                "\"%s\" %s %s",
                                column.getIndex(),
                                Types.getSqlType(column.getType().getClassName())
                                        .orElse("VARCHAR(255)"),
                                column.getType().isNullable() ? "" : "NOT NULL"
                        ))
                        .collect(Collectors.joining(", "))
        );

        DSL.using(transaction).execute(command);

        DSL.using(transaction).execute(
                String.format(
                        "SELECT create_hypertable('%s', '0')",
                        Sql.sanitize(tableName)
                )
        );
    }

    public void deleteTableWithTableName(
            String tableName,
            Configuration transaction
    ) {
        deleteTable(tableName, transaction);
    }

    public void deleteTable(
            DataSeries dataSeries,
            Configuration transaction
    ) {
        deleteTable(dataSeries.getTableName(), transaction);
    }

    public void deleteTable(
            SeriesResult seriesResult,
            Configuration transaction
    ) {
        deleteTable(seriesResult.getTableName(), transaction);
    }

    private void deleteTable(
            String tableName,
            Configuration transaction
    ) {
        String command = String.format(
                "DROP TABLE \"%s\"",
                Sql.sanitize(tableName)
        );
        DSL.using(transaction).execute(command);
    }

    public void deleteRowsWithTableName(
            String tableName,
            String column,
            Instant start,
            Instant end,
            DateTimeFormatter dateTimeFormatter,
            Configuration transaction
    ) {
        deleteRows(
                tableName,
                column,
                start,
                end,
                dateTimeFormatter,
                transaction
        );
    }

    private void deleteRows(
            String tableName,
            String column,
            Instant start,
            Instant end,
            DateTimeFormatter dateTimeFormatter,
            Configuration transaction
    ) {
        String command = String.format(
                "DELETE FROM \"%s\" WHERE \"%s\" BETWEEN '%s' AND '%s'",
                Sql.sanitize(tableName),
                Sql.sanitize(column),
                dateTimeFormatter.format(start),
                dateTimeFormatter.format(end)
        );
        DSL.using(transaction).execute(command);
    }

    public void truncateTable(
            DataSeries dataSeries,
            Configuration transaction
    ) {
        truncateTable(dataSeries.getTableName(), transaction);
    }

    public void truncateTable(
            SeriesResult seriesResult,
            Configuration transaction
    ) {
        truncateTable(seriesResult.getTableName(), transaction);
    }

    private void truncateTable(
            String tableName,
            Configuration transaction
    ) {
        String command = String.format(
                "TRUNCATE TABLE \"%s\"",
                Sql.sanitize(tableName)
        );

        DSL.using(transaction).execute(command);
    }

    public long insertData(
            DataSeries dataSeries,
            Stream<List<String>> data
    ) {
        return insertData(dataSeries, data, conf);
    }

    public long insertData(
            DataSeries dataSeries,
            Stream<List<String>> data,
            Configuration transaction
    ) {
        return insertData(
                dataSeries.getTableName(),
                dataSeries.getColumns()
                        .stream()
                        .sorted(Comparator.comparingInt(Column::getIndex))
                        .collect(Collectors.toList()),
                data,
                transaction
        );
    }

    private long insertData(
            String tableName,
            List<Column> columns,
            Stream<List<String>> data,
            Configuration transaction
    ) {
        List<Type> types = columns.stream()
                .map(Column::getType)
                .collect(Collectors.toList());

        if (types.size() == 0) {
            throw new IllegalStateException();
        }

        String command = String.format(
                "COPY \"%s\" FROM STDIN WITH CSV",
                tableName
        );

        Stream<String> stream = data.map(value -> mapToSql(value, types))
                .filter(Optional::isPresent)
                .map(Optional::get);

        return DSL.using(transaction).connectionResult(connection -> {
            try (InputStream is = new StreamInputStream(stream)) {
                CopyManager copyManager = new CopyManager(
                        connection.unwrap(BaseConnection.class)
                );

                return copyManager.copyIn(command, is);
            }
            catch (IOException | SQLException exception) {
                throw new IllegalStateException(exception);
            }
        });
    }

    public long insertDataWithOutputColumns(
            SeriesResult seriesResult,
            List<OutputColumn> outputColumns,
            Stream<List<String>> data,
            Configuration transaction
    ) {
        return insertDataWithOutputColumns(
                seriesResult.getTableName(),
                outputColumns
                        .stream()
                        .sorted(Comparator.comparingInt(OutputColumn::getIndex))
                        .collect(Collectors.toList()),
                data,
                transaction
        );
    }

    public long insertDataWithOutputColumns(
            SeriesResult seriesResult,
            List<OutputColumn> outputColumns,
            Stream<List<String>> data
    ) {
        return insertDataWithOutputColumns(
                seriesResult.getTableName(),
                outputColumns
                        .stream()
                        .sorted(Comparator.comparingInt(OutputColumn::getIndex))
                        .collect(Collectors.toList()),
                data,
                conf
        );
    }

    public long insertDataWithOutputColumns(
            DataSeries dataSeries,
            List<OutputColumn> outputColumns,
            Stream<List<String>> data,
            Configuration transaction
    ) {
        return insertDataWithOutputColumns(
                dataSeries.getTableName(),
                outputColumns
                        .stream()
                        .sorted(Comparator.comparingInt(OutputColumn::getIndex))
                        .collect(Collectors.toList()),
                data,
                transaction
        );
    }

    public long insertDataWithOutputColumns(
            DataSeries dataSeries,
            List<OutputColumn> outputColumns,
            Stream<List<String>> data
    ) {
        return insertDataWithOutputColumns(
                dataSeries.getTableName(),
                outputColumns
                        .stream()
                        .sorted(Comparator.comparingInt(OutputColumn::getIndex))
                        .collect(Collectors.toList()),
                data,
                conf
        );
    }

    private long insertDataWithOutputColumns(
            String tableName,
            List<OutputColumn> outputColumns,
            Stream<List<String>> data,
            Configuration transaction
    ) {
        List<Type> types = outputColumns.stream()
                .map(OutputColumn::getType)
                .collect(Collectors.toList());

        if (types.size() == 0) {
            throw new IllegalStateException();
        }

        String command = String.format(
                "COPY \"%s\" FROM STDIN WITH CSV",
                tableName
        );

        Stream<String> stream = data.map(value -> mapToSql(value, types))
                .filter(Optional::isPresent)
                .map(Optional::get);

        return DSL.using(transaction).connectionResult(connection -> {
            try (InputStream is = new StreamInputStream(stream)) {
                CopyManager copyManager = new CopyManager(
                        connection.unwrap(BaseConnection.class)
                );

                return copyManager.copyIn(command, is);
            }
            catch (IOException | SQLException exception) {
                throw new IllegalStateException(exception);
            }
        });
    }

    public Stream<Measurement> select(DataSeries dataSeries, Pagination pagination) {
        return DSL.using(conf)
                .select()
                .from(DSL.table(DSL.name(dataSeries.getTableName())))
                .offset(pagination.getOffset().orElse(0))
                .limit(pagination.getLimit().orElse(1000000))
                .fetchStream()
                .map(record -> Measurement.builder()
                        .setTime(record.get(0, Instant.class))
                        .setValues(
                                IntStream.range(1, dataSeries.getColumns().size())
                                        .boxed()
                                        .collect(
                                                Collectors.toMap(
                                                        Object::toString,
                                                        index -> record.get(
                                                                index,
                                                                dataSeries.getColumns()
                                                                        .get(index)
                                                                        .getType()
                                                                        .getClassName()
                                                        )
                                                )
                                        )
                        ).build()
                );
    }

    public Map<List<?>, List<Measurement>> query(
            TimeSeriesQuery query,
            TimeSeriesSelector selection,
            DataSeries selectionDataSeries,
            Pagination pagination
    ) {
        Field<?> timeField;
        String selectorPrefix;
        String tableName;
        List<Column> columns = null;
        List<OutputColumn> outputColumns = null;

        List<TimeSeriesColumnSelector> selectColumns = selection.getColumnSelectors();
        List<TimeSeriesFilter> filters = selection.getFilters();

        List<TimeSeriesResultOutputColumnSelector> selectResultOutputColumns
                = selection.getResultOutputColumnSelectors();
        List<TimeSeriesResultOutputFilter> resultOutputFilters
                = selection.getResultOutputFilters();

        if (selection.getDataSeries() != null) {
            selectorPrefix = "series:" + selection.getDataSeries().getName();
            tableName = selection.getDataSeries().getTableName();
            columns = selection.getDataSeries().getColumns();
        }
        else if (selection.getSeriesResult() != null) {
            selectorPrefix = "result:"
                    + Objects.requireNonNull(selection.getSeriesResult().getInvocation())
                    .getTask().getName() + "."
                    + Objects.requireNonNull(selection.getSeriesResult().getInvocation())
                    .getInvocationNumber();
            tableName = selectionDataSeries.getTableName();
            columns = selectionDataSeries.getColumns();
        }
        else if (selection.getSeriesResultOutput() != null) {
            selectorPrefix = "result_output:"
                    + Objects.requireNonNull(selection.getSeriesResultOutput().getInvocation())
                    .getTask().getName() + "."
                    + Objects.requireNonNull(selection.getSeriesResultOutput().getInvocation())
                    .getInvocationNumber();
            tableName = selection.getSeriesResultOutput().getTableName();
            outputColumns = selection.getSeriesResultOutput()
                    .getInvocation()
                    .getOutputColumns();
        }
        else {
            throw new IllegalStateException();
        }

        if (
                (
                        selection.getSeriesResult() != null
                        || selection.getDataSeries() != null
                )
                && selection.isRawData()
        ) {
            // Raw data query
            return rawQuery(
                    TIMESTAMP_FIELD.as(TIME_BUCKET_FIELD),
                    selectorPrefix,
                    tableName,
                    selectColumns,
                    filters,
                    columns,
                    query,
                    pagination
            );
        }

        if (selection.getSeriesResultOutput() != null && selection.isRawWorkerData()) {
            // Raw worker data query
            return rawQueryResultOutputs(
                    TIMESTAMP_FIELD.as(TIME_BUCKET_FIELD),
                    selectorPrefix,
                    tableName,
                    selectResultOutputColumns,
                    outputColumns,
                    buildConditionResultOutput(resultOutputFilters, query),
                    pagination
            );
        }

        if (query.getIntervalSeconds() == 0L) {
            timeField = TIMESTAMP_FIELD.as(TIME_BUCKET_FIELD);
        }
        else {
            timeField = DSL.field(DSL.sql(
                    String.format(
                            "time_bucket('%d seconds', %s)",
                            query.getIntervalSeconds(),
                            DSL.name(TIMESTAMP_COLUMN)
                    )
            )).as(TIME_BUCKET_FIELD);
        }

        if (selection.getSeriesResultOutput() != null) {
            return queryResultOutputs(
                    timeField,
                    selectorPrefix,
                    tableName,
                    selection.getResultOutputGroupings(),
                    selectResultOutputColumns,
                    outputColumns,
                    buildConditionResultOutput(resultOutputFilters, query),
                    pagination
            );
        }
        return query(
                timeField,
                selectorPrefix,
                tableName,
                selection.getGroupings(),
                selectColumns,
                columns,
                buildCondition(filters, query),
                pagination
        );

    }

    /**.
     * Query TimeSeries with groupings by either column, time-bucket or both
     * @param selectorPrefix Prefix string to append column name in Measurements
     * @param tableName Table to SELECT data from
     * @param groupings List of columns to act as grouping parameters
     * @param selectColumns List of columns to be selected as data
     * @param columns Full list of columns in series
     * @param condition SQL Condition built from filters and query's time-range
     * @return Map with
     *     Keys: lists of grouping params' values
     *     Values: lists of Measurements
     */
    private Map<List<?>, List<Measurement>> query(
            Field<?> timeField,
            String selectorPrefix,
            String tableName,
            List<TimeSeriesGroupSelector> groupings,
            List<TimeSeriesColumnSelector> selectColumns,
            List<Column> columns,
            Condition condition,
            Pagination pagination
    ) {
        List<Field<?>> groupingFields = groupings.stream()
                .map(groupSelector ->
                        DSL.field(DSL.sql(
                                String.format(
                                        "\"%d\"",
                                        groupSelector
                                                .getColumn()
                                                .getIndex()
                                )
                        ))
                )
                .collect(Collectors.toList());

        // Result are grouped/ordered by these fields, thus "anchor"
        List<Field<?>> anchorFields = Stream.concat(
                groupingFields.stream(),
                Stream.of(
                        timeField
                )
        ).collect(Collectors.toList());

        List<Field<?>> selectFields = selectColumns.stream()
                .filter(columnSelector -> !Objects.equals(
                        columnSelector.getModifier(),
                        TimeSeriesModifier.distinct
                ))
                .map(columnSelector -> {
                    if (columnSelector.getModifier() != null) {
                        return DSL.field(DSL.sql(
                                String.format(
                                        "%s(\"%d\")",
                                        columnSelector.getModifier(),
                                        columnSelector.getColumn().getIndex()
                                )
                        ));
                    }
                    else {
                        return DSL.field(DSL.sql(
                                String.format(
                                        "\"%d\"",
                                        columnSelector.getColumn().getIndex()
                                )
                        ));
                    }
                })
                .collect(Collectors.toList());

        List<Field<?>> distinctFields = selectColumns.stream()
                .filter(columnSelector ->
                        Objects.equals(
                                columnSelector.getModifier(),
                                TimeSeriesModifier.distinct
                        )
                )
                .map(columnSelector ->
                        DSL.field(DSL.sql(
                                String.format(
                                        "\"%d\"",
                                        columnSelector.getColumn().getIndex()
                                )
                        ))
                ).collect(Collectors.toList());

        if (!selectFields.isEmpty() && !distinctFields.isEmpty()) {
            throw new IllegalArgumentException(
                    "Do not combine distinct aggregation with other selectors"
            );
        }

        if (!distinctFields.isEmpty()) {
            return queryDistinct(
                    selectorPrefix,
                    tableName,
                    anchorFields,
                    distinctFields,
                    groupingFields,
                    columns,
                    condition,
                    pagination
            );
        }

        return query(
                selectorPrefix,
                tableName,
                anchorFields,
                selectFields,
                groupingFields,
                columns,
                condition,
                pagination
        );
    }

    private Map<List<?>, List<Measurement>> query(
            String selectorPrefix,
            String tableName,
            List<Field<?>> anchorFields,
            List<Field<?>> selectFields,
            List<Field<?>> groupingFields,
            List<Column> columns,
            Condition condition,
            Pagination pagination
    ) {
        return DSL.using(conf)
                .select(
                        Stream.concat(
                                anchorFields.stream(),
                                selectFields.stream()
                        ).collect(Collectors.toList())
                )
                .from(DSL.table(DSL.name(tableName)))
                .where(condition)
                .groupBy(
                        anchorFields
                )
                .orderBy(
                        anchorFields
                                .stream()
                                .map(Field::asc)
                                .collect(Collectors.toList())
                )
                .offset(pagination.getOffset().orElse(0))
                .limit(pagination.getLimit().orElse(Integer.MAX_VALUE))
                .fetchGroups(
                        groupingFields.toArray(new Field[0])
                )
                .entrySet()
                .stream()
                .collect(
                        Collectors.toMap(
                                entry -> entry.getKey().intoList(),
                                entry -> entry.getValue()
                                        .stream()
                                        .map(
                                                record -> mapToMeasurement(
                                                        record,
                                                        columns,
                                                        anchorFields.size(),
                                                        selectFields,
                                                        selectorPrefix
                                                )
                                        )
                                        .filter(Optional::isPresent)
                                        .map(Optional::get)
                                        .collect(Collectors.toList())
                        )
                );
    }

    private Map<List<?>, List<Measurement>> queryDistinct(
            String selectorPrefix,
            String tableName,
            List<Field<?>> anchorFields,
            List<Field<?>> distinctFields,
            List<Field<?>> groupingFields,
            List<Column> columns,
            Condition condition,
            Pagination pagination
    ) {
        return  DSL.using(conf)
                .select(
                        Stream.concat(
                                anchorFields.stream(),
                                distinctFields.stream()
                        ).collect(Collectors.toList())
                )
                .distinctOn(distinctFields)
                .from(DSL.table(DSL.name(tableName)))
                .where(condition)
                .orderBy(
                        distinctFields
                )
                .offset(pagination.getOffset().orElse(0))
                .limit(pagination.getLimit().orElse(Integer.MAX_VALUE))
                .fetchGroups(
                        groupingFields.toArray(new Field[0])
                )
                .entrySet()
                .stream()
                .collect(
                        Collectors.toMap(
                                entry -> entry.getKey().intoList(),
                                entry -> entry.getValue()
                                        .stream()
                                        .map(
                                                record -> mapToMeasurement(
                                                        record,
                                                        columns,
                                                        anchorFields.size(),
                                                        distinctFields,
                                                        selectorPrefix
                                                )
                                        )
                                        .filter(Optional::isPresent)
                                        .map(Optional::get)
                                        .collect(Collectors.toList())
                        )
                );
    }

    /**.
     * Query TimeSeries with groupings by either column, time-bucket or both
     * @param selectorPrefix Prefix string to append column name in Measurements
     * @param tableName Table to SELECT data from
     * @param groupings List of columns to act as grouping parameters
     * @param selectColumns List of columns to be selected as data
     * @param outputColumns Full list of invocation output columns
     * @param condition SQL Condition built from filters and query's time-range
     * @return Map with
     *     Keys: lists of grouping params' values
     *     Values: lists of Measurements
     */
    private Map<List<?>, List<Measurement>> queryResultOutputs(
            Field<?> timeField,
            String selectorPrefix,
            String tableName,
            List<TimeSeriesResultOutputGroupSelector> groupings,
            List<TimeSeriesResultOutputColumnSelector> selectColumns,
            List<OutputColumn> outputColumns,
            Condition condition,
            Pagination pagination
    ) {
        List<Field<?>> groupingFields = groupings.stream()
                .map(groupSelector ->
                        DSL.field(DSL.sql(
                                String.format(
                                        "\"%d\"",
                                        groupSelector
                                                .getOutputColumn()
                                                .getIndex()
                                )
                        ))
                )
                .collect(Collectors.toList());

        // Result are grouped/ordered by these fields, thus "anchor"
        List<Field<?>> anchorFields = Stream.concat(
                groupingFields.stream(),
                Stream.of(
                        timeField
                )
        ).collect(Collectors.toList());

        List<Field<?>> selectFields = selectColumns.stream()
                .filter(columnSelector -> columnSelector.getOutputColumn().getIndex() != 0)
                .filter(columnSelector -> !Objects.equals(
                        columnSelector.getModifier(),
                        TimeSeriesModifier.distinct
                ))
                .map(columnSelector -> {
                    if (columnSelector.getModifier() != null) {
                        return DSL.field(DSL.sql(
                                String.format(
                                        "%s(\"%d\")",
                                        columnSelector.getModifier(),
                                        columnSelector
                                                .getOutputColumn()
                                                .getIndex()
                                )
                        ));
                    }
                    else {
                        return DSL.field(DSL.sql(
                                String.format(
                                        "\"%d\"",
                                        columnSelector
                                                .getOutputColumn()
                                                .getIndex()
                                )
                        ));
                    }
                })
                .collect(Collectors.toList());

        List<Field<?>> distinctFields = selectColumns.stream()
                .filter(
                        columnSelector ->
                                Objects.equals(
                                        columnSelector.getModifier(),
                                        TimeSeriesModifier.distinct
                                )
                )
                .map(columnSelector ->
                        DSL.field(DSL.sql(
                                String.format(
                                        "\"%d\"",
                                        columnSelector
                                                .getOutputColumn()
                                                .getIndex()
                                )
                        ))
                )
                .collect(Collectors.toList());

        if (!selectFields.isEmpty() && !distinctFields.isEmpty()) {
            throw new IllegalArgumentException(
                    "Do not combine distinct aggregation with other selectors"
            );
        }

        if (!distinctFields.isEmpty()) {
            return queryDistinctResultOutput(
                    selectorPrefix,
                    tableName,
                    anchorFields,
                    distinctFields,
                    groupingFields,
                    outputColumns,
                    condition,
                    pagination
            );
        }

        return queryResultOutput(
                selectorPrefix,
                tableName,
                anchorFields,
                selectFields,
                groupingFields,
                outputColumns,
                condition,
                pagination
        );
    }

    private Map<List<?>, List<Measurement>> queryResultOutput(
            String selectorPrefix,
            String tableName,
            List<Field<?>> anchorFields,
            List<Field<?>> selectFields,
            List<Field<?>> groupingFields,
            List<OutputColumn> outputColumns,
            Condition condition,
            Pagination pagination
    ) {
        return DSL.using(conf)
                .select(
                        Stream.concat(
                                anchorFields.stream(),
                                selectFields.stream()
                        ).collect(Collectors.toList())
                )
                .from(DSL.table(DSL.name(tableName)))
                .where(condition)
                .groupBy(
                        anchorFields
                )
                .orderBy(
                        anchorFields
                                .stream()
                                .map(Field::asc)
                                .collect(Collectors.toList())
                )
                .offset(pagination.getOffset().orElse(0))
                .limit(pagination.getLimit().orElse(Integer.MAX_VALUE))
                .fetchGroups(
                        groupingFields.toArray(new Field[0])
                )
                .entrySet()
                .stream()
                .collect(
                        Collectors.toMap(
                                entry -> entry.getKey().intoList(),
                                entry -> entry.getValue()
                                        .stream()
                                        .map(
                                                record -> mapToMeasurementWithOutputColumns(
                                                        record,
                                                        outputColumns,
                                                        anchorFields.size(),
                                                        selectFields,
                                                        selectorPrefix
                                                )
                                        )
                                        .filter(Optional::isPresent)
                                        .map(Optional::get)
                                        .collect(Collectors.toList())
                        )
                );
    }

    private Map<List<?>, List<Measurement>> queryDistinctResultOutput(
            String selectorPrefix,
            String tableName,
            List<Field<?>> anchorFields,
            List<Field<?>> distinctFields,
            List<Field<?>> groupingFields,
            List<OutputColumn> outputColumns,
            Condition condition,
            Pagination pagination
    ) {
        return DSL.using(conf)
                .select(
                        Stream.concat(
                                anchorFields.stream(),
                                distinctFields.stream()
                        ).collect(Collectors.toList())
                )
                .distinctOn(
                        distinctFields
                )
                .from(DSL.table(DSL.name(tableName)))
                .where(condition)
                .orderBy(
                        distinctFields
                )
                .offset(pagination.getOffset().orElse(0))
                .limit(pagination.getLimit().orElse(Integer.MAX_VALUE))
                .fetchGroups(
                        groupingFields.toArray(new Field[0])
                )
                .entrySet()
                .stream()
                .collect(
                        Collectors.toMap(
                                entry -> entry.getKey().intoList(),
                                entry -> entry.getValue()
                                        .stream()
                                        .map(
                                                record -> mapToMeasurementWithOutputColumns(
                                                        record,
                                                        outputColumns,
                                                        anchorFields.size(),
                                                        distinctFields,
                                                        selectorPrefix
                                                )
                                        )
                                        .filter(Optional::isPresent)
                                        .map(Optional::get)
                                        .collect(Collectors.toList())
                        )
                );
    }

    public Map<List<?>, List<Map<String, Object>>> queryToWorkerInputFormat(
            TimeSeriesQuery query,
            TimeSeriesSelector selection,
            List<ColumnSelector> columnSelectors,
            Boolean filterNonNumericalValues,
            Pagination pagination
    ) {
        Field<?> timeField;
        String selectorPrefix;
        String tableName;
        List<TimeSeriesColumnSelector> selectColumns = selection.getColumnSelectors();
        List<TimeSeriesFilter> filters = selection.getFilters();

        if (selection.getDataSeries() != null) {
            selectorPrefix = "series:" + selection.getDataSeries().getName();
            tableName = selection.getDataSeries().getTableName();
        }
        else {
            throw new IllegalStateException();
        }

        if (selection.isRawData()) {
            // Raw data query
            return rawQueryToWorkerInputFormat(
                    TIMESTAMP_FIELD.as(TIME_BUCKET_FIELD),
                    selectorPrefix,
                    tableName,
                    selectColumns,
                    filters,
                    columnSelectors,
                    query,
                    filterNonNumericalValues,
                    pagination
            );
        }

        if (query.getIntervalSeconds() == 0L) {
            timeField = TIMESTAMP_FIELD.as(TIME_BUCKET_FIELD);
        }
        else {
            timeField = DSL.field(DSL.sql(
                    String.format(
                            "time_bucket('%d seconds', %s)",
                            query.getIntervalSeconds(),
                            DSL.name(TIMESTAMP_COLUMN)
                    )
            )).as(TIME_BUCKET_FIELD);
        }

        return queryToWorkerInputFormat(
                timeField,
                selectorPrefix,
                tableName,
                selection.getGroupings(),
                selectColumns,
                columnSelectors,
                buildCondition(filters, query),
                filterNonNumericalValues,
                pagination
        );
    }

    /**.
     * Query TimeSeries with groupings by either column, time-bucket or both
     * @param selectorPrefix Prefix string to append column name in Measurements
     * @param tableName Table to SELECT data from
     * @param groupings List of columns to act as grouping parameters
     * @param selectColumns List of columns to be selected as data
     * @param columnSelectors Invocation's column selectors
     * @param condition SQL Condition built from filters and query's time-range
     * @param filterNonNumericalValues Mapping data to worker requires all the values
     * @return Map with
     *     Keys: lists of grouping params' values
     *     Values: lists of Measurements
     */
    private Map<List<?>, List<Map<String, Object>>> queryToWorkerInputFormat(
            Field<?> timeField,
            String selectorPrefix,
            String tableName,
            List<TimeSeriesGroupSelector> groupings,
            List<TimeSeriesColumnSelector> selectColumns,
            List<ColumnSelector> columnSelectors,
            Condition condition,
            Boolean filterNonNumericalValues,
            Pagination pagination
    ) {
        List<Field<?>> groupingFields = groupings.stream()
                .map(groupSelector ->
                        DSL.field(DSL.sql(
                                String.format(
                                        "\"%d\"",
                                        groupSelector
                                                .getColumn()
                                                .getIndex()
                                )
                        ))
                )
                .collect(Collectors.toList());

        // Result are grouped/ordered by these fields, thus "anchor"
        List<Field<?>> anchorFields = Stream.concat(
                groupingFields.stream(),
                Stream.of(
                        timeField
                )
        ).collect(Collectors.toList());

        List<Field<?>> selectFields = selectColumns.stream()
                .filter(columnSelector -> {
                    if (columnSelector.getColumn().getIndex() == 0) {
                        return false;
                    }
                    if (filterNonNumericalValues) {
                        Class<?> className = columnSelector.getColumn().getType().getClassName();
                        return className.equals(Integer.class)
                                || className.equals(Long.class)
                                || className.equals(Float.class)
                                || className.equals(Double.class);
                    }
                    return true;
                })
                .map(columnSelector -> {
                    if (columnSelector.getModifier() != null) {
                        return DSL.field(DSL.sql(
                                String.format(
                                        "%s(\"%d\")",
                                        columnSelector.getModifier(),
                                        columnSelector
                                                .getColumn()
                                                .getIndex()
                                )
                        ));
                    }
                    else {
                        return DSL.field(DSL.sql(
                                String.format(
                                        "\"%d\"",
                                        columnSelector
                                                .getColumn()
                                                .getIndex()
                                )
                        ));
                    }
                })
                .collect(Collectors.toList());

        return DSL.using(conf)
                .select(
                        Stream.concat(
                                anchorFields.stream(),
                                selectFields.stream()
                        ).collect(Collectors.toList())
                )
                .from(DSL.table(DSL.name(tableName)))
                .where(condition)
                .groupBy(
                        anchorFields
                )
                .orderBy(
                        anchorFields
                                .stream()
                                .map(Field::asc)
                                .collect(Collectors.toList())
                )
                .offset(pagination.getOffset().orElse(0))
                .limit(pagination.getLimit().orElse(Integer.MAX_VALUE))
                .fetchGroups(
                        groupingFields.toArray(new Field[0])
                )
                .entrySet()
                .stream()
                .collect(
                        Collectors.toMap(
                                entry -> entry.getKey().intoList(),
                                entry -> entry.getValue()
                                        .stream()
                                        .map(
                                                record -> mapToWorkerInputFormat(
                                                        record,
                                                        columnSelectors,
                                                        anchorFields.size(),
                                                        selectFields,
                                                        selectorPrefix
                                                )
                                        )
                                        .filter(Optional::isPresent)
                                        .map(Optional::get)
                                        .collect(Collectors.toList())
                        )
                );
    }

    public Map<List<?>, List<Measurement>> queryWorkerOutputs(
            TimeSeriesQuery query,
            TimeSeriesResultOutputSelector selection,
            List<OutputColumn> outputColumns,
            Boolean filterNonNumericalValues,
            Pagination pagination
    ) {
        Field<?> timeField;
        String selectorPrefix;
        String tableName;
        List<TimeSeriesResultOutputColumnSelector> selectResultOutputColumns
                = selection.getResultOutputColumnSelectors();

        // Filters aren't used here
        List<TimeSeriesResultOutputFilter> filters = Collections.emptyList();

        if (selection.getSeriesResult() != null) {
            selectorPrefix = "result_output:"
                    + Objects.requireNonNull(selection.getSeriesResult().getInvocation())
                    .getTask().getName() + "."
                    + Objects.requireNonNull(selection.getSeriesResult().getInvocation())
                    .getInvocationNumber();
            tableName = selection.getSeriesResult().getTableName();
        }
        else {
            throw new IllegalStateException();
        }

        if (selection.isRawData()) {
            // Raw data query
            return rawQueryResultOutputs(
                    TIMESTAMP_FIELD.as(TIME_BUCKET_FIELD),
                    selectorPrefix,
                    tableName,
                    selectResultOutputColumns,
                    outputColumns,
                    buildConditionResultOutput(filters, query),
                    pagination
            );
        }

        if (query.getIntervalSeconds() == 0L) {
            timeField = TIMESTAMP_FIELD.as(TIME_BUCKET_FIELD);
        }
        else {
            timeField = DSL.field(DSL.sql(
                    String.format(
                            "time_bucket('%d seconds', %s)",
                            query.getIntervalSeconds(),
                            DSL.name(TIMESTAMP_COLUMN)
                    )
            )).as(TIME_BUCKET_FIELD);
        }

        return queryResultOutputs(
                timeField,
                selectorPrefix,
                tableName,
                selection.getResultOutputGroupings(),
                selectResultOutputColumns,
                outputColumns,
                buildConditionResultOutput(filters, query),
                pagination
        );
    }

    private Map<List<?>, List<Measurement>> rawQuery(
            Field<?> timeField,
            String selectorPrefix,
            String tableName,
            List<TimeSeriesColumnSelector> selectColumns,
            List<TimeSeriesFilter> filters,
            List<Column> columns,
            TimeSeriesQuery query,
            Pagination pagination
    ) {
        List<Field<?>> selectFields = selectColumns.stream()
                .map(columnSelector -> {
                    if (columnSelector.getModifier() != null) {
                        throw new IllegalArgumentException("rawQuery can not have aggregation");
                    }
                    else {
                        return DSL.field(DSL.sql(
                                String.format(
                                        "\"%d\"",
                                        columnSelector.getColumn().getIndex()
                                )
                        ));
                    }
                })
                .collect(Collectors.toList());

        List<Measurement> measurements = DSL.using(conf)
                .select(
                        Stream.concat(
                                Stream.of(timeField),
                                selectFields.stream()
                        ).collect(Collectors.toList())
                )
                .from(DSL.table(DSL.name(tableName)))
                .where(buildCondition(filters, query))
                .orderBy(timeField.asc())
                .offset(pagination.getOffset().orElse(0))
                .limit(pagination.getLimit().orElse(Integer.MAX_VALUE))
                .fetchStream()
                .map(record -> mapToMeasurement(
                        record,
                        columns,
                        1,
                        selectFields,
                        selectorPrefix
                ))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        Map<List<?>, List<Measurement>> result = new HashMap<>();
        result.put(
                Collections.emptyList(),
                measurements
        );
        return result;
    }

    private Map<List<?>, List<Measurement>> rawQueryResultOutputs(
            Field<?> timeField,
            String selectorPrefix,
            String tableName,
            List<TimeSeriesResultOutputColumnSelector> selectColumns,
            List<OutputColumn> outputColumns,
            Condition condition,
            Pagination pagination
    ) {
        List<Field<?>> selectFields = selectColumns.stream()
                .filter(columnSelector -> columnSelector.getOutputColumn().getIndex() != 0)
                .map(columnSelector -> {
                    if (columnSelector.getModifier() != null) {
                        throw new IllegalArgumentException("rawQuery can not have aggregation");
                    }
                    else {
                        return DSL.field(DSL.sql(
                                String.format(
                                        "\"%d\"",
                                        columnSelector
                                                .getOutputColumn()
                                                .getIndex()
                                )
                        ));
                    }
                })
                .collect(Collectors.toList());

        List<Measurement> measurements = DSL.using(conf)
                .select(
                        Stream.concat(
                                Stream.of(timeField),
                                selectFields.stream()
                        ).collect(Collectors.toList())
                )
                .from(DSL.table(DSL.name(tableName)))
                .where(condition)
                .orderBy(timeField.asc())
                .offset(pagination.getOffset().orElse(0))
                .limit(pagination.getLimit().orElse(Integer.MAX_VALUE))
                .fetchStream()
                .map(record -> mapToMeasurementWithOutputColumns(
                        record,
                        outputColumns,
                        1,
                        selectFields,
                        selectorPrefix
                ))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        Map<List<?>, List<Measurement>> result = new HashMap<>();
        result.put(
                Collections.emptyList(),
                measurements
        );
        return result;
    }

    private Map<List<?>, List<Map<String, Object>>> rawQueryToWorkerInputFormat(
            Field<?> timeField,
            String selectorPrefix,
            String tableName,
            List<TimeSeriesColumnSelector> selectColumns,
            List<TimeSeriesFilter> filters,
            List<ColumnSelector> columnSelectors,
            TimeSeriesQuery query,
            Boolean filterNonNumericalValues,
            Pagination pagination
    ) {
        List<Field<?>> selectFields = selectColumns.stream()
                .filter(columnSelector -> {
                    if (columnSelector.getColumn().getIndex() == 0) {
                        return false;
                    }
                    if (filterNonNumericalValues) {
                        Class<?> className = columnSelector.getColumn().getType().getClassName();
                        return className.equals(Integer.class)
                                || className.equals(Long.class)
                                || className.equals(Float.class)
                                || className.equals(Double.class);
                    }
                    return true;
                })
                .map(columnSelector -> {
                    if (columnSelector.getModifier() != null) {
                        throw new IllegalArgumentException("rawQuery can not have aggregation");
                    }
                    else {
                        return DSL.field(DSL.sql(
                                String.format(
                                        "\"%d\"",
                                        columnSelector
                                                .getColumn()
                                                .getIndex()
                                )
                        ));
                    }
                }).collect(Collectors.toList());

        List<Map<String, Object>> values = DSL.using(conf)
                .select(
                        Stream.concat(
                                Stream.of(timeField),
                                selectFields.stream()
                        ).collect(Collectors.toList())
                )
                .from(DSL.table(DSL.name(tableName)))
                .where(buildCondition(filters, query))
                .orderBy(timeField.asc())
                .offset(pagination.getOffset().orElse(0))
                .limit(pagination.getLimit().orElse(Integer.MAX_VALUE))
                .fetchStream()
                .map(record -> mapToWorkerInputFormat(
                        record,
                        columnSelectors,
                        1,
                        selectFields,
                        selectorPrefix
                ))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        Map<List<?>, List<Map<String, Object>>> result = new HashMap<>();
        result.put(
                Collections.emptyList(),
                values
        );
        return result;
    }

    private Condition buildCondition(List<TimeSeriesFilter> filters, TimeSeriesQuery query) {
        return filters
                .stream()
                .map(filter ->
                        DSL.condition(
                                DSL.sql(
                                        String.format(
                                                "\"%d\"%s",
                                                filter.getColumn().getIndex(),
                                                filter.getFilterCondition()
                                        )
                                )
                        )
                )
                .reduce(Condition::and)
                .orElseGet(DSL::trueCondition)
                .and(
                        TIMESTAMP_FIELD.greaterOrEqual(Timestamp.from(query.getStart()))
                                .and(TIMESTAMP_FIELD.lessOrEqual(Timestamp.from(query.getEnd())))
                );
    }

    private Condition buildConditionResultOutput(
            List<TimeSeriesResultOutputFilter> filters,
            TimeSeriesQuery query
    ) {
        return filters
                .stream()
                .map(filter ->
                        DSL.condition(
                                DSL.sql(
                                        String.format(
                                                "\"%d\"%s",
                                                filter.getOutputColumn().getIndex(),
                                                filter.getFilterCondition()
                                        )
                                )
                        )
                )
                .reduce(Condition::and)
                .orElseGet(DSL::trueCondition)
                .and(
                        TIMESTAMP_FIELD.greaterOrEqual(Timestamp.from(query.getStart()))
                                .and(TIMESTAMP_FIELD.lessOrEqual(Timestamp.from(query.getEnd())))
                );
    }

    private Optional<String> mapToSql(List<String> values, List<Type> types) {
        return IntStream.range(0, types.size())
                .mapToObj(i -> {
                    Type type = types.get(i);
                    Optional<String> value = Optional.ofNullable(values.get(i));
                    if (!value.isPresent()) {
                        return Optional.of("");
                    }
                    return Types.getSqlValue(
                            type.getClassName(),
                            values.get(i),
                            type.getFormat()
                    );
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .reduce((a, b) -> {
                    if (b.isEmpty()) return String.format("%s,", a);
                    return String.format("%s,%s", a, b);
                });
    }

    private static Optional<Measurement> mapToMeasurement(
            Record record,
            List<Column> columns,
            int offset,
            List<Field<?>> selectFields,
            String selectorPrefix
    ) {
        Pattern pattern = Pattern.compile(
                "^(?:(avg|min|max|sum)\\()?\"([0-9]+)\"(?:\\))?$"
        );
        Map<String, Object> values = new HashMap<>();
        for (int selectIndex = 0; selectIndex < selectFields.size(); selectIndex++) {
            String name = selectFields.get(selectIndex).getName(); // max("2") or "2";
            Matcher m = pattern.matcher(name);
            if (!m.matches()) {
                throw new IllegalStateException("Can not parse SQL result");
            }
            int columnIndex = Integer.parseInt(Objects.requireNonNull(m.group(2)));
            Column columnOfIndex = columns
                    .stream()
                    .filter(column -> column
                            .getIndex()
                            .equals(columnIndex)
                    )
                    .findFirst()
                    .orElseThrow(NotFoundException::new);
            String valueKey;
            if (Objects.nonNull(m.group(1)) && !m.group(1).isEmpty()) {
                // Agg
                valueKey = String.format(
                        "%s(%s.%s)",
                        m.group(1),
                        selectorPrefix,
                        columnOfIndex.getName()
                );
            }
            else {
                valueKey = String.format(
                        "%s.%s",
                        selectorPrefix,
                        columnOfIndex.getName()
                );
            }
            values.put(
                    valueKey,
                    record.get(
                            offset + selectIndex,
                            columnOfIndex.getType().getClassName()
                    )
            );
        }

        if (values.isEmpty()) return Optional.empty();

        return Optional.of(Measurement.builder()
                .setTime(record.get(TIME_BUCKET_FIELD).toInstant())
                .setValues(values)
                .build());
    }

    private static Optional<Measurement> mapToMeasurementWithOutputColumns(
            Record record,
            List<OutputColumn> outputColumns,
            int offset,
            List<Field<?>> selectFields,
            String selectorPrefix
    ) {
        Pattern p = Pattern.compile(
                "^(?:(avg|min|max|sum)\\()?\"([0-9]+)\"(?:\\))?$"
        );
        Map<String, Object> values = new HashMap<>();
        for (int selectIndex = 0; selectIndex < selectFields.size(); selectIndex++) {
            String name = selectFields.get(selectIndex).getName(); // max("2") or "2";
            Matcher m = p.matcher(name);
            if (!m.matches()) {
                throw new IllegalStateException("Can not parse SQL result");
            }
            int outputColumnIndex = Integer.parseInt(Objects.requireNonNull(m.group(2)));
            OutputColumn outputColumnOfIndex = outputColumns
                    .stream()
                    .filter(outputColumn -> outputColumn
                            .getIndex()
                            .equals(outputColumnIndex)
                    )
                    .findFirst()
                    .orElseThrow(NotFoundException::new);
            String valueKey;
            if (Objects.nonNull(m.group(1)) && !m.group(1).isEmpty()) {
                // Agg
                if (Objects.nonNull(outputColumnOfIndex.getAlias())
                        && !outputColumnOfIndex.getAlias().isEmpty()) {
                    valueKey = String.format(
                            "%s(%s.%s) as %s",
                            m.group(1),
                            selectorPrefix,
                            outputColumnOfIndex.getColumnName(),
                            outputColumnOfIndex.getAlias()
                    );
                }
                else {
                    valueKey = String.format(
                            "%s(%s.%s)",
                            m.group(1),
                            selectorPrefix,
                            outputColumnOfIndex.getColumnName()
                    );
                }
            }
            else {
                if (Objects.nonNull(outputColumnOfIndex.getAlias())
                        && !outputColumnOfIndex.getAlias().isEmpty()) {
                    valueKey = String.format(
                            "%s.%s as %s",
                            selectorPrefix,
                            outputColumnOfIndex.getColumnName(),
                            outputColumnOfIndex.getAlias()
                    );
                }
                else {
                    valueKey = String.format(
                            "%s.%s",
                            selectorPrefix,
                            outputColumnOfIndex.getColumnName()
                    );
                }
            }
            values.put(
                    valueKey,
                    record.get(
                            offset + selectIndex,
                            outputColumnOfIndex.getType().getClassName()
                    )
            );
        }

        if (values.isEmpty()) return Optional.empty();

        return Optional.of(Measurement.builder()
                .setTime(record.get(TIME_BUCKET_FIELD).toInstant())
                .setValues(values)
                .build());
    }

    // Aliasing the data to worker input format e.g
    /*
        {
            "ds": 2020-01-01T00:00:00Z,
            "y": 10,
            "group" : "ABC"
        }
     */
    private static Optional<Map<String, Object>> mapToWorkerInputFormat(
            Record record,
            List<ColumnSelector> columnSelectors,
            int offset,
            List<Field<?>> selectFields,
            String selectorPrefix
    ) {
        Pattern p = Pattern.compile(
                "^(?:(avg|min|max|sum)\\()?\"([0-9]+)\"(?:\\))?$"
        );
        Map<String, Object> values = new HashMap<>();

        ColumnSelector timeColumnSelector = columnSelectors
                .stream()
                .filter(columnSelector -> columnSelector
                        .getColumnIndex()
                        .equals(0)
                )
                .findFirst()
                .orElseThrow(NotFoundException::new);

        if (Objects.nonNull(timeColumnSelector)) {
            values.put(
                    timeColumnSelector.getWorkerDefColumn().getName(),
                    record.get(TIME_BUCKET_FIELD).toInstant()
            );
        }
        else {
            values.put(
                    "time",
                    record.get(TIME_BUCKET_FIELD).toInstant()
            );
        }

        for (int selectIndex = 0; selectIndex < selectFields.size(); selectIndex++) {
            // max("2") or "2";
            String name = selectFields.get(selectIndex).getName();
            Matcher m = p.matcher(name);
            if (!m.matches()) {
                throw new IllegalStateException("Can not parse SQL result");
            }
            int columnSelectorIndex = Integer.parseInt(Objects.requireNonNull(m.group(2)));
            ColumnSelector columnSelectorOfIndex = columnSelectors
                    .stream()
                    .filter(columnSelector -> columnSelector
                            .getColumnIndex()
                            .equals(columnSelectorIndex)
                    )
                    .findFirst()
                    .orElseThrow(NotFoundException::new);

            String valueKey = columnSelectorOfIndex.getWorkerDefColumn().getName();

            values.put(
                    valueKey,
                    record.get(
                            offset + selectIndex,
                            columnSelectorOfIndex
                                    .getType()
                                    .getClassName()
                    )
            );
        }

        if (values.isEmpty()) return Optional.empty();

        return Optional.of(values);
    }
}
