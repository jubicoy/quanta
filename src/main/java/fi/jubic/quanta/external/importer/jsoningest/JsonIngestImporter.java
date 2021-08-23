package fi.jubic.quanta.external.importer.jsoningest;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;
import com.jayway.jsonpath.ReadContext;
import fi.jubic.quanta.exception.InputException;
import fi.jubic.quanta.external.Importer;
import fi.jubic.quanta.external.Ingester;
import fi.jubic.quanta.models.Column;
import fi.jubic.quanta.models.DataConnection;
import fi.jubic.quanta.models.DataConnectionType;
import fi.jubic.quanta.models.DataSample;
import fi.jubic.quanta.models.DataSeries;
import fi.jubic.quanta.models.DataSeriesConfiguration;
import fi.jubic.quanta.models.configuration.CsvDataSeriesConfiguration;
import fi.jubic.quanta.models.configuration.ImportWorkerDataSeriesConfiguration;
import fi.jubic.quanta.models.configuration.JdbcDataSeriesConfiguration;
import fi.jubic.quanta.models.configuration.JsonIngestDataSeriesConfiguration;
import fi.jubic.quanta.models.metadata.DataConnectionMetadata;
import fi.jubic.quanta.models.typemetadata.TypeMetadata;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Singleton
public class JsonIngestImporter implements Importer, Ingester {
    @Inject
    JsonIngestImporter() {

    }

    @Override
    public boolean test(DataConnection dataConnection) {
        return true;
    }

    @Override
    public DataConnection validate(DataConnection dataConnection) {
        return dataConnection;
    }

    @Override
    public DataConnection getWithEmptyLogin(DataConnection dataConnection) {
        return dataConnection;
    }

    @Override
    public DataSample getSample(DataSeries dataSeries, int rows) {
        return DataSample
                .builder()
                .setDataSeries(dataSeries)
                .setData(
                        getJsonRows(
                                dataSeries,
                                getJsonConfig(dataSeries).getSampleJsonDocument()
                        )
                                .stream()
                                .limit(rows)
                                .collect(Collectors.toList())
                )
                .build();
    }

    @Override
    public Stream<List<String>> getRows(DataSeries dataSeries) {
        return Stream.empty();
    }

