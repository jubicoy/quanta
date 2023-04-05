package fi.jubic.quanta.external.importer.jdbc;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import fi.jubic.quanta.config.Configuration;
import fi.jubic.quanta.exception.ApplicationException;
import fi.jubic.quanta.external.Importer;
import fi.jubic.quanta.models.Column;
import fi.jubic.quanta.models.DataConnection;
import fi.jubic.quanta.models.DataConnectionConfiguration;
import fi.jubic.quanta.models.DataConnectionType;
import fi.jubic.quanta.models.DataSample;
import fi.jubic.quanta.models.DataSeries;
import fi.jubic.quanta.models.DataSeriesConfiguration;
import fi.jubic.quanta.models.Type;
import fi.jubic.quanta.models.configuration.JdbcDataConnectionConfiguration;
import fi.jubic.quanta.models.configuration.JdbcDataSeriesConfiguration;
import fi.jubic.quanta.models.metadata.DataConnectionMetadata;
import fi.jubic.quanta.models.metadata.JdbcDataConnectionMetadata;
import fi.jubic.quanta.models.typemetadata.JdbcTypeMetadata;
import fi.jubic.quanta.models.typemetadata.TypeMetadata;
import fi.jubic.quanta.util.DateUtil;
import jakarta.ws.rs.NotAuthorizedException;
import org.apache.commons.io.FilenameUtils;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Singleton
public class JdbcImporter implements Importer {
    private final String jdbcPath;

    @Inject
    public JdbcImporter(Configuration configuration) {
        this.jdbcPath = configuration.getJdbcPath();
    }

    @SuppressFBWarnings(
            value = "PATH_TRAVERSAL_IN",
            justification = "DriverJar is verified."
    )
    private Connection getConnection(
            JdbcDataConnectionConfiguration configuration
    ) throws SQLException {
        Optional<java.sql.Driver> driver = Drivers.get(
                Paths.get(
                        jdbcPath,
                        FilenameUtils.getName(configuration.getDriverJar())
                ).toString(),
                configuration.getDriverClass()
        );
        if (driver.isPresent()) {
            // Register driver if available
            // Should still work if not
            DriverManager.registerDriver(driver.get());
        }

        return DriverManager.getConnection(
                configuration.getConnectionString(),
                configuration.getUsername(),
                configuration.getPassword()
        );
    }

    @Override
    public boolean test(DataConnection dataConnection) {
        JdbcDataConnectionConfiguration jdbcConfiguration = getConnectionConfiguration(
                dataConnection
        );
        try {
            return getConnection(jdbcConfiguration).isValid(1000);
        }
        catch (SQLException exception) {
            throw new NotAuthorizedException("Not Authorized");
        }
    }

    @Override
    public DataConnection validate(DataConnection dataConnection) {
        // TODO: Add validation if necessary
        return dataConnection;
    }



