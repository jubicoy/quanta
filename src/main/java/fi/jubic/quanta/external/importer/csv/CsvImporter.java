package fi.jubic.quanta.external.importer.csv;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import fi.jubic.quanta.config.Configuration;
import fi.jubic.quanta.exception.ApplicationException;
import fi.jubic.quanta.external.Importer;
import fi.jubic.quanta.external.importer.Types;
import fi.jubic.quanta.models.Column;
import fi.jubic.quanta.models.DataConnection;
import fi.jubic.quanta.models.DataConnectionConfiguration;
import fi.jubic.quanta.models.DataConnectionType;
import fi.jubic.quanta.models.DataSample;
import fi.jubic.quanta.models.DataSeries;
import fi.jubic.quanta.models.DataSeriesConfiguration;
import fi.jubic.quanta.models.Type;
import fi.jubic.quanta.models.configuration.CsvDataConnectionConfiguration;
import fi.jubic.quanta.models.configuration.CsvDataSeriesConfiguration;
import fi.jubic.quanta.models.metadata.DataConnectionMetadata;
import fi.jubic.quanta.models.typemetadata.CsvTypeMetadata;
import fi.jubic.quanta.models.typemetadata.TypeMetadata;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Singleton
public class CsvImporter implements Importer {
    private final String filePath;

    @Inject
    CsvImporter(Configuration configuration) {
        this.filePath = configuration.getFilePath();
    }

    @Override
    public boolean test(DataConnection dataConnection) {
        return true;
    }

    @Override
    public DataConnection validate(DataConnection dataConnection) {
        // TODO: Add validation if necessary
        return dataConnection;
    }

    @Override
    public DataConnection getWithEmptyLogin(DataConnection dataConnection) {
        return dataConnection;
    }

    @Override
    @SuppressFBWarnings(
            value = "PATH_TRAVERSAL_IN",
            justification = "Csv filename is not customizable AND also verified."
    )
    public DataSample getSample(DataSeries dataSeries, int rows) {
        final CsvDataConnectionConfiguration csvConnectionConfig;
        csvConnectionConfig = getConnectionConfiguration(dataSeries.getDataConnection());
        final CsvDataSeriesConfiguration csvSeriesConfig;
        csvSeriesConfig = getSeriesConfiguration(dataSeries);

        CSVFormat csvFormat;
        csvFormat = CSVFormat.DEFAULT
                .withDelimiter(csvSeriesConfig.getDelimiter())
                .withRecordSeparator(csvSeriesConfig.getSeparator())
                .withQuote(csvSeriesConfig.getQuote());
        if (csvSeriesConfig.getHeaders() == null) {
            csvFormat = csvFormat.withFirstRecordAsHeader();
        }

        CSVParser parser;
        try (
                Reader input = new InputStreamReader(
                        new FileInputStream(Paths.get(
                                filePath,
                                csvConnectionConfig.getInitialSyncFileName()
                        ).toFile()),
                        Charset.forName(csvSeriesConfig.getCharset())
                )

        ) {
            parser = csvFormat.parse(input);

            List<List<String>> sampleRows = StreamSupport.stream(parser.spliterator(), false)
                    .limit(rows)
                    .map(record -> IntStream.range(0, record.size())
                            .boxed()
                            .map(record::get)
                            .collect(Collectors.toList())
                    )
                    .collect(Collectors.toList());

            List<Column> columns = dataSeries.getColumns();

            if (columns.isEmpty()) {
                // Auto-detect types from CSVRecords
                Map<String, String> csvMap = parser.iterator().next().toMap(); // Map<Header, Data>

                List<String> headers = csvSeriesConfig.getHeaders();

                Map<String, Integer> headersMap;
                // If custom headers are provided
                if (headers != null && headers.size() > 0) {
                    headersMap = IntStream.range(0, headers.size())
                            .collect(
                                    HashMap::new,
                                    (m, i) -> m.put(headers.get(i), i),
                                    Map::putAll
                            );
                }
                else {
                    headersMap = parser.getHeaderMap();
                }
                // headersMap<String, Integer> where key is the header
                // and value is order from 0
                columns = headersMap
                        .entrySet()
                        .stream()
                        .map(header -> {
                            Map.Entry<Class<?>, String> classFormatPair = Types.of(
                                    csvMap.get(header.getKey())
                            ).guess();

                            return Column.builder()
                                            .setId(0L)
                                            .setName(header.getKey())
                                            .setType(
                                                    Type.builder()
                                                            .setFormat(classFormatPair.getValue())
                                                            .setClassName(classFormatPair.getKey())
                                                            .setNullable(false)
                                                            .build()
                                            )
                                            .setSeries(null)
                                            .setIndex(header.getValue())
                                            .build();

                        }).collect(Collectors.toList());

            }

            return DataSample.builder()
                    .setDataSeries(
                            dataSeries.toBuilder()
                                    .setColumns(columns)
                                    .build()
                    )
                    .setData(sampleRows)
                    .build();
        }
        catch (IOException e) {
            throw new ApplicationException("Can not read CSV file: " + e);
        }
    }