    @Override
    public CompletableFuture<Void> getRows(
            DataSeries dataSeries,
            Consumer<Stream<List<String>>> consumer
    ) {
        consumer.accept(getRows(dataSeries));
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public List<List<String>> getIngestRows(DataSeries dataSeries, Object ingestPayload) {
        return getJsonRows(
                dataSeries,
                ingestPayload
        );
    }

    @Override
    public TypeMetadata getMetadata(DataConnectionType type) {
        return null;
    }

    @Override
    public DataConnectionMetadata getConnectionMetadata(DataConnection dataConnection) {
        return null;
    }

    private List<List<String>> getJsonRows(DataSeries dataSeries, Object jsonDocument) {
        LinkedHashMap<Column, String> mappings = getJsonColumnPathMappings(dataSeries);

        // Calculate how many rows are there
        // based on how many rows does time column have

        Map.Entry<Column, String> timeColumnEntry = mappings
                .entrySet()
                .stream()
                .findFirst()
                .orElseThrow(InputException::new);
        List<?> timeRows = getJsonRowsOfOneColumn(
                jsonDocument,
                timeColumnEntry.getValue(),
                timeColumnEntry.getKey().getType().getClassName(),
                dataSeries
        );

        if (timeRows == null) {
            throw new InputException("Can not find time rows!");
        }

        return IntStream
                .range(0, timeRows.size())
                .boxed()
                .map(
                        integer -> {
                            List<Object> row = getJsonRow(
                                            mappings,
                                            jsonDocument,
                                            integer,
                                            dataSeries
                                    );

                            return row
                                    .stream()
                                    .map(object -> Objects.isNull(object)
                                            ? "null"
                                            : object.toString()
                                    )
                                    .collect(Collectors.toList());
                        }
                )
                .collect(Collectors.toList());
    }

    public LinkedHashMap<Column, String> getJsonColumnPathMappings(DataSeries dataSeries) {
        List<Column> columns = dataSeries.getColumns();
        JsonIngestDataSeriesConfiguration jsonConfig = getJsonConfig(dataSeries);
        List<String> paths = jsonConfig.getPaths();

        if (columns.size() != paths.size()) {
            throw new InputException("JSON paths are inconsistent with Columns");
        }

        return IntStream.range(0, columns.size())
                .boxed()
                .collect(
                        Collectors.toMap(
                                columns::get,
                                paths::get,
                                (u, v) -> {
                                    throw new IllegalStateException(
                                            "Duplicate path found: " + u
                                    );
                                },
                                LinkedHashMap::new
                        )
                );
    }

    private JsonIngestDataSeriesConfiguration getJsonConfig(DataSeries dataSeries) {
        return dataSeries
                .getConfiguration()
                .visit(new DataSeriesConfiguration
                        .FunctionVisitor<JsonIngestDataSeriesConfiguration>() {
                    @Override
                    public JsonIngestDataSeriesConfiguration onCsv(
                            CsvDataSeriesConfiguration csvConfiguration
                    ) {
                        throw new InputException(
                                "JSON_INGEST DataSeries has invalid configurations"
                        );
                    }

                    @Override
                    public JsonIngestDataSeriesConfiguration onJdbc(
                            JdbcDataSeriesConfiguration jdbcConfiguration
                    ) {
                        throw new InputException(
                                "JSON_INGEST DataSeries has invalid configurations"
                        );
                    }

                    @Override
                    public JsonIngestDataSeriesConfiguration onJson(
                            JsonIngestDataSeriesConfiguration jsonConfiguration
                    ) {
                        return jsonConfiguration;
                    }

                    @Override
                    public JsonIngestDataSeriesConfiguration onImportWorker(
                            ImportWorkerDataSeriesConfiguration importWorkerConfiguration
                    ) {
                        throw new InputException(
                                "JSON_INGEST DataSeries has invalid configurations"
                        );
                    }
                });
    }

    private <T> List<T> getJsonRowsOfOneColumn(
            Object jsonDocument,
            String path,
            Class<T> className,
            DataSeries dataSeries
    ) {
        boolean isCollections = getJsonConfig(dataSeries).getIsCollections();
        Configuration configuration = Configuration
                .defaultConfiguration();


        ReadContext jsonContext = JsonPath
                .using(configuration)
                .parse(jsonDocument.toString());

        if (!isCollections) {
            try {
                return Collections.singletonList(
                        jsonContext
                                .read(
                                        path,
                                        className
                                )
                );
            }
            catch (NoSuchMethodError e) {
                throw new InputException(
                        "Invalid Json path: " + path
                );
            }
            catch (JsonPathException e) {
                throw new InputException(
                        String.format(
                                "Path '%s' return a value incompatible with class '%s' (%s)",
                                path,
                                className,
                                e.getMessage()
                        )
                );
            }
        }
        else {
            try {
                //noinspection unchecked
                return jsonContext
                        .read(
                                path,
                                List.class
                        );
            }
            catch (JsonPathException e) {
                throw new InputException(
                        "Invalid Json path: " + path
                );
            }
            catch (ClassCastException e) {
                throw new InputException(
                        "Path is not pointing to collection: " + path
                );
            }
        }
    }

    private List<Object> getJsonRow(
            LinkedHashMap<Column, String> mappings,
            Object jsonDocument,
            Integer rowIndex,
            DataSeries dataSeries
    ) {
        return mappings
                .entrySet()
                .stream()
                .map(entry -> {
                    Column column = entry.getKey();
                    Object result;
                    result = getJsonRowsOfOneColumn(
                            jsonDocument,
                            entry.getValue(),
                            column
                                    .getType()
                                    .getClassName(),
                            dataSeries
                    )
                            .get(rowIndex);

                    if (!column.getType().isNullable() && Objects.isNull(result)) {
                        throw new InputException(
                                "Unexpected null"
                        );
                    }

                    return result;
                })
                .collect(Collectors.toList());
    }
}