    @SuppressFBWarnings(
            value = {
                    "ODR_OPEN_DATABASE_RESOURCE",
                    "SQL_INJECTION_JDBC"
            },
            justification = "The method is accessing user-owned data with given queries."
    )
    @Override
    public DataSample getSample(DataSeries dataSeries, int rows) {
        final JdbcDataConnectionConfiguration jdbcConnectionConfig;
        jdbcConnectionConfig = getConnectionConfiguration(dataSeries.getDataConnection());
        final JdbcDataSeriesConfiguration jdbcSeriesConfig;
        jdbcSeriesConfig = getSeriesConfiguration(dataSeries);

        List<List<String>> data = new ArrayList<>();
        List<Column> columns = new ArrayList<>(dataSeries.getColumns());

        // Is this period in the format pattern below intentional ?
        DateTimeFormatter dateTimeFormatter = columns.isEmpty()
                ? DateUtil.dateTimeFormatter(null)
                : DateUtil.dateTimeFormatter(
                        columns.get(0).getType().getFormat()
        );
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

        String rawQuery = jdbcSeriesConfig.getQuery();
        String query = rawQuery.contains("$START") && rawQuery.contains("$END")
                ? rawQuery
                .replace(
                        "$START",
                        dateTimeFormatter.format(
                                Instant.parse("1970-01-01T00:00:00.00Z")
                        )
                )
                .replace(
                        "$END",
                        dateTimeFormatter.format(
                                Instant.parse("2100-12-31T23:59:59.00Z")
                        )
                )
                : rawQuery;

        try (
                Connection connection = getConnection(jdbcConnectionConfig);
                PreparedStatement select = setMaxRows(connection.prepareStatement(query), rows);
                ResultSet resultSet = select.executeQuery()
        ) {
            ResultSetMetaData resultMeta = resultSet.getMetaData();
            int columnCount = resultMeta.getColumnCount();
            while (resultSet.next()) {
                List<String> row = new ArrayList<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnType = resultSet.getMetaData().getColumnTypeName(i).toUpperCase();
                    switch (columnType) {
                        case "DATETIME":
                        case "TIMESTAMP":
                        case "TIMESTAMPTZ":
                            Timestamp timeStamp = resultSet.getTimestamp(i);
                            row.add(
                                    timeStamp != null
                                            ? dateTimeFormatter.format(
                                                            timeStamp.toInstant()
                                    )
                                            : null
                            );
                            break;
                        case "DATE":
                            Date date = resultSet.getDate(i);
                            row.add(
                                    date != null
                                            ? dateFormat.format(date)
                                            : null
                            );
                            break;
                        case "TIME":
                        case "TIMETZ":
                            Time time = resultSet.getTime(i);
                            row.add(
                                    time != null
                                            ? timeFormat.format(time)
                                            : null
                            );
                            break;
                        default:
                            row.add(resultSet.getString(i));
                            break;
                    }
                }
                data.add(row);
            }

            if (columns.isEmpty()) {
                // Auto-detect types
                for (int i = 1; i <= columnCount; i++) {
                    Class<?> className = String.class;
                    String format = null;
                    String columnType = resultMeta.getColumnTypeName(i).toUpperCase();
                    switch (columnType) {
                        case "BIT":
                            className = Boolean.class;
                            break;
                        case "NUMERIC":
                        case "DECIMAL":
                        case "REAL":
                        case "FLOAT":
                        case "FLOAT4":
                        case "FLOAT8":
                        case "DOUBLE":
                            className = Double.class;
                            break;
                        case "TINYINT":
                        case "SMALLINT":
                        case "INTEGER":
                        case "BIGINT":
                        case "INT2":
                        case "INT4":
                        case "INT8":
                            className = Long.class;
                            break;
                        case "DATE":
                            format = "yyyy-MM-dd";
                            className = Instant.class;
                            break;
                        case "TIME":
                        case "TIMETZ":
                            format = "HH:mm:ss";
                            className = Instant.class;
                            break;
                        case "DATETIME":
                        case "TIMESTAMP":
                        case "TIMESTAMPTZ":
                            format = "yyyy-MM-dd'T'HH:mm:ss.nnnnnnnnn'Z'";
                            className = Instant.class;
                            break;
                        default: break;
                    }

                    columns.add(Column.builder()
                            .setId(0L)
                            .setName(resultMeta.getColumnLabel(i))
                            .setType(Type.builder()
                                    .setFormat(format)
                                    .setClassName(className)
                                    .setNullable(
                                        resultMeta.isNullable(i) == ResultSetMetaData.columnNullable
                                    )
                                    .build()
                            )
                            .setIndex(i - 1)
                            .build()
                    );
                }

            }
        }
        catch (SQLException e) {
            throw new ApplicationException("Failed to get connection: " + e);
        }