    @Override
    @SuppressFBWarnings(
            value = "PATH_TRAVERSAL_IN",
            justification = "Csv filename not customizable AND also verified."
    )
    public Stream<List<String>> getRows(DataSeries dataSeries) {
        final CsvDataConnectionConfiguration csvConnectionConfig;
        csvConnectionConfig = getConnectionConfiguration(dataSeries.getDataConnection());
        final CsvDataSeriesConfiguration csvSeriesConfig;
        csvSeriesConfig = getSeriesConfiguration(dataSeries);

        CSVFormat csvFormat;
        csvFormat = CSVFormat.DEFAULT
                .withDelimiter(csvSeriesConfig.getDelimiter())
                .withRecordSeparator(csvSeriesConfig.getSeparator())
                .withQuote(csvSeriesConfig.getQuote());
        if (csvSeriesConfig.getHeaders() == null) {
            csvFormat = csvFormat.withFirstRecordAsHeader();
        }

        Runnable onClose = () -> {};
        try {
            Reader input = new InputStreamReader(
                    new FileInputStream(Paths.get(
                            filePath,
                            csvConnectionConfig.getInitialSyncFileName()
                    ).toFile()),
                    Charset.forName(csvSeriesConfig.getCharset())
            );

            onClose = () -> {
                try {
                    input.close();
                }
                catch (IOException ignored) {

                }
            };

            CSVParser parser = csvFormat.parse(input);

            List<Column> columns = dataSeries.getColumns();
            if (columns == null || columns.isEmpty()) {
                columns = getSample(dataSeries, 5).getDataSeries().getColumns();
            }

            List<Integer> columnIndexes = columns
                    .stream()
                    .map(column -> column.getIndex())
                    .collect(Collectors.toList());

            return StreamSupport.stream(parser.spliterator(), false)
                    .map(r -> {
                        List<String> row = new ArrayList<>();
                        columnIndexes.stream().forEach(index ->
                                row.add(r.get(index))
                        );

                        return row;
                    })
                    .onClose(() -> {
                        try {
                            input.close();
                        }
                        catch (IOException e) {
                            throw new ApplicationException("Failed to close input stream: " + e);
                        }
                    });
        }
        catch (IOException e) {
            onClose.run();
            throw new ApplicationException("Can not read CSV file: " + e);
        }
    }

    @Override
    public TypeMetadata getMetadata(DataConnectionType ignored) {
        return TypeMetadata.builder()
                .setType(DataConnectionType.CSV)
                .setCsvTypeMetadata(CsvTypeMetadata.builder()
                        .setComment("CSV has no metadata.")
                        .build())
                .build();
    }

    @Override
    public DataConnectionMetadata getConnectionMetadata(DataConnection dataConnection) {
        // No metadata available. Maybe should use Optional?
        return null;
    }

    private CsvDataConnectionConfiguration getConnectionConfiguration(
            @Nullable DataConnection dataConnection
    ) {
        return Optional.ofNullable(dataConnection)
                .orElseThrow(() -> new IllegalStateException("Called with partial state"))
                .getConfiguration()
                .visit(
                        new DataConnectionConfiguration
                                .DefaultFunctionVisitor<CsvDataConnectionConfiguration>() {
                            @Override
                            public CsvDataConnectionConfiguration onCsv(
                                    CsvDataConnectionConfiguration csvConfiguration
                            ) {
                                return csvConfiguration;
                            }

                            @Override
                            public CsvDataConnectionConfiguration otherwise(
                                    DataConnectionConfiguration configuration
                            ) {
                                throw new IllegalStateException();
                            }
                        }
                );
    }

    private CsvDataSeriesConfiguration getSeriesConfiguration(DataSeries dataSeries) {
        return dataSeries.getConfiguration().visit(
                new DataSeriesConfiguration
                        .DefaultFunctionVisitor<CsvDataSeriesConfiguration>() {
                    @Override
                    public CsvDataSeriesConfiguration onCsv(
                            CsvDataSeriesConfiguration csvConfiguration
                    ) {
                        return csvConfiguration;
                    }

                    @Override
                    public CsvDataSeriesConfiguration otherwise(
                            DataSeriesConfiguration ignored
                    ) {
                        throw new IllegalStateException();
                    }
                }
        );
    }
}
