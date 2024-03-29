package fi.jubic.quanta.external;

import fi.jubic.quanta.external.importer.Types;
import fi.jubic.quanta.models.DataConnection;
import fi.jubic.quanta.models.DataConnectionType;
import fi.jubic.quanta.models.DataSample;
import fi.jubic.quanta.models.DataSeries;
import fi.jubic.quanta.models.metadata.DataConnectionMetadata;
import fi.jubic.quanta.models.typemetadata.TypeMetadata;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface Importer {
    boolean test(DataConnection dataConnection);

    DataConnection validate(DataConnection dataConnection);


    DataSample getSample(DataSeries dataSeries, int rows);

    default Stream<List<String>> getRows(DataSeries dataSeries) {
        List<List<String>> rows = new ArrayList<>();
        getRows(
                dataSeries,
                batch -> rows.addAll(batch.collect(Collectors.toList()))
        ).join();
        return rows.stream();
    }

    default CompletableFuture<Void> getRows(
            DataSeries dataSeries,
            Consumer<Stream<List<String>>> consumer
    ) {
        return getRows(
                dataSeries,
                consumer,
                Instant.parse("1970-01-01T00:00:00.00Z"),
                Instant.parse("2100-12-31T23:59:59.00Z")
        );
    }

    CompletableFuture<Void> getRows(
            DataSeries dataSeries,
            Consumer<Stream<List<String>>> consumer,
            Instant start,
            Instant end
    );

    TypeMetadata getMetadata(DataConnectionType type);

    DataConnectionMetadata getConnectionMetadata(DataConnection dataConnection);

    default List<Class<?>> getSupportedTypes(DataSeries dataSeries) {
        return Types.getSupportedTypes();
    }
}
