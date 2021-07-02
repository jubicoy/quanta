package fi.jubic.quanta.external;

import fi.jubic.quanta.external.importer.Types;
import fi.jubic.quanta.models.DataConnection;
import fi.jubic.quanta.models.DataConnectionType;
import fi.jubic.quanta.models.DataSample;
import fi.jubic.quanta.models.DataSeries;
import fi.jubic.quanta.models.metadata.DataConnectionMetadata;
import fi.jubic.quanta.models.typemetadata.TypeMetadata;

import java.util.List;
import java.util.stream.Stream;

public interface Importer {
    boolean test(DataConnection dataConnection);

    DataConnection validate(DataConnection dataConnection);

    DataConnection getWithEmptyLogin(DataConnection dataConnection);

    DataSample getSample(DataSeries dataSeries, int rows);

    Stream<List<String>> getRows(DataSeries dataSeries);

    TypeMetadata getMetadata(DataConnectionType type);

    DataConnectionMetadata getConnectionMetadata(DataConnection dataConnection);

    default List<Class<?>> getSupportedTypes(DataSeries dataSeries) {
        return Types.getSupportedTypes();
    }
}