        return DataSample.builder()
                .setDataSeries(
                        dataSeries.toBuilder()
                            .setColumns(columns)
                            .setDataConnection(
                                   
                                            Objects.requireNonNull(dataSeries.getDataConnection())
                                    
                            )
                            .build()
                )
                .setData(data)
                .build();
    }

    @Override
    @SuppressFBWarnings(
            value = {
                    "SQL_INJECTION_JDBC",
                    "ODR_OPEN_DATABASE_RESOURCE",
                    "OBL_UNSATISFIED_OBLIGATION"
            },
            justification = "The method is accessing user-owned data with given queries. "
                + "Closeables are handled, but spotbugs does not realize it."
    )
    public Stream<List<String>> getRows(DataSeries dataSeries) {
        final JdbcDataConnectionConfiguration jdbcConnectionConfig;
        jdbcConnectionConfig = getConnectionConfiguration(dataSeries.getDataConnection());
        final JdbcDataSeriesConfiguration jdbcSeriesConfig;
        jdbcSeriesConfig = getSeriesConfiguration(dataSeries);

        List<Column> columns = dataSeries.getColumns();

        DateTimeFormatter datetimeFormatter = columns.isEmpty()
                ? DateUtil.dateTimeFormatter(null)
                : DateUtil.dateTimeFormatter(
                        columns.get(0).getType().getFormat()
        );

        String rawQuery = jdbcSeriesConfig.getQuery();
        String query = rawQuery.contains("$START") && rawQuery.contains("$END")
                ? rawQuery
                .replace(
                        "$START",
                        datetimeFormatter.format(
                                Instant.parse("1970-01-01T00:00:00.00Z")
                        )
                )
                .replace(
                        "$END",
                        datetimeFormatter.format(
                                Instant.parse("2100-12-31T23:59:59.00Z")
                        )
                )
                : rawQuery;

        List<AutoCloseable> closeables = new ArrayList<>();
        Runnable onClose = () -> {
            try {
                for (AutoCloseable closeable : closeables) {
                    closeable.close();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        };

        try {
            Connection connection = getConnection(jdbcConnectionConfig);
            closeables.add(connection);

            PreparedStatement select = connection.prepareStatement(query);
            closeables.add(select);

            ResultSet resultSet = select.executeQuery();
            closeables.add(resultSet);

            int columnCount = resultSet.getMetaData().getColumnCount();

            return StreamSupport
                    .stream(
                            new Spliterators.AbstractSpliterator<List<String>>(
                                    100_000_000,
                                    Spliterator.ORDERED
                            ) {
                                @Override
                                public boolean tryAdvance(Consumer<? super List<String>> consumer) {
                                    try {
                                        if (!resultSet.next()) return false;
                                        consumer.accept(parseRow(resultSet, columnCount));
                                        return true;
                                    }
                                    catch (SQLException e) {
                                        throw new IllegalStateException(e);
                                    }
                                }
                            },
                            false
                    )
                    .onClose(onClose);
        }
        catch (SQLException e) {
            onClose.run();
            throw new ApplicationException("Failed to get connection: " + e);
        }
    }

    @SuppressFBWarnings(
            value = {
                    "SQL_INJECTION_JDBC",
                    "ODR_OPEN_DATABASE_RESOURCE",
                    "OBL_UNSATISFIED_OBLIGATION"
            },
            justification = "The method is accessing user-owned data with given queries. "
                    + "Closeables are handled, but spotbugs does not realize it."
    )
    public Stream<List<String>> getRows(
            DataSeries dataSeries,
            Instant start,
            Instant end
    ) {
        final JdbcDataConnectionConfiguration jdbcConnectionConfig;
        jdbcConnectionConfig = getConnectionConfiguration(dataSeries.getDataConnection());
        final JdbcDataSeriesConfiguration jdbcSeriesConfig;
        jdbcSeriesConfig = getSeriesConfiguration(dataSeries);

        DateTimeFormatter dateTimeFormatter = dataSeries.getColumns().isEmpty()
                ? DateUtil.dateTimeFormatter(null)
                : DateUtil.dateTimeFormatter(
                        dataSeries.getColumns().get(0).getType().getFormat()
        );

        String rawQuery = jdbcSeriesConfig.getQuery();
        String query = rawQuery.contains("$START") && rawQuery.contains("$END")
                ? rawQuery
                .replace(
                        "$START",
                        dateTimeFormatter.format(start)
                )
                .replace(
                        "$END",
                        dateTimeFormatter.format(end)
                )
                : rawQuery;

        List<AutoCloseable> closeables = new ArrayList<>();
        Runnable onClose = () -> {
            try {
                for (AutoCloseable closeable : closeables) {
                    closeable.close();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        };

        try {
            Connection connection = getConnection(jdbcConnectionConfig);
            closeables.add(connection);

            PreparedStatement select = connection.prepareStatement(query);
            closeables.add(select);

            ResultSet resultSet = select.executeQuery();
            closeables.add(resultSet);

            int columnCount = resultSet.getMetaData().getColumnCount();

            return StreamSupport
                    .stream(
                            new Spliterators.AbstractSpliterator<List<String>>(
                                    100_000_000,
                                    Spliterator.ORDERED
                            ) {
                                @Override
                                public boolean tryAdvance(Consumer<? super List<String>> consumer) {
                                    try {
                                        if (!resultSet.next()) return false;
                                        consumer.accept(parseRow(resultSet, columnCount));
                                        return true;
                                    }
                                    catch (SQLException e) {
                                        throw new IllegalStateException(e);
                                    }
                                }
                            },
                            false
                    )
                    .onClose(onClose);
        }
        catch (SQLException e) {
            onClose.run();
            throw new ApplicationException("Failed to get connection: " + e);
        }
    }

    @Override
    public CompletableFuture<Void> getRows(
            DataSeries dataSeries,
            Consumer<Stream<List<String>>> consumer,
            Instant start,
            Instant end
    ) {
        consumer.accept(getRows(dataSeries, start, end));
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @SuppressFBWarnings(
            value = "PATH_TRAVERSAL_IN",
            justification = "jdbcPath is get from server configuration, it's safe."
    )
    public TypeMetadata getMetadata(DataConnectionType ignored) {
        try {
            List<Driver> drivers = Files.list(Paths.get(jdbcPath))
                    .map(java.nio.file.Path::toString)
                    .filter(driverName -> driverName.endsWith(".jar"))
                    .map(Driver::of)
                    .collect(Collectors.toList());
            return TypeMetadata.builder()
                    .setType(DataConnectionType.JDBC)
                    .setJdbcTypeMetadata(
                            JdbcTypeMetadata.builder()
                                    .setDrivers(drivers)
                                    .build()
                    )
                    .build();
        }
        catch (IOException exception) {
            throw new IllegalStateException("Failed to get JDBC drivers", exception);
        }
    }

    @Override
    public DataConnectionMetadata getConnectionMetadata(DataConnection dataConnection) {
        return DataConnectionMetadata.builder()
                .setType(DataConnectionType.JDBC)
                .setJdbcDataConnectionMetadata(
                        JdbcDataConnectionMetadata.builder()
                                .setTables(
                                        getTables(dataConnection)
                                )
                                .build()
                )
                .build();
    }

    private JdbcDataConnectionConfiguration getConnectionConfiguration(
            @Nullable DataConnection dataConnection
    ) {
        return Optional.ofNullable(dataConnection)
                .orElseThrow(() -> new IllegalStateException("Called with partial state"))
                .getConfiguration()
                .visit(
                        new DataConnectionConfiguration
                                .DefaultFunctionVisitor<JdbcDataConnectionConfiguration>() {
                            @Override
                            public JdbcDataConnectionConfiguration onJdbc(
                                    JdbcDataConnectionConfiguration jdbcConfiguration
                            ) {
                                return jdbcConfiguration;
                            }

                            @Override
                            public JdbcDataConnectionConfiguration otherwise(
                                    DataConnectionConfiguration ignored
                            ) {
                                throw new IllegalStateException();
                            }
                        }
                );
    }

    private JdbcDataSeriesConfiguration getSeriesConfiguration(DataSeries dataSeries) {
        return dataSeries.getConfiguration()
                .visit(
                        new DataSeriesConfiguration
                                .DefaultFunctionVisitor<JdbcDataSeriesConfiguration>() {
                            @Override
                            public JdbcDataSeriesConfiguration onJdbc(
                                    JdbcDataSeriesConfiguration jdbcConfiguration
                            ) {
                                return jdbcConfiguration;
                            }

                            @Override
                            public JdbcDataSeriesConfiguration otherwise(
                                    DataSeriesConfiguration ignored
                            ) {
                                throw new IllegalStateException();
                            }
                        }
                );
    }

    private PreparedStatement setMaxRows(
            PreparedStatement select,
            int rows
    ) throws SQLException {
        select.setMaxRows(rows);
        return select;
    }

    private List<String> parseRow(ResultSet resultSet, int columnCount) throws SQLException {
        List<String> row = new ArrayList<>();
        for (int i = 1; i <= columnCount; i++) {
            String columnType = resultSet.getMetaData().getColumnTypeName(i).toUpperCase();
            if (
                    columnType.equals("DATETIME")
                            || columnType.equals("TIMESTAMP")
                            || columnType.equals("TIMESTAMPTZ")
            ) {
                Timestamp timeStamp = resultSet.getTimestamp(i);
                row.add(timeStamp != null
                                ? timeStamp.toInstant().toString()
                                : null
                );
            }
            else row.add(resultSet.getString(i));
        }
        return row;
    }

    private List<String> getTables(DataConnection dataConnection) {
        JdbcDataConnectionConfiguration jdbcConfiguration = getConnectionConfiguration(
                dataConnection
        );

        List<String> tables = new ArrayList<>();
        try (
                Connection connection = getConnection(jdbcConfiguration)
        ) {
            ResultSet resultSet = connection
                    .getMetaData()
                    .getTables(
                            null, null, "%", new String[] { "TABLE" }
                    );

            while (resultSet.next()) {
                if (resultSet.getString(4).equalsIgnoreCase("table")) {
                    tables.add(
                            String.format(
                                    "%s.%s",
                                    resultSet.getString("TABLE_SCHEM"),
                                    resultSet.getString("TABLE_NAME")
                            )
                    );
                }
            }
        }
        catch (SQLException e) {
            throw new ApplicationException("Failed to get connection: " + e);
        }

        return tables;
    }

}
